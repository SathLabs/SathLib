package dev.satherov.sathlib.client.lang;

import dev.satherov.sathlib.network.chat.SLComponent;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

///
/// Marks a class as being translateable.
/// This should typically be used for language enums
///
public interface SLTranslatable {
    
    ///
    /// Gets the translation key
    ///
    /// @return The translation key
    ///
    String key();
    
    ///
    /// Gets the English translation
    ///
    /// @return The English translation
    ///
    String translation();
    
    ///
    /// Creates a translation component with the given translation key
    ///
    /// @return Translation component
    ///
    default SLComponent translate() {
        return SLComponent.of(Component.translatable(this.key()));
    }
    
    ///
    /// Creates a translation component with the given translation key and
    /// translation arguments to be interpreted into {@link ChatFormatting} and args
    ///
    /// @param args translation arguments passed to the component formatter
    ///
    /// @return Translation component
    ///
    /// @see SLComponent#identify(String, Object...)
    ///
    default SLComponent translate(Object... args) {
        return SLComponent.identify(this.key(), args);
    }
}
