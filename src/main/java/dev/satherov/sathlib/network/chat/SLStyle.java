package dev.satherov.sathlib.network.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import org.jspecify.annotations.Nullable;

public class SLStyle {
    
    private Style style = Style.EMPTY;
    
    public SLStyle color(@Nullable ChatFormatting color) {
        this.style = this.style.withColor(color);
        return this;
    }
    
    public SLStyle color(int color) {
        this.style = this.style.withColor(color);
        return this;
    }
    
    public SLStyle color(TextColor color) {
        this.style = this.style.withColor(color);
        return this;
    }
    
    public SLStyle shadow(int color) {
        this.style = this.style.withShadowColor(color);
        return this;
    }
    
    public SLStyle withoutShadow() {
        this.style = this.style.withoutShadow();
        return this;
    }
    
    public SLStyle bold(boolean bold) {
        this.style = this.style.withBold(bold);
        return this;
    }
    
    public SLStyle bold() {
        return this.bold(true);
    }
    
    public SLStyle italic(boolean italic) {
        this.style = this.style.withItalic(italic);
        return this;
    }
    
    public SLStyle italic() {
        return this.italic(true);
    }
    
    public SLStyle underlined(boolean underlined) {
        this.style = this.style.withUnderlined(underlined);
        return this;
    }
    
    public SLStyle underlined() {
        return this.underlined(true);
    }
    
    public SLStyle strikethrough(boolean strikethrough) {
        this.style = this.style.withStrikethrough(strikethrough);
        return this;
    }
    
    public SLStyle strikethrough() {
        return this.strikethrough(true);
    }
    
    public SLStyle obfuscated(boolean obfuscated) {
        this.style = this.style.withObfuscated(obfuscated);
        return this;
    }
    
    public SLStyle obfuscated() {
        return this.obfuscated(true);
    }
    
    public SLStyle clickEvent(ClickEvent clickEvent) {
        this.style = this.style.withClickEvent(clickEvent);
        return this;
    }
    
    public SLStyle hoverEvent(HoverEvent hoverEvent) {
        this.style = this.style.withHoverEvent(hoverEvent);
        return this;
    }
    
    public SLStyle insertion(String insertion) {
        this.style = this.style.withInsertion(insertion);
        return this;
    }
    
    public SLStyle font(FontDescription description) {
        this.style = this.style.withFont(description);
        return this;
    }
    
    public Style create() {
        return this.style;
    }
}
