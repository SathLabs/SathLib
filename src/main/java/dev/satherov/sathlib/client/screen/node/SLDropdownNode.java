package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;
import lombok.Singular;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.state.UIState;
import dev.satherov.sathlib.util.SLColorUtils;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

///
/// Built-in dropdown/select control backed by a selected option index.
///
/// The dropdown renders its popup as an overlay so opening it does not relayout
/// neighboring controls.
///
public final class SLDropdownNode extends UILeafNode<SLDropdownNode> {
    
    private static final int TRIGGER_HEIGHT = 20;
    private static final int OPTION_HEIGHT = 18;
    private static final int DEFAULT_WIDTH = 120;
    private static final int OPEN_GAP = 4;
    private static final int PRESSED_TRIGGER = -2;
    
    private final List<Component> options;
    private @Nullable Component placeholder;
    private int selectedIndex;
    private boolean open;
    private @Nullable UIState<Integer> selectedIndexState;
    private @Nullable Consumer<Integer> onValueChanged;
    private @Nullable Consumer<Integer> onCommit;
    private @Nullable Runnable unsubscribeSelectedIndexState;
    
    private boolean wasFocused;
    private int pressedTarget = -1;
    private SLBounds triggerBounds = SLBounds.EMPTY;
    private SLBounds popupBounds = SLBounds.EMPTY;
    private List<SLBounds> optionBounds = List.of();
    
    ///
    /// Creates an empty dropdown.
    ///
    public SLDropdownNode() {
        this(SLModifier.none(), List.of(), null, -1, false, null, null, null);
    }
    
    ///
    /// Creates a fully configured dropdown.
    ///
    /// @param modifier           node modifier
    /// @param options            selectable options
    /// @param placeholder        placeholder shown when no option is selected
    /// @param selectedIndex      initially selected option index
    /// @param open               whether the dropdown starts expanded
    /// @param selectedIndexState optional selected-index binding
    /// @param onValueChanged     optional live-change callback
    /// @param onCommit           optional commit callback
    ///
    private SLDropdownNode(
            SLModifier modifier,
            List<Component> options,
            @Nullable Component placeholder,
            int selectedIndex,
            boolean open,
            @Nullable UIState<Integer> selectedIndexState,
            @Nullable Consumer<Integer> onValueChanged,
            @Nullable Consumer<Integer> onCommit
    ) {
        super(modifier);
        this.options = List.copyOf(Objects.requireNonNullElse(options, List.of()));
        this.placeholder = placeholder;
        this.selectedIndex = this.normalizeIndex(selectedIndex);
        this.open = open && !this.options.isEmpty();
        this.selectedIndexState = selectedIndexState;
        this.onValueChanged = onValueChanged;
        this.onCommit = onCommit;
        
        if (selectedIndexState != null) {
            this.selectedIndex = this.normalizeIndex(Objects.requireNonNullElse(selectedIndexState.get(), -1));
        }
    }
    
    ///
    /// Creates a builder-backed dropdown while normalizing omitted values to
    /// framework defaults.
    ///
    /// @param modifier           node modifier
    /// @param options            selectable options
    /// @param placeholder        placeholder shown when no option is selected
    /// @param selectedIndex      initially selected option index
    /// @param open               whether the dropdown starts expanded
    /// @param selectedIndexState optional selected-index binding
    /// @param onValueChanged     optional live-change callback
    /// @param onCommit           optional commit callback
    ///
    /// @return configured dropdown node
    ///
    @Builder(builderMethodName = "builder")
    public static SLDropdownNode of(
            SLModifier modifier,
            @Singular("option") List<Component> options,
            Component placeholder,
            Integer selectedIndex,
            Boolean open,
            UIState<Integer> selectedIndexState,
            Consumer<Integer> onValueChanged,
            Consumer<Integer> onCommit
    ) {
        return new SLDropdownNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(options, List.of()),
                placeholder,
                Objects.requireNonNullElse(selectedIndex, -1),
                Objects.requireNonNullElse(open, false),
                selectedIndexState,
                onValueChanged,
                onCommit
        );
    }
    
    ///
    /// Sets the placeholder label.
    ///
    /// @param placeholder new placeholder label
    ///
    /// @return this dropdown
    ///
    public SLDropdownNode placeholder(@Nullable Component placeholder) {
        this.placeholder = placeholder;
        this.invalidateLayout();
        return this;
    }
    
    ///
    /// Sets the selected option index.
    ///
    /// @param selectedIndex new selected option index
    ///
    /// @return this dropdown
    ///
    public SLDropdownNode selectedIndex(int selectedIndex) {
        this.setSelectedIndex(selectedIndex, false);
        return this;
    }
    
    ///
    /// Binds the selected index to observable state.
    ///
    /// @param selectedIndexState observable selected-index state
    ///
    /// @return this dropdown
    ///
    public SLDropdownNode bindSelectedIndex(UIState<Integer> selectedIndexState) {
        this.selectedIndexState = Objects.requireNonNull(selectedIndexState);
        this.selectedIndex = this.normalizeIndex(Objects.requireNonNullElse(selectedIndexState.get(), -1));
        return this;
    }
    
    ///
    /// Sets the live-change callback.
    ///
    /// @param onValueChanged callback invoked after the selected option changes
    ///
    /// @return this dropdown
    ///
    public SLDropdownNode onValueChanged(@Nullable Consumer<Integer> onValueChanged) {
        this.onValueChanged = onValueChanged;
        return this;
    }
    
    ///
    /// Sets the commit callback.
    ///
    /// @param onCommit callback invoked after one selection activation
    ///
    /// @return this dropdown
    ///
    public SLDropdownNode onCommit(@Nullable Consumer<Integer> onCommit) {
        this.onCommit = onCommit;
        return this;
    }
    
    @Override
    protected boolean isInputTarget() {
        return true;
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        if (this.selectedIndexState != null) {
            this.unsubscribeSelectedIndexState = this.selectedIndexState.listen(value -> this.selectedIndex = this.normalizeIndex(Objects.requireNonNullElse(value, -1)));
        }
    }
    
    @Override
    protected void onDetached() {
        if (this.unsubscribeSelectedIndexState != null) {
            this.unsubscribeSelectedIndexState.run();
            this.unsubscribeSelectedIndexState = null;
        }
    }
    
    @Override
    protected void tick() {
        if (this.wasFocused && !this.isFocused()) this.setOpen(false);
        this.wasFocused = this.isFocused();
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        int widestLabel = 0;
        for (Component option : this.options) widestLabel = Math.max(widestLabel, font.width(option));
        if (this.placeholder != null) widestLabel = Math.max(widestLabel, font.width(this.placeholder));
        
        int width = Math.max(SLDropdownNode.DEFAULT_WIDTH, widestLabel + 26);
        return new SLMeasuredSize(width, SLDropdownNode.TRIGGER_HEIGHT);
    }
    
    @Override
    protected void onLayout(Font font, SLBounds contentBounds) {
        this.triggerBounds = new SLBounds(contentBounds.x(), contentBounds.y(), contentBounds.width(), SLDropdownNode.TRIGGER_HEIGHT);
        
        if (!this.open || this.options.isEmpty()) {
            this.popupBounds = SLBounds.EMPTY;
            this.optionBounds = List.of();
            return;
        }
        
        int popupHeight = this.options.size() * SLDropdownNode.OPTION_HEIGHT;
        this.popupBounds = new SLBounds(contentBounds.x(), this.triggerBounds.bottom() + SLDropdownNode.OPEN_GAP, contentBounds.width(), popupHeight);
        
        List<SLBounds> computedOptionBounds = new ArrayList<>(this.options.size());
        int optionY = this.popupBounds.y();
        for (int optionIndex = 0; optionIndex < this.options.size(); optionIndex++) {
            computedOptionBounds.add(new SLBounds(this.popupBounds.x(), optionY, this.popupBounds.width(), SLDropdownNode.OPTION_HEIGHT));
            optionY += SLDropdownNode.OPTION_HEIGHT;
        }
        this.optionBounds = List.copyOf(computedOptionBounds);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        int borderColor = this.isFocused() || this.isHovered()
                ? context.theme().colors().insetStrong()
                : context.theme().colors().insetBorder();
        int triggerTop = SLColorUtils.lerp(0.16F, context.theme().colors().insetFill(), context.theme().colors().panelFillTop());
        int triggerBottom = context.theme().colors().insetFill();
        
        context.fillVerticalGradient(this.triggerBounds, triggerTop, triggerBottom);
        context.outline(this.triggerBounds, borderColor);
        
        Component selectedText = this.selectedIndex >= 0 && this.selectedIndex < this.options.size()
                ? this.options.get(this.selectedIndex)
                : Objects.requireNonNullElse(this.placeholder, Component.empty());
        boolean showPlaceholder = this.selectedIndex < 0;
        int textColor = showPlaceholder ? context.theme().colors().textMuted() : context.theme().labelColor(this.isEnabled());
        int textY = context.centeredVisualTextY(this.triggerBounds);
        context.text(selectedText, this.triggerBounds.x() + 6, textY, textColor, false);
        
        int arrowCenterX = this.triggerBounds.right() - 10;
        int arrowCenterY = this.triggerBounds.y() + (this.triggerBounds.height() / 2);
        if (this.open) {
            context.fill(new SLBounds(arrowCenterX - 3, arrowCenterY + 1, 7, 1), context.theme().colors().textPrimary());
            context.fill(new SLBounds(arrowCenterX - 2, arrowCenterY, 5, 1), context.theme().colors().textPrimary());
            context.fill(new SLBounds(arrowCenterX - 1, arrowCenterY - 1, 3, 1), context.theme().colors().textPrimary());
        } else {
            context.fill(new SLBounds(arrowCenterX - 1, arrowCenterY - 1, 3, 1), context.theme().colors().textPrimary());
            context.fill(new SLBounds(arrowCenterX - 2, arrowCenterY, 5, 1), context.theme().colors().textPrimary());
            context.fill(new SLBounds(arrowCenterX - 3, arrowCenterY + 1, 7, 1), context.theme().colors().textPrimary());
        }
        
    }
    
    @Override
    protected void renderOverlay(SLRenderContext context) {
        if (!this.open || this.optionBounds.isEmpty()) return;
        int borderColor = this.isFocused() || this.isHovered()
                ? context.theme().colors().insetStrong()
                : context.theme().colors().insetBorder();
        context.fill(new SLBounds(this.popupBounds.x() - 2, this.popupBounds.y() - 2, this.popupBounds.width() + 4, this.popupBounds.height() + 4), 0x18000000);
        context.fillVerticalGradient(this.popupBounds, context.theme().colors().panelFillTop(), context.theme().colors().panelInset());
        context.outline(this.popupBounds, borderColor);
        
        int hoveredOption = this.optionIndexAt(context.mouseX(), context.mouseY());
        for (int optionIndex = 0; optionIndex < this.optionBounds.size(); optionIndex++) {
            SLBounds optionBounds = this.optionBounds.get(optionIndex);
            boolean selected = optionIndex == this.selectedIndex;
            boolean hovered = optionIndex == hoveredOption;
            
            if (selected || hovered) {
                int highlightTop = selected
                        ? SLColorUtils.lerp(0.22F, context.theme().accentColor(), context.theme().colors().white())
                        : SLColorUtils.lerp(0.12F, context.theme().colors().panelFillTop(), context.theme().accentColor());
                int highlightBottom = selected
                        ? SLColorUtils.lerp(0.42F, context.theme().colors().panelFillBottom(), context.theme().accentColor())
                        : context.theme().colors().panelInset();
                context.fillVerticalGradient(optionBounds, highlightTop, highlightBottom);
            }
            
            if (optionIndex > 0) {
                context.fill(new SLBounds(optionBounds.x() + 1, optionBounds.y(), Math.max(0, optionBounds.width() - 2), 1), 0x18000000);
            }
            
            int optionTextY = context.centeredVisualTextY(optionBounds);
            context.text(this.options.get(optionIndex), optionBounds.x() + 6, optionTextY, context.theme().labelColor(this.isEnabled()), false);
        }
    }
    
    @Override
    public @Nullable UINode<?> hitTestOverlay(double mouseX, double mouseY) {
        if (!this.open || !this.isVisible() || !this.isEnabled()) return null;
        return this.popupBounds.contains(mouseX, mouseY) ? this : null;
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isEnabled() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        
        if (this.triggerBounds.contains(event.x(), event.y())) {
            this.pressedTarget = SLDropdownNode.PRESSED_TRIGGER;
            this.setPressedState(true);
            return true;
        }
        
        int optionIndex = this.optionIndexAt(event.x(), event.y());
        if (optionIndex >= 0) {
            this.pressedTarget = optionIndex;
            this.setPressedState(true);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        int pressedTarget = this.pressedTarget;
        this.setPressedState(false);
        this.pressedTarget = -1;
        if (!wasPressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        
        if (pressedTarget == SLDropdownNode.PRESSED_TRIGGER && this.triggerBounds.contains(event.x(), event.y())) {
            this.setOpen(!this.open);
            return true;
        }
        if (pressedTarget >= 0 && pressedTarget == this.optionIndexAt(event.x(), event.y())) {
            this.setSelectedIndex(pressedTarget, true);
            this.setOpen(false);
            return true;
        }
        return true;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.isFocused() || !this.isEnabled() || this.options.isEmpty()) {
            return false;
        }
        
        return switch (event.key()) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_SPACE -> {
                this.setOpen(!this.open);
                yield true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                this.setOpen(false);
                yield true;
            }
            case GLFW.GLFW_KEY_UP -> {
                this.setSelectedIndex(this.selectedIndex <= 0 ? this.options.size() - 1 : this.selectedIndex - 1, true);
                yield true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                this.setSelectedIndex(this.selectedIndex >= this.options.size() - 1 ? 0 : this.selectedIndex + 1, true);
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                this.setSelectedIndex(0, true);
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                this.setSelectedIndex(this.options.size() - 1, true);
                yield true;
            }
            default -> false;
        };
    }
    
    ///
    /// Updates the expanded state and relayouts the control when needed.
    ///
    /// @param open new expanded state
    ///
    private void setOpen(boolean open) {
        boolean normalizedOpen = open && !this.options.isEmpty();
        if (this.open == normalizedOpen) return;
        
        this.open = normalizedOpen;
        this.invalidateLayout();
    }
    
    ///
    /// Applies a new selected index.
    ///
    /// @param selectedIndex requested selected index
    /// @param commit        whether to fire the commit callback
    ///
    @SuppressWarnings("DuplicatedCode")
    private void setSelectedIndex(int selectedIndex, boolean commit) {
        int normalizedIndex = this.normalizeIndex(selectedIndex);
        if (this.selectedIndex == normalizedIndex) {
            return;
        }
        
        this.selectedIndex = normalizedIndex;
        if (this.selectedIndexState != null) {
            this.selectedIndexState.set(normalizedIndex);
        }
        if (this.onValueChanged != null) {
            this.onValueChanged.accept(normalizedIndex);
        }
        if (commit && this.onCommit != null) {
            this.onCommit.accept(normalizedIndex);
        }
    }
    
    ///
    /// Resolves the option index under the given pointer location.
    ///
    /// @param mouseX pointer x position
    /// @param mouseY pointer y position
    ///
    /// @return option index, or {@code -1} when no option is hit
    ///
    private int optionIndexAt(double mouseX, double mouseY) {
        for (int optionIndex = 0; optionIndex < this.optionBounds.size(); optionIndex++) {
            if (this.optionBounds.get(optionIndex).contains(mouseX, mouseY)) {
                return optionIndex;
            }
        }
        return -1;
    }
    
    ///
    /// Clamps one selected index to the available option range.
    ///
    /// @param selectedIndex requested selected index
    ///
    /// @return normalized selected index
    ///
    private int normalizeIndex(int selectedIndex) {
        if (this.options.isEmpty()) {
            return -1;
        }
        return Mth.clamp(selectedIndex, 0, this.options.size() - 1);
    }
}
