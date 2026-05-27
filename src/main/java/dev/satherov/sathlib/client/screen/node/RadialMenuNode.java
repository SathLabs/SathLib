package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;
import lombok.Singular;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.util.SLMathUtils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

///
/// Custom radial action component used by radial-style screens.
///
/// The node owns a list of entries, computes orbital button positions during
/// rendering and hit testing, and invokes entry callbacks on activation.
///
public class RadialMenuNode extends UILeafNode<RadialMenuNode> {
    
    private final List<Entry> entries = new ArrayList<>();
    
    private float startAngleDegrees;
    
    private int ringRadius;
    private int buttonWidth;
    private int buttonHeight;
    private int hoverOffset;
    
    private int hoveredIndex = -1;
    private int pressedIndex = -1;
    
    ///
    /// Creates an empty radial menu node.
    ///
    public RadialMenuNode() {
        this(SLModifier.none(), List.of(), -90.0F, 92, 76, 22, 8);
    }
    
    ///
    /// Creates a fully configured radial menu.
    ///
    /// @param modifier          node modifier
    /// @param entries           initial entry list
    /// @param startAngleDegrees starting angle for the first entry
    /// @param ringRadius        orbit radius in pixels
    /// @param buttonWidth       entry width in pixels
    /// @param buttonHeight      entry height in pixels
    /// @param hoverOffset       outward hover offset in pixels
    ///
    protected RadialMenuNode(
            SLModifier modifier,
            List<Entry> entries,
            float startAngleDegrees,
            int ringRadius,
            int buttonWidth,
            int buttonHeight,
            int hoverOffset
    ) {
        super(modifier);
        this.entries.addAll(List.copyOf(entries));
        this.startAngleDegrees = startAngleDegrees;
        this.ringRadius = Math.max(8, ringRadius);
        this.buttonWidth = Math.max(10, buttonWidth);
        this.buttonHeight = Math.max(10, buttonHeight);
        this.hoverOffset = Math.max(0, hoverOffset);
    }
    
    ///
    /// Creates a builder-backed radial menu while normalizing omitted values to
    /// the framework defaults.
    ///
    /// @param modifier          node modifier
    /// @param entries           initial entry list
    /// @param startAngleDegrees starting angle for the first entry
    /// @param ringRadius        orbit radius in pixels
    /// @param buttonWidth       entry width in pixels
    /// @param buttonHeight      entry height in pixels
    /// @param hoverOffset       outward hover offset in pixels
    ///
    /// @return configured radial-menu node
    ///
    @Builder
    public static RadialMenuNode of(
            SLModifier modifier,
            @Singular("entry") List<Entry> entries,
            Float startAngleDegrees,
            Integer ringRadius,
            Integer buttonWidth,
            Integer buttonHeight,
            Integer hoverOffset
    ) {
        return new RadialMenuNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(entries, List.of()),
                Objects.requireNonNullElse(startAngleDegrees, -90.0F),
                Objects.requireNonNullElse(ringRadius, 92),
                Objects.requireNonNullElse(buttonWidth, 76),
                Objects.requireNonNullElse(buttonHeight, 22),
                Objects.requireNonNullElse(hoverOffset, 8)
        );
    }
    
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
        Entry normalizedEntry = Objects.requireNonNull(entry);
        this.entries.add(normalizedEntry);
        this.invalidateLayout();
        return normalizedEntry;
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
        
        context.graphics().fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, context.theme().colors().textPrimary());
        
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
        if (!this.isEnabled() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        
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
    
    ///
    /// Returns the entry index under the cursor or {@code -1} when none is hit.
    ///
    /// @param mouseX pointer x position
    /// @param mouseY pointer y position
    ///
    /// @return hit entry index, or {@code -1}
    ///
    private int entryIndexAt(double mouseX, double mouseY) {
        if (!this.getBounds().contains(mouseX, mouseY)) return -1;
        
        for (int entryIndex = this.entries.size() - 1; entryIndex >= 0; entryIndex--) {
            if (this.entryBounds(entryIndex).contains(mouseX, mouseY)) {
                return entryIndex;
            }
        }
        
        return -1;
    }
    
    ///
    /// Resolves the bounds for one orbital entry.
    ///
    /// @param entryIndex entry index
    ///
    /// @return resolved entry bounds
    ///
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
    public static class Entry {
        
        private final Component label;
        private final Runnable action;
        
        ///
        /// Creates a radial entry with a label and activation callback.
        ///
        /// @param label  entry label
        /// @param action callback invoked when the entry is activated
        ///
        public Entry(Component label, Runnable action) {
            this.label = Objects.requireNonNull(label);
            this.action = Objects.requireNonNull(action);
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
            context.theme().renderButton(context, bounds, this.label, hovered, pressed, enabled);
        }
        
        ///
        /// Activates the entry.
        ///
        public void activate() {
            this.action.run();
        }
    }
}
