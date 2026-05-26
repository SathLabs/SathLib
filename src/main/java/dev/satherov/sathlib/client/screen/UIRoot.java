package dev.satherov.sathlib.client.screen;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.style.DefaultTheme;
import dev.satherov.sathlib.client.screen.style.UITheme;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import org.jspecify.annotations.Nullable;

///
/// Root owner for one retained-mode UI tree.
///
/// A screen creates one root during initialization, assigns a viewport and a
/// node tree, then reuses that root for rendering and input until the screen is
/// closed.
///
public final class UIRoot {
    
    private @Nullable UINode<?> content;
    private SLBounds viewport = SLBounds.EMPTY;
    private UITheme theme = DefaultTheme.INSTANCE;
    private boolean layoutDirty = true;
    
    private @Nullable UINode<?> hoveredNode;
    private @Nullable UINode<?> pressedNode;
    private @Nullable UINode<?> focusedNode;
    
    ///
    /// Creates an empty UI root.
    ///
    public UIRoot() { }
    
    ///
    /// Returns the current root node.
    ///
    /// @return current root node, or {@code null} when unset
    ///
    public @Nullable UINode<?> getContent() {
        return this.content;
    }
    
    ///
    /// Replaces the root node tree owned by this UI root.
    ///
    /// @param content new root node, or {@code null} to clear the tree
    ///
    public void setContent(@Nullable UINode<?> content) {
        this.hoveredNode = null;
        this.pressedNode = null;
        this.focusedNode = null;
        
        if (this.content != null) {
            this.content.detach();
        }
        
        this.content = content;
        if (this.content != null) {
            this.content.attach(this, null);
        }
        this.invalidateLayout();
    }
    
    ///
    /// Returns the current viewport used for layout.
    ///
    /// @return layout viewport
    ///
    public SLBounds getViewport() {
        return this.viewport;
    }
    
    ///
    /// Updates the viewport used for measure and layout.
    ///
    /// @param viewport new root viewport
    ///
    public void setViewport(SLBounds viewport) {
        if (this.viewport.equals(viewport)) {
            return;
        }
        this.viewport = viewport;
        this.invalidateLayout();
    }
    
    ///
    /// Returns the active theme.
    ///
    /// @return active UI theme
    ///
    public UITheme getTheme() {
        return this.theme;
    }
    
    ///
    /// Updates the theme used by built-in widgets.
    ///
    /// @param theme new active theme
    ///
    public void setTheme(UITheme theme) {
        this.theme = theme;
    }
    
    ///
    /// Returns the legacy theme accessor kept for compatibility.
    ///
    /// @return active UI theme
    ///
    @Deprecated(forRemoval = false)
    public UITheme getSkin() {
        return this.theme;
    }
    
    ///
    /// Updates the legacy theme setter kept for compatibility.
    ///
    /// @param theme new active theme
    ///
    @Deprecated(forRemoval = false)
    public void setSkin(UITheme theme) {
        this.setTheme(theme);
    }
    
    ///
    /// Marks the UI tree as needing a fresh measure and layout pass.
    ///
    public void invalidateLayout() {
        this.layoutDirty = true;
    }
    
    ///
    /// Requests focus for a specific node.
    ///
    /// @param node target node, or {@code null} to clear focus
    ///
    public void requestFocus(@Nullable UINode<?> node) {
        if (this.focusedNode == node) {
            return;
        }
        
        if (this.focusedNode != null) {
            this.focusedNode.setFocusedState(false);
        }
        
        this.focusedNode = node != null && node.isFocusable() ? node : null;
        
        if (this.focusedNode != null) {
            this.focusedNode.setFocusedState(true);
        }
    }
    
    ///
    /// Performs the render pass for the retained-mode tree.
    ///
    /// @param graphics    graphics extractor
    /// @param font        active font
    /// @param mouseX      current mouse x
    /// @param mouseY      current mouse y
    /// @param partialTick partial tick
    ///
    public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float partialTick) {
        this.resolveLayout(font);
        if (this.content == null) {
            return;
        }
        
        SLRenderContext renderContext = new SLRenderContext(graphics, font, this.theme, partialTick, mouseX, mouseY);
        this.content.renderTree(renderContext);
    }
    
    ///
    /// Ticks the node tree once.
    ///
    public void tick() {
        if (this.content == null) {
            return;
        }
        this.content.tickTree();
    }
    
    ///
    /// Routes mouse movement and hover updates into the node tree.
    ///
    /// @param font   active font used if layout must be resolved
    /// @param mouseX mouse x position
    /// @param mouseY mouse y position
    ///
    public void mouseMoved(Font font, double mouseX, double mouseY) {
        this.resolveLayout(font);
        this.updateHoveredNode(this.findHitNode(mouseX, mouseY));
        if (this.hoveredNode != null) {
            this.hoveredNode.mouseMoved(mouseX, mouseY);
        }
    }
    
    ///
    /// Routes mouse press handling into the node tree.
    ///
    /// @param font        active font used if layout must be resolved
    /// @param event       mouse button event
    /// @param doubleClick whether the click is a double click
    ///
    /// @return {@code true} when a node handled the event
    ///
    public boolean mouseClicked(Font font, MouseButtonEvent event, boolean doubleClick) {
        this.resolveLayout(font);
        UINode<?> target = this.findHitNode(event.x(), event.y());
        this.updateHoveredNode(target);
        
        if (target == null) {
            this.requestFocus(null);
            return false;
        }
        
        if (target.mousePressed(event, doubleClick)) {
            this.pressedNode = target;
            this.requestFocus(target);
            return true;
        }
        
        return false;
    }
    
    ///
    /// Routes mouse drag handling to the currently pressed node.
    ///
    /// @param font   active font used if layout must be resolved
    /// @param event  mouse button event
    /// @param deltaX drag delta x
    /// @param deltaY drag delta y
    ///
    /// @return {@code true} when handled
    ///
    public boolean mouseDragged(Font font, MouseButtonEvent event, double deltaX, double deltaY) {
        this.resolveLayout(font);
        if (this.pressedNode == null) {
            return false;
        }
        return this.pressedNode.mouseDragged(event, deltaX, deltaY);
    }
    
    ///
    /// Routes mouse release handling to the currently pressed node.
    ///
    /// @param font  active font used if layout must be resolved
    /// @param event mouse button event
    ///
    /// @return {@code true} when handled
    ///
    public boolean mouseReleased(Font font, MouseButtonEvent event) {
        this.resolveLayout(font);
        if (this.pressedNode == null) {
            return false;
        }
        
        UINode<?> releasedNode = this.pressedNode;
        this.pressedNode = null;
        boolean handled = releasedNode.mouseReleased(event);
        this.updateHoveredNode(this.findHitNode(event.x(), event.y()));
        return handled;
    }
    
    ///
    /// Routes scroll input to the node under the pointer.
    ///
    /// @param font    active font used if layout must be resolved
    /// @param mouseX  pointer x position
    /// @param mouseY  pointer y position
    /// @param scrollX horizontal scroll
    /// @param scrollY vertical scroll
    ///
    /// @return {@code true} when handled
    ///
    public boolean mouseScrolled(Font font, double mouseX, double mouseY, double scrollX, double scrollY) {
        this.resolveLayout(font);
        UINode<?> target = this.findHitNode(mouseX, mouseY);
        if (target == null) {
            return false;
        }
        return target.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    ///
    /// Routes key press handling to the focused node.
    ///
    /// @param event key event
    ///
    /// @return {@code true} when handled
    ///
    public boolean keyPressed(KeyEvent event) {
        if (this.focusedNode == null) {
            return false;
        }
        return this.focusedNode.keyPressed(event);
    }
    
    ///
    /// Routes key release handling to the focused node.
    ///
    /// @param event key event
    ///
    /// @return {@code true} when handled
    ///
    public boolean keyReleased(KeyEvent event) {
        if (this.focusedNode == null) {
            return false;
        }
        return this.focusedNode.keyReleased(event);
    }
    
    ///
    /// Routes character input to the focused node.
    ///
    /// @param event character event
    ///
    /// @return {@code true} when handled
    ///
    public boolean charTyped(CharacterEvent event) {
        if (this.focusedNode == null) {
            return false;
        }
        return this.focusedNode.charTyped(event);
    }
    
    ///
    /// Resolves the layout tree when it is currently invalidated.
    ///
    /// @param font active font
    ///
    public void resolveLayout(Font font) {
        if (!this.layoutDirty || this.content == null) {
            return;
        }
        
        this.content.measure(font, this.viewport.width(), this.viewport.height());
        this.content.layout(this.viewport, font);
        this.layoutDirty = false;
    }
    
    ///
    /// Updates the hovered node bookkeeping.
    ///
    /// @param nextHoveredNode newly hovered node, or {@code null}
    ///
    private void updateHoveredNode(@Nullable UINode<?> nextHoveredNode) {
        if (this.hoveredNode == nextHoveredNode) {
            return;
        }
        
        if (this.hoveredNode != null) {
            this.hoveredNode.setHoveredState(false);
        }
        
        this.hoveredNode = nextHoveredNode;
        
        if (this.hoveredNode != null) {
            this.hoveredNode.setHoveredState(true);
        }
    }
    
    ///
    /// Finds the deepest input target at the given coordinates.
    ///
    /// @param mouseX pointer x position
    /// @param mouseY pointer y position
    ///
    /// @return deepest input target, or {@code null}
    ///
    private @Nullable UINode<?> findHitNode(double mouseX, double mouseY) {
        if (this.content == null) {
            return null;
        }
        return this.content.hitTest(mouseX, mouseY);
    }
}
