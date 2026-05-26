package dev.satherov.sathlib.client.screen;

import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.node.SLButtonNode;
import dev.satherov.sathlib.client.screen.node.SLColumnNode;
import dev.satherov.sathlib.client.screen.node.SLLabelNode;
import dev.satherov.sathlib.client.screen.node.SLMenuSlotGridNode;
import dev.satherov.sathlib.client.screen.node.SLPanelNode;
import dev.satherov.sathlib.client.screen.node.SLProgressBarNode;
import dev.satherov.sathlib.client.screen.node.SLRowNode;
import dev.satherov.sathlib.client.screen.node.SLStackNode;
import dev.satherov.sathlib.common.menu.SLMenu;
import dev.satherov.sathlib.common.menu.logic.SLSlotSemantic;
import dev.satherov.sathlib.common.menu.logic.SLSlotSemantics;

///
/// Thin UI entrypoint that exposes the real Lombok builders and layout helpers.
///
/// The old duplicated runtime-vs-builder hierarchy has been removed. Callers
/// now work directly with component builders and a shared {@link SLModifier}
/// model.
///
public final class UI {
    
    private UI() { }
    
    ///
    /// Returns a new modifier builder.
    ///
    /// @return modifier builder
    ///
    public static SLModifier.SLModifierBuilder modifier() {
        return SLModifier.builder();
    }
    
    ///
    /// Returns the shared empty modifier.
    ///
    /// @return empty modifier
    ///
    public static SLModifier none() {
        return SLModifier.none();
    }
    
    ///
    /// Returns a fill modifier for both axes.
    ///
    /// @return fill modifier
    ///
    public static SLModifier fill() {
        return SLModifier.fill();
    }
    
    ///
    /// Returns the content-sized length.
    ///
    /// @return content length
    ///
    public static SLLength content() {
        return SLLength.content();
    }
    
    ///
    /// Returns a fixed-pixel length.
    ///
    /// @param pixels fixed size in pixels
    ///
    /// @return fixed length
    ///
    public static SLLength px(int pixels) {
        return SLLength.pixels(pixels);
    }
    
    ///
    /// Returns a percentage-based length.
    ///
    /// @param percent percentage as a fraction between {@code 0.0} and
    ///                {@code 1.0}
    ///
    /// @return percentage length
    ///
    public static SLLength percent(float percent) {
        return SLLength.percent(percent);
    }
    
    ///
    /// Returns a fill length with the default weight.
    ///
    /// @return fill length
    ///
    public static SLLength weight() {
        return SLLength.fill();
    }
    
    ///
    /// Returns a weighted fill length.
    ///
    /// @param weight relative fill weight
    ///
    /// @return weighted fill length
    ///
    public static SLLength weight(float weight) {
        return SLLength.fill(weight);
    }
    
    ///
    /// Returns a fixed scalar.
    ///
    /// @param pixels fixed scalar in pixels
    ///
    /// @return fixed scalar
    ///
    public static SLScalar scalar(int pixels) {
        return SLScalar.pixels(pixels);
    }
    
    ///
    /// Returns a percentage scalar.
    ///
    /// @param percent percentage as a fraction between {@code 0.0} and
    ///                {@code 1.0}
    ///
    /// @return percentage scalar
    ///
    public static SLScalar scalarPercent(float percent) {
        return SLScalar.percent(percent);
    }
    
    ///
    /// Returns uniform insets.
    ///
    /// @param pixels inset size on all sides
    ///
    /// @return uniform insets
    ///
    public static SLInsets inset(int pixels) {
        return SLInsets.all(pixels);
    }
    
    ///
    /// Returns symmetric insets.
    ///
    /// @param horizontal horizontal inset size
    /// @param vertical   vertical inset size
    ///
    /// @return symmetric insets
    ///
    public static SLInsets inset(int horizontal, int vertical) {
        return SLInsets.symmetric(horizontal, vertical);
    }
    
    ///
    /// Returns fully specified insets.
    ///
    /// @param left   left inset size
    /// @param top    top inset size
    /// @param right  right inset size
    /// @param bottom bottom inset size
    ///
    /// @return fully specified insets
    ///
    public static SLInsets inset(int left, int top, int right, int bottom) {
        return SLInsets.of(left, top, right, bottom);
    }
    
    ///
    /// Returns the real column builder.
    ///
    /// @return column builder
    ///
    public static SLColumnNode.SLColumnNodeBuilder column() {
        return SLColumnNode.builder();
    }
    
    ///
    /// Returns the real row builder.
    ///
    /// @return row builder
    ///
    public static SLRowNode.SLRowNodeBuilder row() {
        return SLRowNode.builder();
    }
    
    ///
    /// Returns the real stack builder.
    ///
    /// @return stack builder
    ///
    public static SLStackNode.SLStackNodeBuilder stack() {
        return SLStackNode.builder();
    }
    
    ///
    /// Returns the compose-like box builder.
    ///
    /// @return stack builder used as a box
    ///
    public static SLStackNode.SLStackNodeBuilder box() {
        return SLStackNode.builder();
    }
    
    ///
    /// Returns the real panel builder.
    ///
    /// @return panel builder
    ///
    public static SLPanelNode.SLPanelNodeBuilder panel() {
        return SLPanelNode.builder();
    }
    
    ///
    /// Returns the real label builder.
    ///
    /// @return label builder
    ///
    public static SLLabelNode.SLLabelNodeBuilder label() {
        return SLLabelNode.builder();
    }
    
    ///
    /// Returns the real button builder.
    ///
    /// @return button builder
    ///
    public static SLButtonNode.SLButtonNodeBuilder button() {
        return SLButtonNode.builder();
    }
    
    ///
    /// Returns the real progress-bar builder.
    ///
    /// @return progress-bar builder
    ///
    public static SLProgressBarNode.SLProgressBarNodeBuilder progressBar() {
        return SLProgressBarNode.builder();
    }
    
    ///
    /// Creates a slot-grid builder for one semantic slot group.
    ///
    /// @param menu     backing menu
    /// @param semantic semantic slot group to position
    ///
    /// @return slot-grid builder
    ///
    public static SLMenuSlotGridNode.SLMenuSlotGridNodeBuilder slots(SLMenu menu, SLSlotSemantic semantic) {
        return SLMenuSlotGridNode.builder().slots(menu.getSlots(semantic));
    }
    
    ///
    /// Creates a single-slot builder for one semantic slot group.
    ///
    /// @param menu     backing menu
    /// @param semantic semantic slot group to position
    ///
    /// @return slot-grid builder
    ///
    public static SLMenuSlotGridNode.SLMenuSlotGridNodeBuilder slot(SLMenu menu, SLSlotSemantic semantic) {
        return UI.slots(menu, semantic).columns(1);
    }
    
    ///
    /// Creates a standard 3x9 player inventory slot-grid builder.
    ///
    /// @param menu backing menu
    ///
    /// @return slot-grid builder
    ///
    public static SLMenuSlotGridNode.SLMenuSlotGridNodeBuilder playerInventory(SLMenu menu) {
        return UI.slots(menu, SLSlotSemantics.PLAYER_INVENTORY).columns(9);
    }
    
    ///
    /// Creates a standard 9-slot hotbar grid builder.
    ///
    /// @param menu backing menu
    ///
    /// @return slot-grid builder
    ///
    public static SLMenuSlotGridNode.SLMenuSlotGridNodeBuilder hotbar(SLMenu menu) {
        return UI.slots(menu, SLSlotSemantics.PLAYER_HOTBAR).columns(9);
    }
}
