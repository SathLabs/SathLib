package dev.satherov.sathlib.client.screen;

import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.node.RadialMenuNode;
import dev.satherov.sathlib.client.screen.node.UINode;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

///
/// Convenience screen that centers a reusable radial action menu.
///
/// The screen owns one {@link RadialMenuNode} for its full lifetime and wraps
/// it in a simple centered stack layout.
///
/// - provide a ready-to-use radial menu screen shell
/// - expose the backing radial menu for configuration and entry management
///
/// Subclasses can add entries, change radial settings, or override the
/// background rendering while reusing the same centered layout.
///
public class RadialScreen extends SLScreen {
    
    private final RadialMenuNode radialMenu = new RadialMenuNode();
    
    public RadialScreen() {
        super(Component.empty());
        this.radialMenu.align(SLAlignment.CENTER, SLAlignment.CENTER);
    }
    
    ///
    /// Returns the backing radial menu node.
    ///
    /// @return radial menu node
    ///
    protected final RadialMenuNode menu() {
        return this.radialMenu;
    }
    
    ///
    /// Adds an entry to the radial menu.
    ///
    /// @param entry radial entry
    ///
    /// @return added entry
    ///
    public RadialMenuNode.Entry addEntry(RadialMenuNode.Entry entry) {
        return this.radialMenu.addEntry(entry);
    }
    
    ///
    /// Removes every radial entry.
    ///
    public void clearEntries() {
        this.radialMenu.clearEntries();
    }
    
    ///
    /// Sets the starting angle for entry placement.
    ///
    /// @param startAngleDegrees new starting angle
    ///
    public void setStartAngleDegrees(float startAngleDegrees) {
        this.radialMenu.setStartAngleDegrees(startAngleDegrees);
    }
    
    ///
    /// Sets the orbit radius used by the radial menu.
    ///
    /// @param radius orbit radius in pixels
    ///
    public void setRingRadius(int radius) {
        this.radialMenu.setRingRadius(radius);
    }
    
    ///
    /// Sets the hover offset used by the radial menu.
    ///
    /// @param hoverOffset hover offset in pixels
    ///
    public void setHoverOffset(int hoverOffset) {
        this.radialMenu.setHoverOffset(hoverOffset);
    }
    
    @Override
    protected UINode<?> create() {
        return UI.stack()
                .fill()
                .child(this.radialMenu.size(SLLength.content(), SLLength.content()))
                .build();
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xAA0B0F16);
    }
}
