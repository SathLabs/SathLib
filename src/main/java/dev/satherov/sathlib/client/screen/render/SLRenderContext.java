package dev.satherov.sathlib.client.screen.render;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.style.UITheme;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

///
/// Rendering facade used by SathLib UI nodes.
///
/// A new context is created for each UI render pass and discarded once drawing
/// completes.
///
public final class SLRenderContext {
    
    private final GuiGraphicsExtractor graphics;
    private final Font font;
    private final UITheme theme;
    private final float partialTick;
    private final int mouseX;
    private final int mouseY;
    private final SLClipStack clipStack;
    
    ///
    /// Creates a render context for one UI pass.
    ///
    /// @param graphics    graphics extractor
    /// @param font        active font
    /// @param theme       active UI theme
    /// @param partialTick partial tick value
    /// @param mouseX      current mouse x
    /// @param mouseY      current mouse y
    ///
    public SLRenderContext(
            GuiGraphicsExtractor graphics,
            Font font,
            UITheme theme,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        this.graphics = graphics;
        this.font = font;
        this.theme = theme;
        this.partialTick = partialTick;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.clipStack = new SLClipStack(graphics);
    }
    
    ///
    /// Exposes the raw graphics extractor for specialized custom widgets.
    ///
    /// @return active graphics extractor
    ///
    public GuiGraphicsExtractor graphics() {
        return this.graphics;
    }
    
    ///
    /// Exposes the active font.
    ///
    /// @return active font
    ///
    public Font font() {
        return this.font;
    }
    
    ///
    /// Exposes the active theme.
    ///
    /// @return active theme
    ///
    public UITheme theme() {
        return this.theme;
    }
    
    ///
    /// Returns the current partial tick.
    ///
    /// @return partial tick
    ///
    public float partialTick() {
        return this.partialTick;
    }
    
    ///
    /// Returns the current mouse x position.
    ///
    /// @return mouse x
    ///
    public int mouseX() {
        return this.mouseX;
    }
    
    ///
    /// Returns the current mouse y position.
    ///
    /// @return mouse y
    ///
    public int mouseY() {
        return this.mouseY;
    }
    
    ///
    /// Fills a bounds rectangle with a solid color.
    ///
    /// @param bounds target bounds
    /// @param color  fill color
    ///
    public void fill(SLBounds bounds, int color) {
        this.graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), color);
    }
    
    ///
    /// Draws a vertical gradient across the given bounds.
    ///
    /// @param bounds     target bounds
    /// @param startColor top color
    /// @param endColor   bottom color
    ///
    public void fillVerticalGradient(SLBounds bounds, int startColor, int endColor) {
        this.graphics.fillGradient(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), startColor, endColor);
    }
    
    ///
    /// Draws a one-pixel outline around a bounds rectangle.
    ///
    /// @param bounds target bounds
    /// @param color  outline color
    ///
    public void outline(SLBounds bounds, int color) {
        this.graphics.outline(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color);
    }
    
    ///
    /// Draws a fixed-thickness outline around a bounds rectangle.
    ///
    /// @param bounds    target bounds
    /// @param color     outline color
    /// @param thickness outline thickness in pixels
    ///
    public void outline(SLBounds bounds, int color, int thickness) {
        if (thickness <= 0 || bounds.width() <= 0 || bounds.height() <= 0) return;
        this.graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.y() + thickness, color);
        this.graphics.fill(bounds.x(), bounds.bottom() - thickness, bounds.right(), bounds.bottom(), color);
        this.graphics.fill(bounds.x(), bounds.y(), bounds.x() + thickness, bounds.bottom(), color);
        this.graphics.fill(bounds.right() - thickness, bounds.y(), bounds.right(), bounds.bottom(), color);
    }
    
    ///
    /// Draws left-aligned text.
    ///
    /// @param text   text to draw
    /// @param x      target x
    /// @param y      target y
    /// @param color  text color
    /// @param shadow whether to render a shadow
    ///
    public void text(Component text, int x, int y, int color, boolean shadow) {
        this.graphics.text(this.font, text, x, y, color, shadow);
    }
    
    ///
    /// Draws centered text inside bounds.
    ///
    /// @param text   text to draw
    /// @param bounds target bounds
    /// @param color  text color
    /// @param shadow whether to render a shadow
    ///
    public void centeredText(Component text, SLBounds bounds, int color, boolean shadow) {
        int textWidth = this.font.width(text);
        int textX = bounds.x() + ((bounds.width() - textWidth) / 2);
        int textY = bounds.y() + ((bounds.height() - this.font.lineHeight) / 2);
        this.text(text, textX, textY, color, shadow);
    }
    
    ///
    /// Draws centered text inside the bounds with the same visual alignment used
    /// by vanilla-like buttons.
    ///
    /// @param text   text to draw
    /// @param bounds target bounds
    /// @param color  text color
    /// @param shadow whether to render a shadow
    ///
    public void centeredVisualText(Component text, SLBounds bounds, int color, boolean shadow) {
        int textWidth = this.font.width(text);
        int textX = bounds.x() + ((bounds.width() - textWidth) / 2);
        int textY = this.centeredVisualTextY(bounds);
        this.text(text, textX, textY, color, shadow);
    }
    
    ///
    /// Calculates the vertical center position for text within the given bounds.
    ///
    /// @param bounds target bounds
    ///
    /// @return the calculated y position
    ///
    public int centeredVisualTextY(SLBounds bounds) {
        int lineHeight = this.font.lineHeight + 3;
        if (bounds.height() < lineHeight) {
            return bounds.y() + Math.max(0, (bounds.height() - lineHeight) / 2);
        }
        
        int slack = bounds.height() - lineHeight;
        return bounds.y() + Math.floorDiv(slack + 1, 2) + 2;
    }
    
    ///
    /// Draws a sprite into the given rectangle.
    ///
    /// @param sprite sprite identifier
    /// @param x      left coordinate
    /// @param y      top coordinate
    /// @param width  target width
    /// @param height target height
    ///
    public void blitSprite(Identifier sprite, int x, int y, int width, int height) {
        this.graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }
    
    
    ///
    /// Renders a set amount of a sprite into the given rectangle.
    ///
    /// @param sprite the atlas sprite to render
    /// @param x      the x-offset
    /// @param y      the y-offset
    /// @param width  the width of the rectangle
    /// @param height the height of the rectangle
    /// @param scale  the vertical amount to render
    /// @param color  the color to tint the sprite with
    ///
    public void blitVerticalSprite(TextureAtlasSprite sprite, int x, int y, int width, int height, int scale, int color) {
        SpriteContents spriteContents = sprite.contents();
        GuiSpriteScaling.Tile scaling = new GuiSpriteScaling.Tile(spriteContents.width(), spriteContents.height());
        y = y + height - scale;
        
        this.graphics.enableScissor(x, y, x + width, y + scale);
        {
            this.graphics.blitTiledSprite(
                    RenderPipelines.GUI_TEXTURED,
                    sprite,
                    x,
                    y,
                    width,
                    scale,
                    0,
                    0,
                    scaling.width(),
                    scaling.height(),
                    scaling.width(),
                    scaling.height(),
                    color
            );
        }
        this.graphics.disableScissor();
    }
    
    ///
    /// Draws a normal item stack.
    ///
    /// @param stack stack to draw
    /// @param x     left coordinate
    /// @param y     top coordinate
    /// @param seed  render seed
    ///
    public void item(ItemStack stack, int x, int y, int seed) {
        this.graphics.item(stack, x, y, seed);
    }
    
    ///
    /// Draws a fake item stack.
    ///
    /// @param stack stack to draw
    /// @param x     left coordinate
    /// @param y     top coordinate
    /// @param seed  render seed
    ///
    public void fakeItem(ItemStack stack, int x, int y, int seed) {
        this.graphics.fakeItem(stack, x, y, seed);
    }
    
    ///
    /// Draws item decorations for the given stack.
    ///
    /// @param stack     stack whose decorations should be drawn
    /// @param x         left coordinate
    /// @param y         top coordinate
    /// @param itemCount optional count text override
    ///
    public void itemDecorations(ItemStack stack, int x, int y, @Nullable String itemCount) {
        this.graphics.itemDecorations(this.font, stack, x, y, itemCount);
    }
    
    ///
    /// Moves subsequent draw calls to the next stratum.
    ///
    public void nextStratum() {
        this.graphics.nextStratum();
    }
    
    ///
    /// Pushes a clip rectangle for subsequent drawing.
    ///
    /// @param bounds clip bounds
    ///
    public void pushClip(SLBounds bounds) {
        this.clipStack.push(bounds);
    }
    
    ///
    /// Pops the most recently pushed clip rectangle.
    ///
    public void popClip() {
        this.clipStack.pop();
    }
}
