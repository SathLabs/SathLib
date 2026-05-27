package dev.satherov.sathlib.testmod.client.screen;

import dev.satherov.sathlib.client.screen.ColorPickerScreen;
import dev.satherov.sathlib.client.screen.SLScreen;
import dev.satherov.sathlib.client.screen.UI;
import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.node.RadialMenuNode;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.state.UIState;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

public final class WidgetGalleryScreen extends SLScreen {
    
    private final UIState<Float> ringRadiusState = new UIState<>(0.45F);
    private final UIState<Float> buttonWidthState = new UIState<>(0.38F);
    private final UIState<Float> hoverOffsetState = new UIState<>(0.40F);
    
    private final UIState<Component> ringRadiusTextState = new UIState<>(Component.empty());
    private final UIState<Component> buttonWidthTextState = new UIState<>(Component.empty());
    private final UIState<Component> hoverOffsetTextState = new UIState<>(Component.empty());
    private final UIState<Component> statusTextState = new UIState<>(Component.literal("Radial menu ready"));
    
    private @Nullable RadialMenuNode radialMenu;
    
    public WidgetGalleryScreen() {
        super(Component.literal("SathLib Widget Gallery"));
        this.refreshLabels();
    }
    
    @Override
    protected UINode<?> create() {
        return UI.box()
                .modifier(SLModifier.fill())
                .child(
                        UI.panel()
                                .modifier(SLModifier.none().align(SLAlignment.CENTER, SLAlignment.CENTER).size(UI.px(392), UI.px(284)).padding(UI.inset(12)))
                                .gap(UI.scalar(12))
                                .child(
                                        UI.row()
                                                .gap(UI.scalar(16))
                                                .child(this.createRadialStack())
                                                .child(this.createControlColumn())
                                                .build()
                                )
                                .child(
                                        UI.row()
                                                .gap(UI.scalar(8))
                                                .child(UI.button().text(Component.literal("Open Controls")).onPress(button -> this.minecraft.setScreen(new ComponentShowcaseScreen())).build())
                                                .child(UI.button().text(Component.literal("Open Color Bench")).onPress(button -> this.minecraft.setScreen(new ColorPickerScreen(0x33A6FF))).build())
                                                .child(UI.button().text(Component.literal("Reset")).onPress(button -> this.resetBench()).build())
                                                .child(UI.button().text(Component.literal("Close")).onPress(button -> this.onClose()).build())
                                                .build()
                                )
                                .build()
                )
                .build();
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, this.theme().colors().scrim());
    }
    
    private UINode<?> createRadialStack() {
        this.radialMenu = RadialMenuNode.builder()
                .modifier(SLModifier.none().align(SLAlignment.CENTER, SLAlignment.CENTER).size(UI.px(220), UI.px(220)))
                .entry(new RadialMenuNode.Entry(Component.literal("Ping"), () -> this.setStatus("Ping")))
                .entry(new RadialMenuNode.Entry(Component.literal("Reset"), this::resetBench))
                .entry(new RadialMenuNode.Entry(Component.literal("Color"), () -> this.minecraft.setScreen(new ColorPickerScreen(0x33A6FF))))
                .entry(new RadialMenuNode.Entry(Component.literal("Wide"), this::setWideButtons))
                .entry(new RadialMenuNode.Entry(Component.literal("Tight"), this::setTightLayout))
                .entry(new RadialMenuNode.Entry(Component.literal("Close"), this::onClose))
                .build();
        this.syncRadialMenu();
        
        return UI.stack()
                .modifier(SLModifier.none().size(UI.px(228), UI.px(228)))
                .child(this.radialMenu)
                .child(
                        UI.label()
                                .modifier(SLModifier.none().align(SLAlignment.CENTER, SLAlignment.END))
                                .textState(this.statusTextState)
                                .build()
                )
                .build();
    }
    
    private UINode<?> createControlColumn() {
        return UI.column()
                .modifier(SLModifier.none().width(UI.px(124)))
                .gap(UI.scalar(10))
                .child(UI.label().textState(this.ringRadiusTextState).build())
                .child(
                        UI.slider()
                                .modifier(SLModifier.none().size(UI.px(124), UI.px(14)))
                                .valueState(this.ringRadiusState)
                                .onValueChanged(value -> this.syncFromSlider("Ring radius"))
                                .build()
                )
                .child(UI.label().textState(this.buttonWidthTextState).build())
                .child(
                        UI.slider()
                                .modifier(SLModifier.none().size(UI.px(124), UI.px(14)))
                                .valueState(this.buttonWidthState)
                                .onValueChanged(value -> this.syncFromSlider("Button width"))
                                .build()
                )
                .child(UI.label().textState(this.hoverOffsetTextState).build())
                .child(
                        UI.slider()
                                .modifier(SLModifier.none().size(UI.px(14), UI.px(96)))
                                .axis(SLAxis.VERTICAL)
                                .inverted(true)
                                .valueState(this.hoverOffsetState)
                                .onValueChanged(value -> this.syncFromSlider("Hover offset"))
                                .build()
                )
                .build();
    }
    
    private int ringRadius() {
        return 56 + Math.round(this.ringRadiusState.get() * 84.0F);
    }
    
    private int buttonWidth() {
        return 58 + Math.round(this.buttonWidthState.get() * 30.0F);
    }
    
    private int buttonHeight() {
        return 20 + Math.round(this.buttonWidthState.get() * 8.0F);
    }
    
    private int hoverOffset() {
        return 4 + Math.round(this.hoverOffsetState.get() * 14.0F);
    }
    
    private void syncFromSlider(String label) {
        this.refreshLabels();
        this.syncRadialMenu();
        this.setStatus(label);
    }
    
    private void syncRadialMenu() {
        if (this.radialMenu == null) {
            return;
        }
        
        this.radialMenu.setRingRadius(this.ringRadius());
        this.radialMenu.setButtonSize(this.buttonWidth(), this.buttonHeight());
        this.radialMenu.setHoverOffset(this.hoverOffset());
    }
    
    private void refreshLabels() {
        this.ringRadiusTextState.set(Component.literal("Ring: " + this.ringRadius()));
        this.buttonWidthTextState.set(Component.literal("Buttons: " + this.buttonWidth()));
        this.hoverOffsetTextState.set(Component.literal("Hover: " + this.hoverOffset()));
    }
    
    private void resetBench() {
        this.ringRadiusState.set(0.45F);
        this.buttonWidthState.set(0.38F);
        this.hoverOffsetState.set(0.40F);
        this.refreshLabels();
        this.syncRadialMenu();
        this.setStatus("Radial menu ready");
    }
    
    private void setWideButtons() {
        this.buttonWidthState.set(1.0F);
        this.refreshLabels();
        this.syncRadialMenu();
        this.setStatus("Wide buttons");
    }
    
    private void setTightLayout() {
        this.ringRadiusState.set(0.10F);
        this.hoverOffsetState.set(0.10F);
        this.refreshLabels();
        this.syncRadialMenu();
        this.setStatus("Tight layout");
    }
    
    private void setStatus(String text) {
        this.statusTextState.set(Component.literal(text));
    }
}
