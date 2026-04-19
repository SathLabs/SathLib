package dev.satherov.sathlib.network.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import org.jspecify.annotations.Nullable;

///
/// Fluent builder for Minecraft text {@link Style} instances.
///
public class SLStyle {
    
    private Style style = Style.EMPTY;
    
    ///
    /// Creates a style builder starting from {@link Style#EMPTY}.
    ///
    public SLStyle() { }
    
    ///
    /// Sets the text color from a chat formatting entry.
    ///
    /// @param color chat formatting color, or {@code null}
    ///
    /// @return this style builder
    ///
    public SLStyle color(@Nullable ChatFormatting color) {
        this.style = this.style.withColor(color);
        return this;
    }
    
    ///
    /// Sets the text color from a packed RGB integer.
    ///
    /// @param color packed RGB color
    ///
    /// @return this style builder
    ///
    public SLStyle color(int color) {
        this.style = this.style.withColor(color);
        return this;
    }
    
    ///
    /// Sets the text color from a {@link TextColor}.
    ///
    /// @param color text color
    ///
    /// @return this style builder
    ///
    public SLStyle color(TextColor color) {
        this.style = this.style.withColor(color);
        return this;
    }
    
    ///
    /// Sets the shadow color.
    ///
    /// @param color packed shadow color
    ///
    /// @return this style builder
    ///
    public SLStyle shadow(int color) {
        this.style = this.style.withShadowColor(color);
        return this;
    }
    
    ///
    /// Removes any shadow color from the style.
    ///
    /// @return this style builder
    ///
    public SLStyle withoutShadow() {
        this.style = this.style.withoutShadow();
        return this;
    }
    
    ///
    /// Enables or disables bold text.
    ///
    /// @param bold whether bold text should be enabled
    ///
    /// @return this style builder
    ///
    public SLStyle bold(boolean bold) {
        this.style = this.style.withBold(bold);
        return this;
    }
    
    ///
    /// Enables bold text.
    ///
    /// @return this style builder
    ///
    public SLStyle bold() {
        return this.bold(true);
    }
    
    ///
    /// Enables or disables italic text.
    ///
    /// @param italic whether italic text should be enabled
    ///
    /// @return this style builder
    ///
    public SLStyle italic(boolean italic) {
        this.style = this.style.withItalic(italic);
        return this;
    }
    
    ///
    /// Enables italic text.
    ///
    /// @return this style builder
    ///
    public SLStyle italic() {
        return this.italic(true);
    }
    
    ///
    /// Enables or disables underlined text.
    ///
    /// @param underlined whether underlining should be enabled
    ///
    /// @return this style builder
    ///
    public SLStyle underlined(boolean underlined) {
        this.style = this.style.withUnderlined(underlined);
        return this;
    }
    
    ///
    /// Enables underlined text.
    ///
    /// @return this style builder
    ///
    public SLStyle underlined() {
        return this.underlined(true);
    }
    
    ///
    /// Enables or disables strikethrough text.
    ///
    /// @param strikethrough whether strikethrough should be enabled
    ///
    /// @return this style builder
    ///
    public SLStyle strikethrough(boolean strikethrough) {
        this.style = this.style.withStrikethrough(strikethrough);
        return this;
    }
    
    ///
    /// Enables strikethrough text.
    ///
    /// @return this style builder
    ///
    public SLStyle strikethrough() {
        return this.strikethrough(true);
    }
    
    ///
    /// Enables or disables obfuscated text.
    ///
    /// @param obfuscated whether obfuscation should be enabled
    ///
    /// @return this style builder
    ///
    public SLStyle obfuscated(boolean obfuscated) {
        this.style = this.style.withObfuscated(obfuscated);
        return this;
    }
    
    ///
    /// Enables obfuscated text.
    ///
    /// @return this style builder
    ///
    public SLStyle obfuscated() {
        return this.obfuscated(true);
    }
    
    ///
    /// Sets the click event.
    ///
    /// @param clickEvent click event to apply
    ///
    /// @return this style builder
    ///
    public SLStyle clickEvent(ClickEvent clickEvent) {
        this.style = this.style.withClickEvent(clickEvent);
        return this;
    }
    
    ///
    /// Sets the hover event.
    ///
    /// @param hoverEvent hover event to apply
    ///
    /// @return this style builder
    ///
    public SLStyle hoverEvent(HoverEvent hoverEvent) {
        this.style = this.style.withHoverEvent(hoverEvent);
        return this;
    }
    
    ///
    /// Sets insertion text for shift-click insertion.
    ///
    /// @param insertion insertion text
    ///
    /// @return this style builder
    ///
    public SLStyle insertion(String insertion) {
        this.style = this.style.withInsertion(insertion);
        return this;
    }
    
    ///
    /// Sets the font description used by the style.
    ///
    /// @param description font description
    ///
    /// @return this style builder
    ///
    public SLStyle font(FontDescription description) {
        this.style = this.style.withFont(description);
        return this;
    }
    
    ///
    /// Returns the built immutable {@link Style}.
    ///
    /// @return created style
    ///
    public Style create() {
        return this.style;
    }
}
