package dev.satherov.sathlib.client.lang;

import dev.satherov.sathlib.network.chat.SLComponent;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
    default MutableComponent translate() {
        return Component.translatable(this.key());
    }
    
    ///
    /// Creates a translation component with the given translation key and
    /// translation arguments to be interpreted into {@link ChatFormatting} and args
    ///
    /// @return Translation component
    ///
    /// @see SLComponent#identify(String, Object...)
    ///
    default MutableComponent translate(Object... args) {
        return SLComponent.identify(this.key(), args);
    }
    
    ///
    /// Creates an {@link SLComponent} from this translation key
    ///
    /// @return SLComponent
    ///
    default SLComponent text() {
        return SLComponent.of(this.translate());
    }
    
    ///
    /// Creates an {@link SLComponent} from this translation key and
    /// translation arguments to be interpreted into {@link ChatFormatting} and args
    ///
    /// @return SLComponent
    ///
    default SLComponent text(Object... args) {
        return SLComponent.of(this.translate(args));
    }
}
