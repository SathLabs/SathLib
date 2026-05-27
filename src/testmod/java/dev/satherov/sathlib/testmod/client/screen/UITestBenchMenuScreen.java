package dev.satherov.sathlib.testmod.client.screen;

import dev.satherov.sathlib.client.screen.SLMenuScreen;
import dev.satherov.sathlib.client.screen.UI;
import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.node.SLTextFieldNode;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.slot.SLSlotChrome;
import dev.satherov.sathlib.client.screen.state.UIState;
import dev.satherov.sathlib.client.screen.view.SLEnergyView;
import dev.satherov.sathlib.client.screen.view.SLFluidTankView;
import dev.satherov.sathlib.common.menu.logic.SLSlotKeys;
import dev.satherov.sathlib.testmod.common.menu.UITestBenchMenu;
import dev.satherov.sathlib.util.SLStringUtils;

import net.neoforged.neoforge.fluids.FluidStack;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.jspecify.annotations.NonNull;

import java.util.Locale;

public final class UITestBenchMenuScreen extends SLMenuScreen<UITestBenchMenu> {
    
    private static final long FLUID_CAPACITY = 8_000L;
    private static final long ENERGY_CAPACITY = 120_000L;
    private static final SLSlotChrome MACHINE_SLOT_CHROME = new SLSlotChrome(true, 0xFF0A0D11, 0xFF49505D);
    private static final SLSlotChrome STORAGE_SLOT_CHROME = new SLSlotChrome(true, 0xFF090B0E, 0xFF555D6B);
    private static final SLSlotChrome OUTPUT_CHROME = new SLSlotChrome(true, 0xFF101317, 0xFF6B7892);
    
    private final UIState<Float> progressState = new UIState<>(0.42F);
    private final UIState<Float> fluidFillState = new UIState<>(0.68F);
    private final UIState<Float> energyFillState = new UIState<>(0.81F);
    private final UIState<String> noteState = new UIState<>("Shift-click, drag, and type");
    
    private final UIState<Component> statusTextState = new UIState<>(Component.literal("Idle"));
    private final UIState<Component> progressTextState = new UIState<>(Component.empty());
    private final UIState<Component> notePreviewState = new UIState<>(Component.empty());
    private final UIState<Component> fluidButtonTextState = new UIState<>(Component.empty());
    private final UIState<Component> fluidFillTextState = new UIState<>(Component.empty());
    private final UIState<Component> energyFillTextState = new UIState<>(Component.empty());
    
    private FluidPreset fluidPreset = FluidPreset.WATER;
    
    public UITestBenchMenuScreen(UITestBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 500, 260);
        this.refreshDerivedState();
    }
    
    private static String percentText(float value) {
        return String.format(Locale.ROOT, "%.0f%%", value * 100.0F);
    }
    
    private static String amountText(long amount, String unit) {
        return String.format(Locale.ROOT, "%s %s", SLStringUtils.displayDecimal(amount), unit);
    }
    
    @Override
    protected UINode<?> create() {
        return UI.column()
                .modifier(SLModifier.fill().padding(UI.inset(12)))
                .gap(UI.scalar(14))
                .child(
                        UI.row()
                                .modifier(SLModifier.none().fillWidth())
                                .gap(UI.scalar(16))
                                .child(this.createMachineColumn())
                                .child(this.createMeterSection())
                                .child(this.createControlColumn())
                                .build()
                )
                .child(this.createPlayerInventorySection())
                .build();
    }
    
    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, this.root().getTheme().colors().scrim());
        SLRenderContext context = new SLRenderContext(graphics, this.font, this.root().getTheme(), partialTick, mouseX, mouseY);
        this.root().getTheme().renderPanel(context, new SLBounds(this.leftPos, this.topPos, this.imageWidth, this.imageHeight));
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) { }
    
    private UINode<?> createMachineColumn() {
        return UI.column()
                .modifier(SLModifier.none().width(UI.px(124)))
                .gap(UI.scalar(6))
                .child(UI.label().text(Component.literal("Machine slots")).build())
                .child(
                        UI.row()
                                .gap(UI.scalar(6))
                                .child(
                                        UI.slots(this.menu, SLSlotKeys.MACHINE_INPUT)
                                                .columns(2)
                                                .gap(UI.scalar(2))
                                                .slotSize(20)
                                                .contentInset(2)
                                                .chrome(UITestBenchMenuScreen.MACHINE_SLOT_CHROME)
                                                .build()
                                )
                                .child(
                                        UI.slot(this.menu, SLSlotKeys.MACHINE_OUTPUT)
                                                .slotSize(20)
                                                .contentInset(2)
                                                .chrome(UITestBenchMenuScreen.OUTPUT_CHROME)
                                                .build()
                                )
                                .build()
                )
                .child(UI.label().text(Component.literal("Storage")).build())
                .child(
                        UI.slots(this.menu, SLSlotKeys.MACHINE_STORAGE)
                                .columns(3)
                                .gap(UI.scalar(2))
                                .slotSize(20)
                                .contentInset(2)
                                .chrome(UITestBenchMenuScreen.STORAGE_SLOT_CHROME)
                                .build()
                )
                .build();
    }
    
    private UINode<?> createMeterSection() {
        return UI.row()
                .gap(UI.scalar(10))
                .child(this.createFluidBench())
                .child(this.createEnergyBench())
                .build();
    }
    
    private UINode<?> createControlColumn() {
        return UI.column()
                .modifier(SLModifier.none().width(UI.weight()))
                .gap(UI.scalar(8))
                .child(UI.label().textState(this.statusTextState).build())
                .child(
                        UI.progressBar()
                                .modifier(SLModifier.none().size(UI.px(174), UI.px(16)))
                                .progressState(this.progressState)
                                .overlayTextState(this.progressTextState)
                                .build()
                )
                .child(
                        UI.slider()
                                .modifier(SLModifier.none().size(UI.px(174), UI.px(14)))
                                .valueState(this.progressState)
                                .onValueChanged(this::handleProgressChange)
                                .onCommit(value -> this.updateStatus("Progress " + UITestBenchMenuScreen.percentText(value)))
                                .build()
                )
                .child(UI.label().textState(this.notePreviewState).build())
                .child(this.createNoteField())
                .child(
                        UI.row()
                                .gap(UI.scalar(6))
                                .child(UI.button().text(Component.literal("Reset")).onPress(button -> this.resetBench()).build())
                                .child(UI.button().textState(this.fluidButtonTextState).onPress(button -> this.toggleFluidPreset()).build())
                                .build()
                )
                .build();
    }
    
    private UINode<?> createFluidBench() {
        return UI.column()
                .modifier(SLModifier.none().width(UI.px(60)))
                .gap(UI.scalar(4))
                .child(UI.label().text(Component.literal("Fluid")).build())
                .child(
                        UI.row()
                                .gap(UI.scalar(6))
                                .child(
                                        UI.stack()
                                                .modifier(SLModifier.none().size(UI.px(36), UI.px(88)))
                                                .child(
                                                        UI.fluid(this.createFluidView())
                                                                .modifier(SLModifier.none().align(SLAlignment.CENTER, SLAlignment.START).size(UI.px(18), UI.px(72)))
                                                                .build()
                                                )
                                                .child(
                                                        UI.label()
                                                                .modifier(SLModifier.none().align(SLAlignment.CENTER, SLAlignment.END))
                                                                .textState(this.fluidFillTextState)
                                                                .build()
                                                )
                                                .build()
                                )
                                .child(
                                        UI.slider()
                                                .modifier(SLModifier.none().size(UI.px(12), UI.px(72)))
                                                .axis(SLAxis.VERTICAL)
                                                .inverted(true)
                                                .valueState(this.fluidFillState)
                                                .onValueChanged(this::handleFluidChange)
                                                .onCommit(value -> this.updateStatus(this.fluidPreset.label + " " + UITestBenchMenuScreen.amountText(this.fluidAmount(), "mB")))
                                                .build()
                                )
                                .build()
                )
                .build();
    }
    
    private UINode<?> createEnergyBench() {
        return UI.column()
                .modifier(SLModifier.none().width(UI.px(60)))
                .gap(UI.scalar(4))
                .child(UI.label().text(Component.literal("Energy")).build())
                .child(
                        UI.row()
                                .gap(UI.scalar(6))
                                .child(
                                        UI.stack()
                                                .modifier(SLModifier.none().size(UI.px(36), UI.px(88)))
                                                .child(
                                                        UI.energy(this.createEnergyView())
                                                                .modifier(SLModifier.none().align(SLAlignment.CENTER, SLAlignment.START).size(UI.px(18), UI.px(72)))
                                                                .vertical(true)
                                                                .build()
                                                )
                                                .child(
                                                        UI.label()
                                                                .modifier(SLModifier.none().align(SLAlignment.CENTER, SLAlignment.END))
                                                                .textState(this.energyFillTextState)
                                                                .build()
                                                )
                                                .build()
                                )
                                .child(
                                        UI.slider()
                                                .modifier(SLModifier.none().size(UI.px(12), UI.px(72)))
                                                .axis(SLAxis.VERTICAL)
                                                .inverted(true)
                                                .valueState(this.energyFillState)
                                                .onValueChanged(this::handleEnergyChange)
                                                .onCommit(value -> this.updateStatus("Energy " + UITestBenchMenuScreen.amountText(this.energyAmount(), "FE")))
                                                .build()
                                )
                                .build()
                )
                .build();
    }
    
    private UINode<?> createPlayerInventorySection() {
        return UI.column()
                .gap(UI.scalar(4))
                .child(UI.label().text(Component.literal("Player inventory")).build())
                .child(UI.playerInventory(this.menu).gap(UI.scalar(2)).build())
                .child(UI.hotbar(this.menu).gap(UI.scalar(2)).build())
                .build();
    }
    
    private SLTextFieldNode createNoteField() {
        return SLTextFieldNode.builder()
                .modifier(SLModifier.none().size(UI.px(174), UI.px(20)))
                .placeholder("Bench note")
                .maxLength(28)
                .valueState(this.noteState)
                .onCommit(this::commitNote)
                .build();
    }
    
    private SLFluidTankView createFluidView() {
        return SLFluidTankView.of(() -> new FluidStack(this.fluidPreset.fluid, this.fluidAmount()), () -> UITestBenchMenuScreen.FLUID_CAPACITY);
    }
    
    private SLEnergyView createEnergyView() {
        return SLEnergyView.of(this::energyAmount, () -> UITestBenchMenuScreen.ENERGY_CAPACITY);
    }
    
    private long energyAmount() {
        return Math.round(UITestBenchMenuScreen.ENERGY_CAPACITY * this.energyFillState.get());
    }
    
    private int fluidAmount() {
        return Math.round(UITestBenchMenuScreen.FLUID_CAPACITY * this.fluidFillState.get());
    }
    
    private void handleProgressChange(float value) {
        this.progressTextState.set(Component.literal(UITestBenchMenuScreen.percentText(value)));
    }
    
    private void handleFluidChange(float value) {
        this.fluidFillTextState.set(Component.literal(UITestBenchMenuScreen.percentText(value)));
    }
    
    private void handleEnergyChange(float value) {
        this.energyFillTextState.set(Component.literal(UITestBenchMenuScreen.percentText(value)));
    }
    
    private void commitNote(String value) {
        String normalizedValue = value == null ? "" : value;
        this.noteState.set(normalizedValue);
        this.notePreviewState.set(Component.literal("Note: " + normalizedValue));
        this.updateStatus("Note saved");
    }
    
    private void toggleFluidPreset() {
        this.fluidPreset = this.fluidPreset == FluidPreset.WATER ? FluidPreset.LAVA : FluidPreset.WATER;
        this.refreshDerivedState();
        this.updateStatus("Fluid set to " + this.fluidPreset.label);
    }
    
    private void resetBench() {
        this.progressState.set(0.42F);
        this.fluidFillState.set(0.68F);
        this.energyFillState.set(0.81F);
        this.noteState.set("Shift-click, drag, and type");
        this.fluidPreset = FluidPreset.WATER;
        this.refreshDerivedState();
        this.updateStatus("Bench reset");
    }
    
    private void refreshDerivedState() {
        this.handleProgressChange(this.progressState.get());
        this.handleFluidChange(this.fluidFillState.get());
        this.handleEnergyChange(this.energyFillState.get());
        this.notePreviewState.set(Component.literal("Note: " + this.noteState.get()));
        this.fluidButtonTextState.set(Component.literal("Fluid: " + this.fluidPreset.label));
    }
    
    private void updateStatus(String text) {
        this.statusTextState.set(Component.literal(text));
    }
    
    private enum FluidPreset {
        WATER(Fluids.WATER, "Water"),
        LAVA(Fluids.LAVA, "Lava"),
        ;
        
        private final Fluid fluid;
        private final String label;
        
        FluidPreset(Fluid fluid, String label) {
            this.fluid = fluid;
            this.label = label;
        }
    }
}
