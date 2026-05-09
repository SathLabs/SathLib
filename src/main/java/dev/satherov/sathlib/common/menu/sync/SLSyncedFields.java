package dev.satherov.sathlib.common.menu.sync;

import dev.satherov.sathlib.util.SLReflectionUtils;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;

import org.jspecify.annotations.NonNull;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

///
/// Reflection-backed sync binding for {@link SyncedField} menu fields.
///
/// This utility scans a menu class hierarchy once, caches the resulting field
/// bindings, and exposes the generated `DataSlot` instances to the menu base.
///
/// - remove raw sync-index bookkeeping from menus
/// - support multiple primitive field types and enums
/// - keep the actual NeoForge sync mechanism inside one small class
///
public final class SLSyncedFields {
    
    private static final SyncedBindings BINDINGS = new SyncedBindings();
    
    private SLSyncedFields() { }
    
    ///
    /// Visits every generated `DataSlot` for the given menu host.
    ///
    /// @param host     menu instance hosting the fields
    /// @param consumer consumer receiving generated data slots
    ///
    public static void forEachDataSlot(Object host, Consumer<DataSlot> consumer) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(consumer);
        
        for (FieldBinding binding : SLSyncedFields.BINDINGS.get(host.getClass())) {
            for (int partIndex = 0; partIndex < binding.codec.slotCount(); partIndex++) {
                consumer.accept(new SyncedDataSlot(host, binding, partIndex));
            }
        }
    }
    
    private static List<FieldBinding> collectBindings(Class<?> type) {
        List<FieldBinding> bindings = new ArrayList<>();
        List<Class<?>> hierarchy = new ArrayList<>();
        
        for (Class<?> currentClass = type; currentClass != null; currentClass = currentClass.getSuperclass()) {
            hierarchy.add(currentClass);
            if (currentClass == AbstractContainerMenu.class) {
                break;
            }
        }
        
        for (int hierarchyIndex = hierarchy.size() - 1; hierarchyIndex >= 0; hierarchyIndex--) {
            Class<?> currentClass = hierarchy.get(hierarchyIndex);
            if (currentClass == AbstractContainerMenu.class || currentClass == Object.class) continue;
            
            Field[] declaredFields = currentClass.getDeclaredFields();
            Arrays.sort(declaredFields, Comparator.comparing(Field::getName));
            
            for (Field declaredField : declaredFields) {
                if (!declaredField.isAnnotationPresent(SyncedField.class)) continue;
                
                int modifiers = declaredField.getModifiers();
                if (Modifier.isStatic(modifiers)) throw new IllegalStateException("Cannot sync static field " + currentClass.getName() + "#" + declaredField.getName());
                if (Modifier.isFinal(modifiers)) throw new IllegalStateException("Cannot sync final field " + currentClass.getName() + "#" + declaredField.getName());
                
                FieldCodec codec = SLSyncedFields.createCodec(declaredField);
                VarHandle handle = SLReflectionUtils.findVarHandle(currentClass, declaredField.getType(), declaredField.getName());
                bindings.add(new FieldBinding(currentClass, declaredField.getName(), handle, codec));
            }
        }
        
        return bindings;
    }
    
    private static FieldCodec createCodec(Field field) {
        return switch (field.getType()) {
            case Class<?> type when type == boolean.class -> BooleanCodec.INSTANCE;
            case Class<?> type when type == byte.class || type == short.class || type == int.class -> new IntLikeCodec(type);
            case Class<?> type when type == long.class -> LongCodec.INSTANCE;
            case Class<?> type when type == float.class -> FloatCodec.INSTANCE;
            case Class<?> type when type == double.class -> DoubleCodec.INSTANCE;
            case Class<?> type when type.isEnum() -> {
                @SuppressWarnings("unchecked")
                Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type.asSubclass(Enum.class);
                yield new EnumCodec(enumType);
            }
            default -> throw new IllegalStateException("Unsupported synced field type: " + field.getDeclaringClass().getName() + "#" + field.getName());
        };
    }
    
    private enum BooleanCodec implements FieldCodec {
        INSTANCE;
        
        @Override
        public int slotCount() {
            return 1;
        }
        
        @Override
        public int getPart(VarHandle handle, Object host, int idx) {
            return (boolean) handle.get(host) ? 1 : 0;
        }
        
        @Override
        public void setPart(VarHandle handle, Object host, int idx, int value) {
            handle.set(host, value != 0);
        }
    }
    
    private enum LongCodec implements FieldCodec {
        INSTANCE;
        
        @Override
        public int slotCount() {
            return 2;
        }
        
        @Override
        public int getPart(VarHandle handle, Object host, int idx) {
            long value = (long) handle.get(host);
            if (idx == 0) return (int) (value >>> 32);
            return (int) value;
        }
        
        @Override
        public void setPart(VarHandle handle, Object host, int idx, int value) {
            long currentValue = (long) handle.get(host);
            long nextValue;
            if (idx == 0) nextValue = (currentValue & 0x00000000FFFFFFFFL) | ((long) value << 32);
            else nextValue = (currentValue & 0xFFFFFFFF00000000L) | (value & 0xFFFFFFFFL);
            handle.set(host, nextValue);
        }
    }
    
    private enum FloatCodec implements FieldCodec {
        INSTANCE;
        
        @Override
        public int slotCount() {
            return 1;
        }
        
        @Override
        public int getPart(VarHandle handle, Object host, int idx) {
            return Float.floatToIntBits((float) handle.get(host));
        }
        
        @Override
        public void setPart(VarHandle handle, Object host, int idx, int value) {
            handle.set(host, Float.intBitsToFloat(value));
        }
    }
    
    private enum DoubleCodec implements FieldCodec {
        INSTANCE;
        
        @Override
        public int slotCount() {
            return 2;
        }
        
        @Override
        public int getPart(VarHandle handle, Object host, int idx) {
            long rawValue = Double.doubleToLongBits((double) handle.get(host));
            if (idx == 0) return (int) (rawValue >>> 32);
            return (int) rawValue;
        }
        
        @Override
        public void setPart(VarHandle handle, Object host, int idx, int value) {
            long currentRawValue = Double.doubleToLongBits((double) handle.get(host));
            long nextRawValue;
            if (idx == 0) nextRawValue = (currentRawValue & 0x00000000FFFFFFFFL) | ((long) value << 32);
            else nextRawValue = (currentRawValue & 0xFFFFFFFF00000000L) | (value & 0xFFFFFFFFL);
            handle.set(host, Double.longBitsToDouble(nextRawValue));
        }
    }
    
    private interface FieldCodec {
        
        int slotCount();
        
        int getPart(VarHandle handle, Object host, int idx);
        
        void setPart(VarHandle handle, Object host, int idx, int value);
    }
    
    private record FieldBinding(Class<?> owner, String name, VarHandle handle, FieldCodec codec) {
    }
    
    private static final class SyncedDataSlot extends DataSlot {
        
        private final Object host;
        private final FieldBinding binding;
        private final int partIndex;
        
        private SyncedDataSlot(Object host, FieldBinding binding, int partIndex) {
            this.host = host;
            this.binding = binding;
            this.partIndex = partIndex;
        }
        
        @Override
        public int get() {
            return this.binding.codec.getPart(this.binding.handle, this.host, this.partIndex);
        }
        
        @Override
        public void set(int value) {
            this.binding.codec.setPart(this.binding.handle, this.host, this.partIndex, value);
        }
    }
    
    private record IntLikeCodec(Class<?> type) implements FieldCodec {
        
        @Override
        public int slotCount() {
            return 1;
        }
        
        @Override
        public int getPart(VarHandle handle, Object host, int idx) {
            return switch (this.type) {
                case Class<?> clazz when clazz == byte.class -> (byte) handle.get(host);
                case Class<?> clazz when clazz == short.class -> (short) handle.get(host);
                default -> (int) handle.get(host);
            };
        }
        
        @Override
        public void setPart(VarHandle handle, Object host, int idx, int value) {
            switch (this.type) {
                case Class<?> clazz when clazz == byte.class -> handle.set(host, (byte) value);
                case Class<?> clazz when clazz == short.class -> handle.set(host, (short) value);
                default -> handle.set(host, value);
            }
        }
    }
    
    private record EnumCodec(Enum<?>[] constants) implements FieldCodec {
        
        private EnumCodec(Class<? extends Enum<?>> constants) {
            this(constants.getEnumConstants());
        }
        
        @Override
        public int slotCount() {
            return 1;
        }
        
        @Override
        public int getPart(VarHandle handle, Object host, int idx) {
            Enum<?> constant = (Enum<?>) handle.get(host);
            return constant != null ? constant.ordinal() : -1;
        }
        
        @Override
        public void setPart(VarHandle handle, Object host, int idx, int value) {
            if (value < 0 || value >= this.constants.length) handle.set(host, null);
            else handle.set(host, this.constants[value]);
        }
    }
    
    private static final class SyncedBindings extends ClassValue<List<FieldBinding>> {
        
        @Override
        protected List<FieldBinding> computeValue(@NonNull Class<?> type) {
            return List.copyOf(SLSyncedFields.collectBindings(type));
        }
    }
}
