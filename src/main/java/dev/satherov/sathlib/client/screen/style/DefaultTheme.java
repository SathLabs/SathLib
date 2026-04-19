package dev.satherov.sathlib.client.screen.style;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.common.menu.slot.SLSlotVisuals;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;

///
/// Default skin shipped with the SathLib UI framework.
///
/// A single shared instance is usually enough for the whole mod session.
///
/// - provide a clean baseline visual language for built-in widgets
/// - keep the built-in nodes readable without exposing raw colors everywhere
///
/// Replace this implementation when a project wants a different visual identity.
///
public enum DefaultTheme implements UITheme {
    /// Shared singleton instance of the built-in theme.
    INSTANCE;
    
    private static final int PANEL_FILL = 0xEE1D2431;
    private static final int PANEL_BORDER = 0xFF55627C;
    private static final int BUTTON_FILL = 0xFF314059;
    private static final int BUTTON_HOVER = 0xFF3D5374;
    private static final int BUTTON_PRESS = 0xFF28364C;
    private static final int BUTTON_DISABLED = 0xFF202833;
    private static final int BUTTON_BORDER = 0xFF90A4C4;
    private static final int TRACK_FILL = 0xFF1A2230;
    private static final int TRACK_BORDER = 0xFF617796;
    private static final int ACCENT = 0xFF7CC4FF;
    private static final int TEXT = 0xFFF4F7FB;
    private static final int TEXT_DISABLED = 0xFF8B97AB;
    
    DefaultTheme() { }
    
    @Override
    public void renderPanel(SLRenderContext context, SLBounds bounds) {
        context.fill(bounds, DefaultTheme.PANEL_FILL);
        context.outline(bounds, DefaultTheme.PANEL_BORDER);
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
        int fillColor = DefaultTheme.BUTTON_FILL;
        if (!enabled) fillColor = DefaultTheme.BUTTON_DISABLED;
        else if (pressed) fillColor = DefaultTheme.BUTTON_PRESS;
        else if (hovered) fillColor = DefaultTheme.BUTTON_HOVER;
        
        context.fill(bounds, fillColor);
        context.outline(bounds, DefaultTheme.BUTTON_BORDER);
        context.centeredText(text, bounds, this.labelColor(enabled), false);
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
        context.fill(bounds, DefaultTheme.TRACK_FILL);
        context.outline(bounds, DefaultTheme.TRACK_BORDER);
        
        int innerWidth = Math.max(0, bounds.width() - 2);
        int innerHeight = Math.max(0, bounds.height() - 2);
        int fillWidth = Math.round(innerWidth * clampedProgress);
        if (fillWidth > 0 && innerHeight > 0) {
            int fillColor = enabled ? DefaultTheme.ACCENT : SLColorUtils.lerp(0.5F, DefaultTheme.ACCENT, DefaultTheme.TRACK_BORDER);
            context.fill(
                    new SLBounds(bounds.x() + 1, bounds.y() + 1, fillWidth, innerHeight),
                    fillColor
            );
        }
        
        if (overlay != null) context.centeredText(overlay, bounds, this.labelColor(enabled), false);
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
                ? SLColorUtils.lerp(inactiveBlend, visuals.fillColor(), DefaultTheme.TRACK_FILL)
                : visuals.fillColor();
        int borderColor = inactiveBlend > 0.0F
                ? SLColorUtils.lerp(inactiveBlend, visuals.borderColor(), DefaultTheme.TRACK_BORDER)
                : visuals.borderColor();
        
        if (hovered) {
            fillColor = SLColorUtils.lerp(0.18F, fillColor, DefaultTheme.ACCENT);
            borderColor = SLColorUtils.lerp(0.22F, borderColor, DefaultTheme.ACCENT);
        }
        
        context.fill(bounds, fillColor);
        context.outline(bounds, borderColor);
    }
    
    @Override
    public int labelColor(boolean enabled) {
        return enabled ? DefaultTheme.TEXT : DefaultTheme.TEXT_DISABLED;
    }
    
    @Override
    public int accentColor() {
        return DefaultTheme.ACCENT;
    }
}
