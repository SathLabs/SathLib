package dev.satherov.sathlib.config.data;

///
/// Marks an enum as a config enum.
///
public interface ConfigEnum {
    
    ///
    /// Human-readable description of what the specific enum value does.
    ///
    /// @return Description of the enum value.
    ///
    String description();
}
