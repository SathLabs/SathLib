package dev.satherov.sathlib.client.screen;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.node.SLButtonNode;
import dev.satherov.sathlib.client.screen.node.SLCheckboxNode;
import dev.satherov.sathlib.client.screen.node.SLColumnNode;
import dev.satherov.sathlib.client.screen.node.SLDividerNode;
import dev.satherov.sathlib.client.screen.node.SLDropdownNode;
import dev.satherov.sathlib.client.screen.node.SLEnergyBarNode;
import dev.satherov.sathlib.client.screen.node.SLFluidTankNode;
import dev.satherov.sathlib.client.screen.node.SLLabelNode;
import dev.satherov.sathlib.client.screen.node.SLMenuSlotGridNode;
import dev.satherov.sathlib.client.screen.node.SLPanelNode;
import dev.satherov.sathlib.client.screen.node.SLProgressBarNode;
import dev.satherov.sathlib.client.screen.node.SLRowNode;
import dev.satherov.sathlib.client.screen.node.SLSliderNode;
import dev.satherov.sathlib.client.screen.node.SLStackNode;
import dev.satherov.sathlib.client.screen.node.SLSwitchNode;
import dev.satherov.sathlib.client.screen.node.SLTabsNode;
import dev.satherov.sathlib.client.screen.view.SLEnergyView;
import dev.satherov.sathlib.client.screen.view.SLFluidTankView;
import dev.satherov.sathlib.common.menu.SLMenu;
import dev.satherov.sathlib.common.menu.logic.SLSlotKey;
import dev.satherov.sathlib.common.menu.logic.SLSlotKeys;

///
/// Common UI entrypoint for all default component builders and layout helpers.
///
@UtilityClass
public class UI {
    
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
    /// Returns the real checkbox builder.
    ///
    /// @return checkbox builder
    ///
    public static SLCheckboxNode.SLCheckboxNodeBuilder checkbox() {
        return SLCheckboxNode.builder();
    }
    
    ///
    /// Returns the real toggle-switch builder.
    ///
    /// @return switch builder
    ///
    public static SLSwitchNode.SLSwitchNodeBuilder toggle() {
        return SLSwitchNode.builder();
    }
    
    ///
    /// Returns the real divider builder.
    ///
    /// @return divider builder
    ///
    public static SLDividerNode.SLDividerNodeBuilder divider() {
        return SLDividerNode.builder();
    }
    
    ///
    /// Returns the real tab-strip builder.
    ///
    /// @return tab-strip builder
    ///
    public static SLTabsNode.SLTabsNodeBuilder tabs() {
        return SLTabsNode.builder();
    }
    
    ///
    /// Returns the real dropdown builder.
    ///
    /// @return dropdown builder
    ///
    public static SLDropdownNode.SLDropdownNodeBuilder dropdown() {
        return SLDropdownNode.builder();
    }
    
    ///
    /// Returns the real slider builder.
    ///
    /// @return slider builder
    ///
    public static SLSliderNode.SLSliderNodeBuilder slider() {
        return SLSliderNode.builder();
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
    /// Returns the real energy-bar builder.
    ///
    /// @return energy-bar builder
    ///
    public static SLEnergyBarNode.SLEnergyBarNodeBuilder energyBar() {
        return SLEnergyBarNode.builder();
    }
    
    ///
    /// Returns the real fluid-tank builder.
    ///
    /// @return fluid-tank builder
    ///
    public static SLFluidTankNode.SLFluidTankNodeBuilder fluidTank() {
        return SLFluidTankNode.builder();
    }
    
    ///
    /// Creates an energy-bar builder already bound to one energy view.
    ///
    /// @param energyView backing energy view
    ///
    /// @return energy-bar builder
    ///
    public static SLEnergyBarNode.SLEnergyBarNodeBuilder energy(SLEnergyView energyView) {
        return UI.energyBar().energyView(energyView);
    }
    
    ///
    /// Creates a fluid-tank builder already bound to one fluid-tank view.
    ///
    /// @param fluidTankView backing fluid-tank view
    ///
    /// @return fluid-tank builder
    ///
    public static SLFluidTankNode.SLFluidTankNodeBuilder fluid(SLFluidTankView fluidTankView) {
        return UI.fluidTank().fluidTankView(fluidTankView);
    }
    
    ///
    /// Creates a slot-grid builder for one logical slot group.
    ///
    /// @param menu    backing menu
    /// @param slotKey logical slot group to position
    ///
    /// @return slot-grid builder
    ///
    public static SLMenuSlotGridNode.SLMenuSlotGridNodeBuilder slots(SLMenu menu, SLSlotKey slotKey) {
        return SLMenuSlotGridNode.builder().slots(menu.getSlots(slotKey));
    }
    
    ///
    /// Creates a single-slot builder for one logical slot group.
    ///
    /// @param menu    backing menu
    /// @param slotKey logical slot group to position
    ///
    /// @return slot-grid builder
    ///
    public static SLMenuSlotGridNode.SLMenuSlotGridNodeBuilder slot(SLMenu menu, SLSlotKey slotKey) {
        return UI.slots(menu, slotKey).columns(1);
    }
    
    ///
    /// Creates a standard 3x9 player inventory slot-grid builder.
    ///
    /// @param menu backing menu
    ///
    /// @return slot-grid builder
    ///
    public static SLMenuSlotGridNode.SLMenuSlotGridNodeBuilder playerInventory(SLMenu menu) {
        return UI.slots(menu, SLSlotKeys.PLAYER_INVENTORY).columns(9);
    }
    
    ///
    /// Creates a standard 9-slot hotbar grid builder.
    ///
    /// @param menu backing menu
    ///
    /// @return slot-grid builder
    ///
    public static SLMenuSlotGridNode.SLMenuSlotGridNodeBuilder hotbar(SLMenu menu) {
        return UI.slots(menu, SLSlotKeys.PLAYER_HOTBAR).columns(9);
    }
}
