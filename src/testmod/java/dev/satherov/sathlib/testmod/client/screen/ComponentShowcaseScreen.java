package dev.satherov.sathlib.testmod.client.screen;

import dev.satherov.sathlib.client.screen.ColorPickerScreen;
import dev.satherov.sathlib.client.screen.SLScreen;
import dev.satherov.sathlib.client.screen.UI;
import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.node.SLTextFieldNode;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.state.UIState;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public final class ComponentShowcaseScreen extends SLScreen {
    
    private static final List<String> TAB_LABELS = List.of("General", "Theme", "Preview");
    private static final List<String> DROPDOWN_LABELS = List.of("Compact", "Balanced", "Expanded");
    
    private final UIState<Boolean> checkboxState = new UIState<>(true);
    private final UIState<Boolean> toggleState = new UIState<>(false);
    private final UIState<Integer> tabState = new UIState<>(0);
    private final UIState<Integer> dropdownState = new UIState<>(1);
    private final UIState<String> textState = new UIState<>("Caret test");
    
    private final UIState<Float> scoreState = new UIState<>(0.0F);
    private final UIState<Component> titleState = new UIState<>(Component.literal("Must-Have Controls"));
    private final UIState<Component> statusState = new UIState<>(Component.literal("Ready"));
    private final UIState<Component> scoreTextState = new UIState<>(Component.empty());
    private final UIState<Component> summaryState = new UIState<>(Component.empty());
    private final UIState<Component> detailState = new UIState<>(Component.empty());
    private final UIState<Component> textPreviewState = new UIState<>(Component.empty());
    
    public ComponentShowcaseScreen() {
        super(Component.literal("SathLib Component Showcase"));
        this.refreshDerivedState();
    }
    
    @Override
    protected UINode<?> create() {
        return UI.box()
                .modifier(SLModifier.fill())
                .child(
                        UI.panel()
                                .modifier(
                                        SLModifier.none()
                                                .align(SLAlignment.CENTER, SLAlignment.CENTER)
                                                .size(UI.px(500), UI.px(320))
                                                .padding(UI.inset(14))
                                )
                                .gap(UI.scalar(10))
                                .child(UI.label().textState(this.titleState).build())
                                .child(
                                        UI.tabs()
                                                .modifier(SLModifier.none().width(UI.px(472)))
                                                .selectedIndexState(this.tabState)
                                                .tab(Component.literal("General"))
                                                .tab(Component.literal("Theme"))
                                                .tab(Component.literal("Preview"))
                                                .onCommit(this::handleTabCommit)
                                                .build()
                                )
                                .child(UI.divider().modifier(SLModifier.none().width(UI.px(472))).build())
                                .child(
                                        UI.row()
                                                .modifier(SLModifier.none().fillWidth())
                                                .gap(UI.scalar(16))
                                                .child(this.createControlColumn())
                                                .child(this.createSummaryColumn())
                                                .build()
                                )
                                .child(UI.divider().modifier(SLModifier.none().width(UI.px(472))).build())
                                .child(
                                        UI.row()
                                                .gap(UI.scalar(8))
                                                .child(UI.button().text(Component.literal("Widget Gallery")).onPress(button -> this.minecraft.setScreen(new WidgetGalleryScreen())).build())
                                                .child(UI.button().text(Component.literal("Color Bench")).onPress(button -> this.minecraft.setScreen(new ColorPickerScreen(0x33A6FF))).build())
                                                .child(UI.button().text(Component.literal("Reset")).onPress(button -> this.resetState()).build())
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
    
    private UINode<?> createControlColumn() {
        return UI.column()
                .modifier(SLModifier.none().width(UI.px(216)))
                .gap(UI.scalar(8))
                .child(UI.label().text(Component.literal("Selection")).build())
                .child(
                        UI.checkbox()
                                .text(Component.literal("Pin current layout"))
                                .checkedState(this.checkboxState)
                                .onCommit(this::handleCheckboxCommit)
                                .build()
                )
                .child(
                        UI.toggle()
                                .text(Component.literal("Accent boost"))
                                .checkedState(this.toggleState)
                                .onCommit(this::handleToggleCommit)
                                .build()
                )
                .child(
                        UI.dropdown()
                                .modifier(SLModifier.none().width(UI.px(188)))
                                .selectedIndexState(this.dropdownState)
                                .placeholder(Component.literal("Choose profile"))
                                .option(Component.literal("Compact"))
                                .option(Component.literal("Balanced"))
                                .option(Component.literal("Expanded"))
                                .onCommit(this::handleDropdownCommit)
                                .build()
                )
                .child(UI.divider().modifier(SLModifier.none().width(UI.px(188))).build())
                .child(UI.label().text(Component.literal("Caret test")).build())
                .child(this.createTextField())
                .child(UI.label().textState(this.textPreviewState).build())
                .build();
    }
    
    private UINode<?> createSummaryColumn() {
        return UI.column()
                .modifier(SLModifier.none().width(UI.weight()))
                .gap(UI.scalar(8))
                .child(UI.label().textState(this.statusState).build())
                .child(
                        UI.progressBar()
                                .modifier(SLModifier.none().size(UI.px(240), UI.px(16)))
                                .progressState(this.scoreState)
                                .overlayTextState(this.scoreTextState)
                                .build()
                )
                .child(UI.label().textState(this.summaryState).build())
                .child(UI.label().textState(this.detailState).build())
                .child(UI.divider().modifier(SLModifier.none().width(UI.px(240))).build())
                .child(UI.label().text(Component.literal("Current tab")).build())
                .child(UI.label().text(Component.literal("General / Theme / Preview")).color(this.theme().colors().textMuted()).build())
                .child(UI.label().text(Component.literal("Current profile")).build())
                .child(UI.label().text(Component.literal("Compact / Balanced / Expanded")).color(this.theme().colors().textMuted()).build())
                .build();
    }
    
    private SLTextFieldNode createTextField() {
        return SLTextFieldNode.builder()
                .modifier(SLModifier.none().size(UI.px(188), UI.px(20)))
                .placeholder("Type or paste here")
                .maxLength(32)
                .value(this.textState.get())
                .onValueChanged(this::handleTextChanged)
                .onCommit(this::handleTextCommitted)
                .build();
    }
    
    private void handleTabCommit(int selectedIndex) {
        this.refreshDerivedState();
        this.statusState.set(Component.literal("Tab: " + ComponentShowcaseScreen.TAB_LABELS.get(selectedIndex)));
    }
    
    private void handleCheckboxCommit(boolean checked) {
        this.refreshDerivedState();
        this.statusState.set(Component.literal(checked ? "Layout pinned" : "Layout unlocked"));
    }
    
    private void handleToggleCommit(boolean checked) {
        this.refreshDerivedState();
        this.statusState.set(Component.literal(checked ? "Accent boost on" : "Accent boost off"));
    }
    
    private void handleDropdownCommit(int selectedIndex) {
        this.refreshDerivedState();
        this.statusState.set(Component.literal("Profile: " + ComponentShowcaseScreen.DROPDOWN_LABELS.get(selectedIndex)));
    }
    
    private void handleTextChanged(String value) {
        this.textState.set(value);
        this.refreshDerivedState();
    }
    
    private void handleTextCommitted(String value) {
        this.textState.set(value);
        this.refreshDerivedState();
        this.statusState.set(Component.literal("Text committed"));
    }
    
    private void resetState() {
        this.checkboxState.set(true);
        this.toggleState.set(false);
        this.tabState.set(0);
        this.dropdownState.set(1);
        this.textState.set("Caret test");
        this.refreshDerivedState();
        this.statusState.set(Component.literal("Ready"));
    }
    
    private void refreshDerivedState() {
        float score = 0.20F;
        if (this.checkboxState.get()) {
            score += 0.18F;
        }
        if (this.toggleState.get()) {
            score += 0.17F;
        }
        score += this.dropdownState.get() * 0.12F;
        score += this.tabState.get() * 0.08F;
        score += Math.min(this.textState.get().length(), 16) / 16.0F * 0.13F;
        score = Math.min(1.0F, score);
        
        String tabLabel = ComponentShowcaseScreen.TAB_LABELS.get(this.tabState.get());
        String profileLabel = ComponentShowcaseScreen.DROPDOWN_LABELS.get(this.dropdownState.get());
        String toggleLabel = this.toggleState.get() ? "boosted" : "neutral";
        String pinLabel = this.checkboxState.get() ? "pinned" : "free";
        
        this.scoreState.set(score);
        this.scoreTextState.set(Component.literal(String.format(Locale.ROOT, "Readiness %.0f%%", score * 100.0F)));
        this.summaryState.set(Component.literal("Layout " + pinLabel + ", theme " + toggleLabel + ", tab " + tabLabel));
        this.detailState.set(Component.literal("Profile " + profileLabel + " with " + this.textState.get().length() + " typed chars"));
        this.textPreviewState.set(Component.literal("Preview: " + this.textState.get()));
    }
}
