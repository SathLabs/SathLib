package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.RadialScreen;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.util.SLMathUtils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

///
/// Custom radial action component used by {@link RadialScreen}.
///
/// The node owns a list of entries, computes orbital button positions during
/// rendering and hit testing, and invokes entry callbacks on activation.
///
/// - arrange entries around a center point
/// - render each entry as a skinned action chip
/// - handle pointer interaction without involving screen-level input code
///
/// Override {@link Entry#render(SLRenderContext, SLBounds, boolean, boolean, boolean)}
/// for custom radial entry visuals while keeping the same layout and input
/// behavior.
///
public class RadialMenuNode extends UILeafNode<RadialMenuNode> {
    
    private final List<Entry> entries = new ArrayList<>();
    
    private float startAngleDegrees = -90.0F;
    
    private int ringRadius = 92;
    private int buttonWidth = 76;
    private int buttonHeight = 22;
    private int hoverOffset = 8;
    
    private int hoveredIndex = -1;
    private int pressedIndex = -1;
    
    ///
    /// Returns an immutable entry view.
    ///
    /// @return immutable entry list
    ///
    public List<Entry> entries() {
        return Collections.unmodifiableList(this.entries);
    }
    
    ///
    /// Adds a radial entry.
    ///
    /// @param entry entry to add
    ///
    /// @return added entry
    ///
    public Entry addEntry(Entry entry) {
        this.entries.add(entry);
        this.invalidateLayout();
        return entry;
    }
    
    ///
    /// Sets the angle used for the first radial entry.
    ///
    /// @param startAngleDegrees new starting angle in degrees
    ///
    public void setStartAngleDegrees(float startAngleDegrees) {
        this.startAngleDegrees = startAngleDegrees;
        this.invalidateLayout();
    }
    
    ///
    /// Removes all radial entries.
    ///
    public void clearEntries() {
        this.entries.clear();
        this.hoveredIndex = -1;
        this.pressedIndex = -1;
        this.invalidateLayout();
    }
    
    ///
    /// Sets the orbit radius used for entry placement.
    ///
    /// @param ringRadius orbit radius in pixels
    ///
    public void setRingRadius(int ringRadius) {
        this.ringRadius = Math.max(8, ringRadius);
        this.invalidateLayout();
    }
    
    ///
    /// Sets the entry button size.
    ///
    /// @param width  button width
    /// @param height button height
    ///
    public void setButtonSize(int width, int height) {
        this.buttonWidth = Math.max(10, width);
        this.buttonHeight = Math.max(10, height);
        this.invalidateLayout();
    }
    
    ///
    /// Sets how far hovered entries move outward.
    ///
    /// @param hoverOffset hover offset in pixels
    ///
    public void setHoverOffset(int hoverOffset) {
        this.hoverOffset = Math.max(0, hoverOffset);
        this.invalidateLayout();
    }
    
    @Override
    protected boolean isInputTarget() {
        return true;
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        int diameter = (this.ringRadius * 2) + Math.max(this.buttonWidth, this.buttonHeight) + (this.hoverOffset * 2);
        return new SLMeasuredSize(diameter, diameter);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        int centerX = bounds.x() + (bounds.width() / 2);
        int centerY = bounds.y() + (bounds.height() / 2);
        
        context.graphics().fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xCCECF2F8);
        
        for (int entryIndex = 0; entryIndex < this.entries.size(); entryIndex++) {
            Entry entry = this.entries.get(entryIndex);
            SLBounds entryBounds = this.entryBounds(entryIndex);
            entry.render(
                    context,
                    entryBounds,
                    entryIndex == this.hoveredIndex,
                    entryIndex == this.pressedIndex,
                    this.isEnabled()
            );
        }
    }
    
    @Override
    public boolean mouseMoved(double mouseX, double mouseY) {
        this.hoveredIndex = this.entryIndexAt(mouseX, mouseY);
        return this.hoveredIndex >= 0;
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isEnabled() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        
        this.hoveredIndex = this.entryIndexAt(event.x(), event.y());
        if (this.hoveredIndex < 0) return false;
        
        this.pressedIndex = this.hoveredIndex;
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean hadPress = this.pressedIndex >= 0;
        int releasedIndex = this.entryIndexAt(event.x(), event.y());
        
        if (hadPress && releasedIndex == this.pressedIndex) {
            this.entries.get(this.pressedIndex).activate();
        }
        
        this.pressedIndex = -1;
        this.setPressedState(false);
        this.hoveredIndex = releasedIndex;
        return hadPress;
    }
    
    private int entryIndexAt(double mouseX, double mouseY) {
        if (!this.getBounds().contains(mouseX, mouseY)) {
            return -1;
        }
        
        for (int entryIndex = this.entries.size() - 1; entryIndex >= 0; entryIndex--) {
            if (this.entryBounds(entryIndex).contains(mouseX, mouseY)) {
                return entryIndex;
            }
        }
        
        return -1;
    }
    
    private SLBounds entryBounds(int entryIndex) {
        int entryCount = Math.max(1, this.entries.size());
        float angle = this.startAngleDegrees + ((360.0F / entryCount) * entryIndex);
        int resolvedRadius = this.ringRadius + (entryIndex == this.hoveredIndex ? this.hoverOffset : 0);
        
        SLBounds bounds = this.getBounds();
        int centerX = bounds.x() + (bounds.width() / 2);
        int centerY = bounds.y() + (bounds.height() / 2);
        
        int entryX = Math.round(centerX + (SLMathUtils.cos(angle) * resolvedRadius) - (this.buttonWidth / 2.0F));
        int entryY = Math.round(centerY + (SLMathUtils.sin(angle) * resolvedRadius) - (this.buttonHeight / 2.0F));
        return new SLBounds(entryX, entryY, this.buttonWidth, this.buttonHeight);
    }
    
    ///
    /// Radial entry model with overridable rendering and activation hooks.
    ///
    /// Entries are added to one radial menu node and reused for as long as that
    /// node lives.
    ///
    /// - expose a label for the default radial rendering
    /// - react when the user activates the entry
    ///
    /// Override {@link #render(SLRenderContext, SLBounds, boolean, boolean, boolean)}
    /// to customize visuals or override {@link #activate()} for different action
    /// behavior.
    ///
    public static class Entry {
        
        private final Component label;
        private final Runnable action;
        
        public Entry(Component label, Runnable action) {
            this.label = label;
            this.action = action;
        }
        
        ///
        /// Returns the entry label.
        ///
        /// @return entry label
        ///
        public Component label() {
            return this.label;
        }
        
        ///
        /// Renders the entry.
        ///
        /// @param context render context
        /// @param bounds  entry bounds
        /// @param hovered whether the entry is hovered
        /// @param pressed whether the entry is pressed
        /// @param enabled whether the parent node is enabled
        ///
        public void render(SLRenderContext context, SLBounds bounds, boolean hovered, boolean pressed, boolean enabled) {
            context.skin().renderButton(context, bounds, this.label, hovered, pressed, enabled);
        }
        
        ///
        /// Activates the entry.
        ///
        public void activate() {
            this.action.run();
        }
    }
}
