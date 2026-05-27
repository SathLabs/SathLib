package dev.satherov.sathlib.client.screen;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.node.SLTooltipProvider;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.style.DefaultTheme;
import dev.satherov.sathlib.client.screen.style.UITheme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

///
/// Base client-only screen for retained-mode SathLib UI trees.
///
/// Subclasses rebuild their node tree in {@link #init()}, then reuse the same
/// {@link UIRoot} for layout, rendering, and input until the screen closes or
/// resizes.
///
public abstract class SLScreen extends Screen {
    
    private final UIRoot root = new UIRoot();
    
    ///
    /// Creates a retained-mode screen with the given title.
    ///
    /// @param title screen title
    ///
    protected SLScreen(Component title) {
        super(title);
    }
    
    ///
    /// Returns the owned UI root for advanced custom screens.
    ///
    /// @return retained-mode UI root
    ///
    protected final UIRoot root() {
        return this.root;
    }
    
    ///
    /// Returns the active top-level theme for this screen.
    ///
    /// @return active theme
    ///
    protected final UITheme theme() {
        return this.root.getTheme();
    }
    
    ///
    /// Builds the runtime node tree for this screen.
    ///
    /// @return new root node
    ///
    protected abstract UINode<?> create();
    
    ///
    /// Returns the viewport used to lay out the UI tree.
    ///
    /// @return UI layout viewport
    ///
    protected SLBounds createViewport() {
        return new SLBounds(0, 0, this.width, this.height);
    }
    
    ///
    /// Returns the theme used by built-in widgets.
    ///
    /// @return active theme
    ///
    protected UITheme createTheme() {
        return DefaultTheme.INSTANCE;
    }
    
    ///
    /// Returns the legacy theme factory kept for compatibility with older
    /// subclasses.
    ///
    /// @return active theme
    ///
    @Deprecated(forRemoval = false)
    protected UITheme createSkin() {
        return this.createTheme();
    }
    
    @Override
    protected void init() {
        this.rebuild();
    }
    
    @Override
    public void tick() {
        this.root.tick();
    }
    
    @Override
    public void removed() {
        this.root.setContent(null);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.root.setViewport(this.createViewport());
        this.root.render(graphics, this.font, mouseX, mouseY, partialTick);
        this.extractTooltip(graphics, mouseX, mouseY);
    }
    
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        this.root.mouseMoved(this.font, mouseX, mouseY);
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.root.mouseClicked(this.font, event, doubleClick)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.root.mouseDragged(this.font, event, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.root.mouseReleased(this.font, event)) {
            return true;
        }
        return super.mouseReleased(event);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.root.mouseScrolled(this.font, mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.root.keyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }
    
    @Override
    public boolean keyReleased(KeyEvent event) {
        if (this.root.keyReleased(event)) {
            return true;
        }
        return super.keyReleased(event);
    }
    
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.root.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }
    
    ///
    /// Rebuilds the retained tree and reapplies the top-level theme.
    ///
    private void rebuild() {
        this.root.setTheme(this.createTheme());
        this.root.setViewport(this.createViewport());
        this.root.setContent(this.create());
    }
    
    ///
    /// Extracts the tooltip for the currently hovered retained node, if any.
    ///
    private void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        UINode<?> hoveredNode = this.root.getHoveredNode();
        if (!(hoveredNode instanceof SLTooltipProvider tooltipProvider)) {
            return;
        }
        
        List<Component> tooltipLines = tooltipProvider.getTooltipLines();
        if (tooltipLines == null || tooltipLines.isEmpty()) {
            return;
        }
        
        graphics.setTooltipForNextFrame(this.font, tooltipLines, null, mouseX, mouseY, null);
    }
}
