package dev.satherov.sathlib.client.model.data;

import com.google.common.collect.ImmutableMap;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public class SLModelPropertyFieldHolder {
    
    private final Map<SLModelPropertyField<?>, Object> values;
    private final Map<String, SLModelPropertyField<?>> fields;
    
    private SLModelPropertyFieldHolder(Map<SLModelPropertyField<?>, Object> values, Map<String, SLModelPropertyField<?>> fields) {
        this.values = values;
        this.fields = fields;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public Map<String, String> asMap() {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Map.Entry<SLModelPropertyField<?>, Object> entry : this.values.entrySet()) {
            final SLModelPropertyField<?> field = entry.getKey();
            final String value = field.serializeUnchecked(entry.getValue());
            builder.put(field.name(), value);
        }
        return builder.build();
    }
    
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(SLModelPropertyField<T> field) {
        return (T) this.values.get(field);
    }
    
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(String name) {
        SLModelPropertyField<T> field = (SLModelPropertyField<T>) this.fields.get(name);
        if (field == null) return null;
        return this.get(field);
    }
    
    @SuppressWarnings("unchecked")
    public <T> @Nullable SLModelPropertyField<T> getField(String name) {
        return (SLModelPropertyField<T>) this.fields.get(name);
    }
    
    public boolean matches(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            SLModelPropertyField<?> field = this.fields.get(entry.getKey());
            if (field == null) return false;
            
            final Object value = this.values.get(field);
            if (value == null) return false;
            
            if (!field.matchesSerializedUnchecked(value, entry.getValue())) return false;
        }
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof SLModelPropertyFieldHolder other)) return false;
        return this.values.equals(other.values);
    }
    
    @Override
    public int hashCode() {
        return this.values.hashCode() * 31;
    }
    
    public static class Builder {
        
        private final ImmutableMap.Builder<SLModelPropertyField<?>, Object> values = new ImmutableMap.Builder<>();
        private final ImmutableMap.Builder<String, SLModelPropertyField<?>> fields = new ImmutableMap.Builder<>();
        
        public <T> Builder put(SLModelPropertyField<T> field, T value) {
            this.values.put(field, value);
            this.fields.put(field.name(), field);
            return this;
        }
        
        public SLModelPropertyFieldHolder build() {
            return new SLModelPropertyFieldHolder(this.values.build(), this.fields.build());
        }
    }
}
