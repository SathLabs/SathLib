package dev.satherov.sathlib.client.model.data;

import com.google.common.collect.ImmutableMap;

import org.jspecify.annotations.Nullable;

import java.util.Map;

///
/// Immutable holder for serialized model-property fields and values.
///
public class SLModelPropertyFieldHolder {
    
    private final Map<SLModelPropertyField<?>, Object> values;
    private final Map<String, SLModelPropertyField<?>> fields;
    
    private SLModelPropertyFieldHolder(Map<SLModelPropertyField<?>, Object> values, Map<String, SLModelPropertyField<?>> fields) {
        this.values = values;
        this.fields = fields;
    }
    
    ///
    /// Starts building a field holder.
    ///
    /// @return new builder
    ///
    public static Builder builder() {
        return new Builder();
    }
    
    ///
    /// Returns all stored fields as serialized string values.
    ///
    /// @return serialized field-value map
    ///
    public Map<String, String> asMap() {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Map.Entry<SLModelPropertyField<?>, Object> entry : this.values.entrySet()) {
            final SLModelPropertyField<?> field = entry.getKey();
            final String value = field.serializeUnchecked(entry.getValue());
            builder.put(field.name(), value);
        }
        return builder.build();
    }
    
    ///
    /// Returns the stored value for one field key.
    ///
    /// @param <T>   field value type
    /// @param field field descriptor
    ///
    /// @return stored value, or `null`
    ///
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(SLModelPropertyField<T> field) {
        return (T) this.values.get(field);
    }
    
    ///
    /// Returns the stored value for one field name.
    ///
    /// @param <T>  field value type
    /// @param name serialized field name
    ///
    /// @return stored value, or `null`
    ///
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(String name) {
        SLModelPropertyField<T> field = (SLModelPropertyField<T>) this.fields.get(name);
        if (field == null) return null;
        return this.get(field);
    }
    
    ///
    /// Resolves one field descriptor by name.
    ///
    /// @param <T>  field value type
    /// @param name serialized field name
    ///
    /// @return field descriptor, or `null`
    ///
    @SuppressWarnings("unchecked")
    public <T> @Nullable SLModelPropertyField<T> getField(String name) {
        return (SLModelPropertyField<T>) this.fields.get(name);
    }
    
    ///
    /// Checks whether all supplied serialized values match this holder.
    ///
    /// @param values serialized values to compare
    ///
    /// @return `true` when every supplied field matches
    ///
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
    
    ///
    /// Mutable builder for {@link SLModelPropertyFieldHolder}.
    ///
    public static class Builder {
        
        private final ImmutableMap.Builder<SLModelPropertyField<?>, Object> values = new ImmutableMap.Builder<>();
        private final ImmutableMap.Builder<String, SLModelPropertyField<?>> fields = new ImmutableMap.Builder<>();
        
        private Builder() { }
        
        ///
        /// Adds one field/value pair.
        ///
        /// @param <T>   field value type
        /// @param field field descriptor
        /// @param value field value
        ///
        /// @return this builder
        ///
        public <T> Builder put(SLModelPropertyField<T> field, T value) {
            this.values.put(field, value);
            this.fields.put(field.name(), field);
            return this;
        }
        
        ///
        /// Builds the immutable field holder.
        ///
        /// @return built field holder
        ///
        public SLModelPropertyFieldHolder build() {
            return new SLModelPropertyFieldHolder(this.values.build(), this.fields.build());
        }
    }
}
