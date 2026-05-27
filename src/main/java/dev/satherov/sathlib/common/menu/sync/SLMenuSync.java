package dev.satherov.sathlib.common.menu.sync;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

///
/// Explicit `DataSlot` registration helper for SathLib menus.
///
/// This replaces the old reflection-based sync path with a small API that keeps
/// synchronization close to the menu fields that actually need it. Menus opt in
/// field by field and can mix primitive values, enums, or existing
/// {@link ContainerData} instances.
///
public final class SLMenuSync {
    
    private final Consumer<DataSlot> slotRegistrar;
    
    ///
    /// Creates a sync helper backed by the passed slot registrar.
    ///
    /// @param slotRegistrar consumer that should receive each created data slot
    ///
    public SLMenuSync(Consumer<DataSlot> slotRegistrar) {
        this.slotRegistrar = Objects.requireNonNull(slotRegistrar);
    }
    
    ///
    /// Registers a boolean value.
    ///
    /// @param getter current-value supplier
    /// @param setter client-apply consumer
    ///
    public void trackBoolean(BooleanSupplier getter, Consumer<Boolean> setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        this.register(new DataSlot() {
            @Override
            public int get() {
                return getter.getAsBoolean() ? 1 : 0;
            }
            
            @Override
            public void set(int value) {
                setter.accept(value != 0);
            }
        });
    }
    
    ///
    /// Registers an `int` value.
    ///
    /// @param getter current-value supplier
    /// @param setter client-apply consumer
    ///
    public void trackInt(IntSupplier getter, IntConsumer setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        this.register(new DataSlot() {
            @Override
            public int get() {
                return getter.getAsInt();
            }
            
            @Override
            public void set(int value) {
                setter.accept(value);
            }
        });
    }
    
    ///
    /// Registers a byte value.
    ///
    /// @param getter current-value supplier
    /// @param setter client-apply consumer
    ///
    public void trackByte(IntSupplier getter, IntConsumer setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        this.trackInt(() -> (byte) getter.getAsInt(), value -> setter.accept((byte) value));
    }
    
    ///
    /// Registers a short value.
    ///
    /// @param getter current-value supplier
    /// @param setter client-apply consumer
    ///
    public void trackShort(IntSupplier getter, IntConsumer setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        this.trackInt(() -> (short) getter.getAsInt(), value -> setter.accept((short) value));
    }
    
    ///
    /// Registers a long value across two `DataSlot`s.
    ///
    /// @param getter current-value supplier
    /// @param setter client-apply consumer
    ///
    public void trackLong(LongSupplier getter, LongConsumer setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        
        this.register(new DataSlot() {
            @Override
            public int get() {
                return (int) (getter.getAsLong() >>> 32);
            }
            
            @Override
            public void set(int value) {
                long currentValue = getter.getAsLong();
                long nextValue = (currentValue & 0x00000000FFFFFFFFL) | ((long) value << 32);
                setter.accept(nextValue);
            }
        });
        this.register(new DataSlot() {
            @Override
            public int get() {
                return (int) getter.getAsLong();
            }
            
            @Override
            public void set(int value) {
                long currentValue = getter.getAsLong();
                long nextValue = (currentValue & 0xFFFFFFFF00000000L) | (value & 0xFFFFFFFFL);
                setter.accept(nextValue);
            }
        });
    }
    
    ///
    /// Registers a float value.
    ///
    /// @param getter current-value supplier
    /// @param setter client-apply consumer
    ///
    public void trackFloat(Supplier<Float> getter, Consumer<Float> setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        this.register(new DataSlot() {
            @Override
            public int get() {
                return Float.floatToIntBits(getter.get());
            }
            
            @Override
            public void set(int value) {
                setter.accept(Float.intBitsToFloat(value));
            }
        });
    }
    
    ///
    /// Registers a double value across two `DataSlot`s.
    ///
    /// @param getter current-value supplier
    /// @param setter client-apply consumer
    ///
    public void trackDouble(DoubleSupplier getter, DoubleConsumer setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        
        this.register(new DataSlot() {
            @Override
            public int get() {
                return (int) (Double.doubleToLongBits(getter.getAsDouble()) >>> 32);
            }
            
            @Override
            public void set(int value) {
                long currentValue = Double.doubleToLongBits(getter.getAsDouble());
                long nextValue = (currentValue & 0x00000000FFFFFFFFL) | ((long) value << 32);
                setter.accept(Double.longBitsToDouble(nextValue));
            }
        });
        this.register(new DataSlot() {
            @Override
            public int get() {
                return (int) Double.doubleToLongBits(getter.getAsDouble());
            }
            
            @Override
            public void set(int value) {
                long currentValue = Double.doubleToLongBits(getter.getAsDouble());
                long nextValue = (currentValue & 0xFFFFFFFF00000000L) | (value & 0xFFFFFFFFL);
                setter.accept(Double.longBitsToDouble(nextValue));
            }
        });
    }
    
    ///
    /// Registers an enum value by ordinal.
    ///
    /// @param enumType enum class used to resolve incoming ordinals
    /// @param getter   current-value supplier
    /// @param setter   client-apply consumer
    /// @param <E>      enum type
    ///
    public <E extends Enum<E>> void trackEnum(Class<E> enumType, Supplier<E> getter, Consumer<E> setter) {
        Objects.requireNonNull(enumType);
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);
        
        E[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new IllegalArgumentException("Enum type " + enumType.getName() + " does not expose constants.");
        }
        
        this.register(new DataSlot() {
            @Override
            public int get() {
                return getter.get().ordinal();
            }
            
            @Override
            public void set(int value) {
                int clampedOrdinal = Math.clamp(value, 0, constants.length - 1);
                setter.accept(constants[clampedOrdinal]);
            }
        });
    }
    
    ///
    /// Registers an existing vanilla `ContainerData` object.
    ///
    /// @param data container data to mirror through menu data slots
    ///
    public void trackContainerData(ContainerData data) {
        Objects.requireNonNull(data);
        
        for (int slotIndex = 0; slotIndex < data.getCount(); slotIndex++) {
            final int dataIndex = slotIndex;
            this.register(new DataSlot() {
                @Override
                public int get() {
                    return data.get(dataIndex);
                }
                
                @Override
                public void set(int value) {
                    data.set(dataIndex, value);
                }
            });
        }
    }
    
    ///
    /// Forwards a created data slot to the owning menu.
    ///
    /// @param dataSlot generated data slot
    ///
    private void register(DataSlot dataSlot) {
        this.slotRegistrar.accept(dataSlot);
    }
}
