package dev.satherov.sathlib.client.screen.style;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.common.menu.slot.SLSlotVisuals;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;

///
/// Default theme shipped with the SathLib UI framework.
///
/// A single shared instance is usually enough for the whole mod session.
///
public enum DefaultTheme implements UITheme {
    ///
    /// Shared built-in theme instance.
    ///
    INSTANCE;
    
    private static final UIThemeColors COLORS = new UIThemeColors(
            0x7A07090C,
            0x24000000,
            0xF71A1F26,
            0xF7101419,
            0xD10D1116,
            0xFF46515F,
            0xCC080B0F,
            0xFF232A33,
            0xFFD6DEE7,
            0xFF7CC4FF,
            0xFF6A869F,
            0xFFE8EDF3,
            0xFF7F8A96,
            0xFF8B97AB,
            0x666A869F,
            0xFFFFFFFF
    );
    
    DefaultTheme() { }
    
    @Override
    public UIThemeColors colors() {
        return DefaultTheme.COLORS;
    }
    
    @Override
    public void renderPanel(SLRenderContext context, SLBounds bounds) {
        int shadowInset = 8;
        context.fill(
                new SLBounds(bounds.x() - shadowInset, bounds.y() - shadowInset, bounds.width() + (shadowInset * 2), bounds.height() + (shadowInset * 2)),
                DefaultTheme.COLORS.panelShadow()
        );
        context.fillVerticalGradient(bounds, DefaultTheme.COLORS.panelFillTop(), DefaultTheme.COLORS.panelFillBottom());
        context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, Math.max(0, bounds.width() - 2), Math.max(0, bounds.height() - 2)), DefaultTheme.COLORS.panelInset());
        context.outline(bounds, DefaultTheme.COLORS.panelBorder());
    }
    
    @Override
    public void renderButton(
            SLRenderContext context,
            SLBounds bounds,
            Component text,
            boolean hovered,
            boolean pressed,
            boolean enabled
    ) {
        int fillColor = DefaultTheme.COLORS.panelFillTop();
        if (!enabled) {
            fillColor = SLColorUtils.lerp(0.45F, DefaultTheme.COLORS.panelFillTop(), DefaultTheme.COLORS.insetFill());
        } else if (pressed) {
            fillColor = SLColorUtils.lerp(0.35F, DefaultTheme.COLORS.panelFillTop(), DefaultTheme.COLORS.insetFill());
        } else if (hovered) {
            fillColor = SLColorUtils.lerp(0.22F, DefaultTheme.COLORS.panelFillTop(), DefaultTheme.COLORS.accent());
        }
        
        context.fill(bounds, fillColor);
        context.outline(bounds, DefaultTheme.COLORS.panelBorder());
        context.centeredVisualText(text, bounds, this.labelColor(enabled), false);
    }
    
    @Override
    public void renderProgressBar(
            SLRenderContext context,
            SLBounds bounds,
            float progress,
            @Nullable Component overlay,
            boolean enabled
    ) {
        float clampedProgress = Mth.clamp(progress, 0.0F, 1.0F);
        context.fill(bounds, DefaultTheme.COLORS.insetFill());
        context.outline(bounds, DefaultTheme.COLORS.insetBorder());
        
        int innerWidth = Math.max(0, bounds.width() - 2);
        int innerHeight = Math.max(0, bounds.height() - 2);
        int fillWidth = Math.round(innerWidth * clampedProgress);
        if (fillWidth > 0 && innerHeight > 0) {
            int fillColor = enabled ? DefaultTheme.COLORS.accent() : SLColorUtils.lerp(0.5F, DefaultTheme.COLORS.accent(), DefaultTheme.COLORS.insetBorder());
            context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, fillWidth, innerHeight), fillColor);
        }
        
        if (overlay != null) {
            context.centeredText(overlay, bounds, this.labelColor(enabled), false);
        }
    }
    
    @Override
    public void renderTextField(SLRenderContext context, SLBounds bounds, SLTextFieldRenderState renderState) {
        int outlineColor = renderState.focused() ? DefaultTheme.COLORS.insetStrong() : DefaultTheme.COLORS.panelBorder();
        context.fill(bounds, DefaultTheme.COLORS.insetFill());
        context.outline(bounds, outlineColor);
        
        String renderText = renderState.value().isEmpty() && renderState.placeholder() != null
                ? renderState.placeholder()
                : renderState.value();
        int textColor = renderState.value().isEmpty()
                ? DefaultTheme.COLORS.textMuted()
                : this.labelColor(renderState.enabled());
        int textX = bounds.x() + 4;
        int textY = bounds.y() + ((bounds.height() - context.font().lineHeight) / 2);
        
        if (renderState.hasSelection() && !renderState.value().isEmpty()) {
            int selectionStart = Math.min(renderState.selectionStart(), renderState.selectionEnd());
            int selectionEnd = Math.max(renderState.selectionStart(), renderState.selectionEnd());
            int highlightX = textX + context.font().width(renderState.value().substring(0, selectionStart));
            int highlightRight = textX + context.font().width(renderState.value().substring(0, selectionEnd));
            context.graphics().fill(highlightX, textY - 1, highlightRight, textY + context.font().lineHeight + 1, DefaultTheme.COLORS.selection());
        }
        
        context.graphics().text(context.font(), renderText, textX, textY, textColor, false);
        
        if (renderState.focused()) {
            int cursorX = textX + context.font().width(renderState.value().substring(0, renderState.cursor()));
            context.graphics().fill(cursorX, textY, cursorX + 1, textY + context.font().lineHeight, DefaultTheme.COLORS.white());
        }
    }
    
    @Override
    public void renderSlotFrame(
            SLRenderContext context,
            SLBounds bounds,
            SLSlotVisuals visuals,
            boolean hovered,
            boolean active
    ) {
        if (!visuals.drawFrame()) {
            return;
        }
        
        float inactiveBlend = active ? 0.0F : 0.45F;
        int fillColor = inactiveBlend > 0.0F
                ? SLColorUtils.lerp(inactiveBlend, visuals.fillColor(), DefaultTheme.COLORS.insetFill())
                : visuals.fillColor();
        int borderColor = inactiveBlend > 0.0F
                ? SLColorUtils.lerp(inactiveBlend, visuals.borderColor(), DefaultTheme.COLORS.insetBorder())
                : visuals.borderColor();
        
        if (hovered) {
            fillColor = SLColorUtils.lerp(0.18F, fillColor, DefaultTheme.COLORS.accent());
            borderColor = SLColorUtils.lerp(0.22F, borderColor, DefaultTheme.COLORS.accent());
        }
        
        context.fill(bounds, fillColor);
        context.outline(bounds, borderColor);
    }
    
    @Override
    public int labelColor(boolean enabled) {
        return enabled ? DefaultTheme.COLORS.textPrimary() : DefaultTheme.COLORS.textDisabled();
    }
    
    @Override
    public int accentColor() {
        return DefaultTheme.COLORS.accent();
    }
}
