package dev.satherov.sathlib.client.screen.style;

import lombok.NoArgsConstructor;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.slot.SLSlotChrome;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;

///
/// Default theme for the ui.
///
@NoArgsConstructor
public enum DefaultTheme implements UITheme {
    ///
    /// Default instance.
    ///
    INSTANCE;
    
    private static final UIThemeColors COLORS = new UIThemeColors(
            0x8407080B,
            0x30000000,
            0xF726292D,
            0xF717191D,
            0xD11D2024,
            0xFF5D636C,
            0xD00A0C10,
            0xFF353A42,
            0xFFDDE4EC,
            0xFF37D7B2,
            0xFF6F8D89,
            0xFFF2F5F8,
            0xFF9BA4AE,
            0xFF828A93,
            0x7046DDBE,
            0xFFFFFFFF
    );
    
    @Override
    public UIThemeColors colors() {
        return DefaultTheme.COLORS;
    }
    
    @Override
    public void renderPanel(SLRenderContext context, SLBounds bounds) {
        SLBounds outerShadowBounds = new SLBounds(bounds.x() - 6, bounds.y() - 6, bounds.width() + 12, bounds.height() + 12);
        SLBounds innerShadowBounds = new SLBounds(bounds.x() - 3, bounds.y() - 2, bounds.width() + 6, bounds.height() + 7);
        SLBounds innerBounds = bounds.inset(1);
        
        context.fill(outerShadowBounds, SLColorUtils.multiplyAlpha(DefaultTheme.COLORS.panelShadow(), 1.35F));
        context.fill(innerShadowBounds, SLColorUtils.multiplyAlpha(DefaultTheme.COLORS.panelShadow(), 0.8F));
        context.fillVerticalGradient(bounds, DefaultTheme.COLORS.panelFillTop(), DefaultTheme.COLORS.panelFillBottom());
        context.outline(bounds, DefaultTheme.COLORS.panelBorder());
        context.fillVerticalGradient(
                innerBounds,
                DefaultTheme.COLORS.panelInset(),
                SLColorUtils.lerp(0.16F, DefaultTheme.COLORS.panelInset(), DefaultTheme.COLORS.panelFillBottom())
        );
        context.fill(new SLBounds(innerBounds.x(), innerBounds.bottom() - 1, innerBounds.width(), 1), 0x28000000);
        context.fill(new SLBounds(innerBounds.x(), innerBounds.y(), 1, innerBounds.height()), 0x14FFFFFF);
        context.fill(new SLBounds(innerBounds.right() - 1, innerBounds.y(), 1, innerBounds.height()), 0x22000000);
        
        if (innerBounds.width() > 20 && innerBounds.height() > 20) {
            SLBounds innerFrameBounds = innerBounds.inset(6);
            context.outline(innerFrameBounds, SLColorUtils.multiplyAlpha(DefaultTheme.COLORS.panelBorder(), 0.30F));
        }
        
        int cornerAccent = SLColorUtils.multiplyAlpha(SLColorUtils.lerp(0.55F, DefaultTheme.COLORS.panelBorder(), DefaultTheme.COLORS.accent()), 0.75F);
        this.renderCornerAccent(context, innerBounds.x() + 6, innerBounds.y() + 6, cornerAccent, true, true);
        this.renderCornerAccent(context, innerBounds.right() - 22, innerBounds.y() + 6, cornerAccent, false, true);
        this.renderCornerAccent(context, innerBounds.x() + 6, innerBounds.bottom() - 16, cornerAccent, true, false);
        this.renderCornerAccent(context, innerBounds.right() - 22, innerBounds.bottom() - 16, cornerAccent, false, false);
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
        int topColor = DefaultTheme.COLORS.panelFillTop();
        int bottomColor = DefaultTheme.COLORS.panelInset();
        int borderColor = DefaultTheme.COLORS.panelBorder();
        int shadowColor = SLColorUtils.multiplyAlpha(DefaultTheme.COLORS.panelShadow(), pressed ? 0.0F : 1.15F);
        
        if (!enabled) {
            topColor = SLColorUtils.lerp(0.45F, DefaultTheme.COLORS.panelFillTop(), DefaultTheme.COLORS.insetFill());
            bottomColor = SLColorUtils.lerp(0.55F, DefaultTheme.COLORS.panelInset(), DefaultTheme.COLORS.insetFill());
            borderColor = SLColorUtils.lerp(0.32F, DefaultTheme.COLORS.panelBorder(), DefaultTheme.COLORS.insetBorder());
        } else if (pressed) {
            topColor = SLColorUtils.lerp(0.34F, DefaultTheme.COLORS.panelFillBottom(), DefaultTheme.COLORS.panelInset());
            bottomColor = SLColorUtils.lerp(0.15F, DefaultTheme.COLORS.panelFillBottom(), DefaultTheme.COLORS.panelInset());
            borderColor = DefaultTheme.COLORS.insetStrong();
        } else if (hovered) {
            topColor = SLColorUtils.lerp(0.18F, DefaultTheme.COLORS.panelFillTop(), DefaultTheme.COLORS.accent());
            bottomColor = SLColorUtils.lerp(0.30F, DefaultTheme.COLORS.panelInset(), DefaultTheme.COLORS.accent());
            borderColor = SLColorUtils.lerp(0.30F, DefaultTheme.COLORS.panelBorder(), DefaultTheme.COLORS.accent());
        }
        
        if (shadowColor != 0) {
            context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, bounds.width(), bounds.height()), shadowColor);
        }
        context.fillVerticalGradient(bounds, topColor, bottomColor);
        context.outline(bounds, borderColor);
        if (bounds.width() > 2 && bounds.height() > 2) {
            context.fill(new SLBounds(bounds.x() + 1, bounds.bottom() - 2, Math.max(0, bounds.width() - 2), 1), 0x24000000);
        }
        if ((hovered || pressed) && enabled && bounds.width() > 6 && bounds.height() > 6) {
            int accentStrip = SLColorUtils.multiplyAlpha(SLColorUtils.lerp(0.35F, borderColor, DefaultTheme.COLORS.accent()), hovered ? 0.70F : 0.32F);
            context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, 1, Math.max(0, bounds.height() - 2)), accentStrip);
            context.fill(new SLBounds(bounds.right() - 2, bounds.y() + 1, 1, Math.max(0, bounds.height() - 2)), accentStrip);
        }
        SLBounds textBounds = pressed ? new SLBounds(bounds.x(), bounds.y() + 1, bounds.width(), bounds.height()) : bounds;
        context.centeredVisualText(text, textBounds, this.labelColor(enabled), false);
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
        context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, bounds.width(), bounds.height()), SLColorUtils.multiplyAlpha(DefaultTheme.COLORS.panelShadow(), 0.9F));
        context.fillVerticalGradient(
                bounds,
                SLColorUtils.lerp(0.10F, DefaultTheme.COLORS.insetFill(), DefaultTheme.COLORS.panelFillTop()),
                DefaultTheme.COLORS.insetFill()
        );
        context.outline(bounds, DefaultTheme.COLORS.insetBorder());
        
        int innerWidth = Math.max(0, bounds.width() - 2);
        int innerHeight = Math.max(0, bounds.height() - 2);
        int fillWidth = Math.round(innerWidth * clampedProgress);
        if (fillWidth > 0 && innerHeight > 0) {
            int fillTop = enabled
                    ? SLColorUtils.lerp(0.26F, DefaultTheme.COLORS.accent(), DefaultTheme.COLORS.white())
                    : SLColorUtils.lerp(0.55F, DefaultTheme.COLORS.accent(), DefaultTheme.COLORS.insetBorder());
            int fillBottom = enabled
                    ? SLColorUtils.lerp(0.40F, DefaultTheme.COLORS.panelFillBottom(), DefaultTheme.COLORS.accent())
                    : SLColorUtils.lerp(0.65F, DefaultTheme.COLORS.accent(), DefaultTheme.COLORS.insetBorder());
            SLBounds fillBounds = new SLBounds(bounds.x() + 1, bounds.y() + 1, fillWidth, innerHeight);
            context.fillVerticalGradient(fillBounds, fillTop, fillBottom);
            for (int stripeX = fillBounds.x() + 6; stripeX < fillBounds.right(); stripeX += 8) {
                context.fill(new SLBounds(stripeX, fillBounds.y(), 1, fillBounds.height()), 0x16000000);
            }
        }
        
        if (overlay != null) context.centeredText(overlay, bounds, this.labelColor(enabled), false);
    }
    
    @Override
    public void renderTextField(SLRenderContext context, SLBounds bounds, SLTextFieldRenderState renderState) {
        int outlineColor = renderState.focused() ? DefaultTheme.COLORS.insetStrong() : DefaultTheme.COLORS.panelBorder();
        context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, bounds.width(), bounds.height()), SLColorUtils.multiplyAlpha(DefaultTheme.COLORS.panelShadow(), 0.9F));
        context.fillVerticalGradient(
                bounds,
                SLColorUtils.lerp(0.22F, DefaultTheme.COLORS.insetFill(), DefaultTheme.COLORS.panelFillTop()),
                DefaultTheme.COLORS.insetFill()
        );
        context.outline(bounds, outlineColor);
        if (bounds.width() > 2 && bounds.height() > 2) {
            context.fill(new SLBounds(bounds.x() + 1, bounds.bottom() - 2, Math.max(0, bounds.width() - 2), 1), 0x33000000);
        }
        
        String renderText = renderState.value().isEmpty() && renderState.placeholder() != null
                ? renderState.placeholder()
                : renderState.value();
        int textColor = renderState.value().isEmpty()
                ? DefaultTheme.COLORS.textMuted()
                : this.labelColor(renderState.enabled());
        int textX = bounds.x() + 4;
        int textY = context.centeredVisualTextY(bounds);
        int visibleWidth = Math.max(0, bounds.width() - 8);
        int totalTextWidth = context.font().width(renderState.value());
        String string = renderState.value().substring(0, renderState.cursor());
        int cursorWidth = context.font().width(string);
        int maxScroll = Math.max(0, totalTextWidth - visibleWidth);
        int scrollOffset = Mth.clamp(cursorWidth - Math.max(0, visibleWidth - 2), 0, maxScroll);
        int drawTextX = textX - scrollOffset;
        
        context.pushClip(new SLBounds(bounds.x() + 2, bounds.y() + 2, Math.max(0, bounds.width() - 4), Math.max(0, bounds.height() - 4)));
        try {
            if (renderState.hasSelection() && !renderState.value().isEmpty()) {
                int selectionStart = Math.min(renderState.selectionStart(), renderState.selectionEnd());
                int selectionEnd = Math.max(renderState.selectionStart(), renderState.selectionEnd());
                int highlightX = drawTextX + context.font().width(renderState.value().substring(0, selectionStart));
                int highlightRight = drawTextX + context.font().width(renderState.value().substring(0, selectionEnd));
                context.graphics().fill(highlightX, textY - 1, highlightRight, textY + context.font().lineHeight + 1, DefaultTheme.COLORS.selection());
            }
            
            context.graphics().text(context.font(), renderText, drawTextX, textY, textColor, false);
            
            if (renderState.focused() && renderState.cursorVisible()) {
                int cursorX = drawTextX + context.font().width(string);
                context.graphics().fill(cursorX, textY, cursorX + 1, textY + context.font().lineHeight, DefaultTheme.COLORS.white());
            }
        } finally {
            context.popClip();
        }
    }
    
    @Override
    public void renderSlotFrame(
            SLRenderContext context,
            SLBounds bounds,
            SLSlotChrome chrome,
            boolean hovered,
            boolean active
    ) {
        if (!chrome.drawFrame()) return;
        
        float inactiveBlend = active ? 0.0F : 0.45F;
        int fillColor = inactiveBlend > 0.0F
                ? SLColorUtils.lerp(inactiveBlend, chrome.fillColor(), DefaultTheme.COLORS.insetFill())
                : chrome.fillColor();
        int borderColor = inactiveBlend > 0.0F
                ? SLColorUtils.lerp(inactiveBlend, chrome.borderColor(), DefaultTheme.COLORS.insetBorder())
                : chrome.borderColor();
        
        if (hovered) {
            fillColor = SLColorUtils.lerp(0.18F, fillColor, DefaultTheme.COLORS.accent());
            borderColor = SLColorUtils.lerp(0.22F, borderColor, DefaultTheme.COLORS.accent());
        }
        
        context.fill(new SLBounds(bounds.x() + 1, bounds.y() + 1, bounds.width(), bounds.height()), SLColorUtils.multiplyAlpha(DefaultTheme.COLORS.panelShadow(), 0.8F));
        context.fillVerticalGradient(
                bounds,
                SLColorUtils.lerp(0.15F, fillColor, DefaultTheme.COLORS.panelFillTop()),
                SLColorUtils.lerp(0.20F, fillColor, DefaultTheme.COLORS.panelFillBottom())
        );
        context.outline(bounds, borderColor);
        if (bounds.width() > 2 && bounds.height() > 2) {
            context.fill(new SLBounds(bounds.x() + 1, bounds.bottom() - 2, Math.max(0, bounds.width() - 2), 1), 0x22000000);
        }
    }
    
    @Override
    public int labelColor(boolean enabled) {
        return enabled ? DefaultTheme.COLORS.textPrimary() : DefaultTheme.COLORS.textDisabled();
    }
    
    @Override
    public int accentColor() {
        return DefaultTheme.COLORS.accent();
    }
    
    ///
    /// Renders one subtle L-shaped accent used in panel corners.
    ///
    /// @param context     render context
    /// @param x           anchor x
    /// @param y           anchor y
    /// @param color       accent color
    /// @param leftAligned whether the accent hugs the left edge
    /// @param topAligned  whether the accent hugs the top edge
    ///
    private void renderCornerAccent(
            SLRenderContext context,
            int x,
            int y,
            int color,
            boolean leftAligned,
            boolean topAligned
    ) {
        int horizontalX = leftAligned ? x : x + 6;
        int verticalX = leftAligned ? x : x + 15;
        int horizontalY = topAligned ? y : y + 9;
        int verticalY = topAligned ? y : y + 2;
        
        context.fill(new SLBounds(horizontalX, horizontalY, 16 - 6, 1), color);
        context.fill(new SLBounds(verticalX, verticalY, 1, 8), color);
    }
}
