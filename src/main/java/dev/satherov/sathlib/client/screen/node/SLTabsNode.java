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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

///
/// Compact retained-mode tab strip with one selected index.
///
public final class SLTabsNode extends UILeafNode<SLTabsNode> {
    
    private static final int TAB_HEIGHT = 20;
    private static final int TAB_MIN_WIDTH = 54;
    private static final int TAB_HORIZONTAL_PADDING = 10;
    
    private final List<Component> tabs;
    private int selectedIndex;
    private @Nullable UIState<Integer> selectedIndexState;
    private @Nullable Consumer<Integer> onValueChanged;
    private @Nullable Consumer<Integer> onCommit;
    private @Nullable Runnable unsubscribeSelectedIndexState;
    
    private List<SLBounds> tabBounds = List.of();
    private int pressedIndex = -1;
    
    ///
    /// Creates an empty tab strip.
    ///
    public SLTabsNode() {
        this(SLModifier.none(), List.of(), -1, null, null, null);
    }
    
    ///
    /// Creates a fully configured tab strip.
    ///
    /// @param modifier           node modifier
    /// @param tabs               tab labels
    /// @param selectedIndex      initially selected tab index
    /// @param selectedIndexState optional selected-index binding
    /// @param onValueChanged     optional live-change callback
    /// @param onCommit           optional commit callback
    ///
    private SLTabsNode(
            SLModifier modifier,
            List<Component> tabs,
            int selectedIndex,
            @Nullable UIState<Integer> selectedIndexState,
            @Nullable Consumer<Integer> onValueChanged,
            @Nullable Consumer<Integer> onCommit
    ) {
        super(modifier);
        this.tabs = List.copyOf(Objects.requireNonNullElse(tabs, List.of()));
        this.selectedIndex = this.normalizeIndex(selectedIndex);
        this.selectedIndexState = selectedIndexState;
        this.onValueChanged = onValueChanged;
        this.onCommit = onCommit;
        
        if (selectedIndexState != null) {
            this.selectedIndex = this.normalizeIndex(Objects.requireNonNullElse(selectedIndexState.get(), -1));
        }
    }
    
    ///
    /// Creates a builder-backed tab strip while normalizing omitted values to
    /// framework defaults.
    ///
    /// @param modifier           node modifier
    /// @param tabs               tab labels
    /// @param selectedIndex      initially selected tab index
    /// @param selectedIndexState optional selected-index binding
    /// @param onValueChanged     optional live-change callback
    /// @param onCommit           optional commit callback
    ///
    /// @return configured tab strip
    ///
    @Builder(builderMethodName = "builder")
    public static SLTabsNode of(
            SLModifier modifier,
            @Singular("tab") List<Component> tabs,
            Integer selectedIndex,
            UIState<Integer> selectedIndexState,
            Consumer<Integer> onValueChanged,
            Consumer<Integer> onCommit
    ) {
        return new SLTabsNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(tabs, List.of()),
                Objects.requireNonNullElse(selectedIndex, -1),
                selectedIndexState,
                onValueChanged,
                onCommit
        );
    }
    
    ///
    /// Returns the currently selected tab index.
    ///
    /// @return selected tab index, or {@code -1} when nothing is selected
    ///
    public int selectedIndex() {
        return this.selectedIndex;
    }
    
    ///
    /// Sets the selected tab index.
    ///
    /// @param selectedIndex new selected tab index
    ///
    /// @return this tab strip
    ///
    public SLTabsNode selectedIndex(int selectedIndex) {
        this.setSelectedIndex(selectedIndex, false);
        return this;
    }
    
    ///
    /// Binds the selected index to observable state.
    ///
    /// @param selectedIndexState observable selected-index state
    ///
    /// @return this tab strip
    ///
    public SLTabsNode bindSelectedIndex(UIState<Integer> selectedIndexState) {
        this.selectedIndexState = Objects.requireNonNull(selectedIndexState);
        this.selectedIndex = this.normalizeIndex(Objects.requireNonNullElse(selectedIndexState.get(), -1));
        return this;
    }
    
    ///
    /// Sets the live-change callback.
    ///
    /// @param onValueChanged callback invoked after the selected tab changes
    ///
    /// @return this tab strip
    ///
    public SLTabsNode onValueChanged(@Nullable Consumer<Integer> onValueChanged) {
        this.onValueChanged = onValueChanged;
        return this;
    }
    
    ///
    /// Sets the commit callback.
    ///
    /// @param onCommit callback invoked after one tab activation completes
    ///
    /// @return this tab strip
    ///
    public SLTabsNode onCommit(@Nullable Consumer<Integer> onCommit) {
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
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        if (this.tabs.isEmpty()) {
            return new SLMeasuredSize(SLTabsNode.TAB_MIN_WIDTH, SLTabsNode.TAB_HEIGHT);
        }
        
        int width = 0;
        for (Component tab : this.tabs) {
            width += Math.max(SLTabsNode.TAB_MIN_WIDTH, font.width(tab) + (SLTabsNode.TAB_HORIZONTAL_PADDING * 2));
        }
        return new SLMeasuredSize(width, SLTabsNode.TAB_HEIGHT);
    }
    
    @Override
    protected void onLayout(Font font, SLBounds contentBounds) {
        if (this.tabs.isEmpty()) {
            this.tabBounds = List.of();
            return;
        }
        
        List<Integer> tabWidths = new ArrayList<>(this.tabs.size());
        int preferredWidth = 0;
        for (Component tab : this.tabs) {
            int width = Math.max(SLTabsNode.TAB_MIN_WIDTH, font.width(tab) + (SLTabsNode.TAB_HORIZONTAL_PADDING * 2));
            tabWidths.add(width);
            preferredWidth += width;
        }
        
        List<SLBounds> computedBounds = this.computeBounds(contentBounds, preferredWidth, tabWidths);
        this.tabBounds = List.copyOf(computedBounds);
    }
    
    private @NonNull List<SLBounds> computeBounds(SLBounds contentBounds, int preferredWidth, List<Integer> tabWidths) {
        int extraWidth = Math.max(0, contentBounds.width() - preferredWidth);
        int sharedWidth = extraWidth / this.tabs.size();
        int remainder = extraWidth % this.tabs.size();
        
        List<SLBounds> computedBounds = new ArrayList<>(this.tabs.size());
        int x = contentBounds.x();
        for (int tabIndex = 0; tabIndex < this.tabs.size(); tabIndex++) {
            int width = tabWidths.get(tabIndex) + sharedWidth + (tabIndex < remainder ? 1 : 0);
            computedBounds.add(new SLBounds(x, contentBounds.y(), width, contentBounds.height()));
            x += width;
        }
        return computedBounds;
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        if (this.tabs.isEmpty()) {
            context.fill(this.getBounds(), context.theme().colors().insetFill());
            context.outline(this.getBounds(), context.theme().colors().insetBorder());
            return;
        }
        
        int hoveredIndex = this.tabIndexAt(context.mouseX(), context.mouseY());
        for (int tabIndex = 0; tabIndex < this.tabs.size(); tabIndex++) {
            SLBounds tabBounds = this.tabBounds.get(tabIndex);
            boolean selected = tabIndex == this.selectedIndex;
            boolean hovered = tabIndex == hoveredIndex;
            boolean pressed = tabIndex == this.pressedIndex && this.isPressed();
            int topColor;
            int bottomColor;
            int borderColor = selected || hovered
                    ? context.theme().colors().insetStrong()
                    : context.theme().colors().insetBorder();
            
            if (selected) {
                topColor = SLColorUtils.lerp(0.24F, context.theme().accentColor(), context.theme().colors().white());
                bottomColor = SLColorUtils.lerp(0.44F, context.theme().colors().panelFillBottom(), context.theme().accentColor());
            } else if (pressed) {
                topColor = SLColorUtils.lerp(0.18F, context.theme().colors().panelFillBottom(), context.theme().colors().panelInset());
                bottomColor = context.theme().colors().panelFillBottom();
            } else if (hovered) {
                topColor = SLColorUtils.lerp(0.14F, context.theme().colors().panelFillTop(), context.theme().accentColor());
                bottomColor = context.theme().colors().panelInset();
            } else {
                topColor = context.theme().colors().panelFillTop();
                bottomColor = context.theme().colors().panelInset();
            }
            
            context.fillVerticalGradient(tabBounds, topColor, bottomColor);
            context.outline(tabBounds, borderColor);
            context.centeredVisualText(this.tabs.get(tabIndex), tabBounds, context.theme().labelColor(this.isEnabled()), false);
        }
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isEnabled() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        
        int tabIndex = this.tabIndexAt(event.x(), event.y());
        if (tabIndex < 0) return false;
        
        this.pressedIndex = tabIndex;
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        int pressedIndex = this.pressedIndex;
        this.setPressedState(false);
        this.pressedIndex = -1;
        if (!wasPressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        
        if (pressedIndex >= 0 && pressedIndex == this.tabIndexAt(event.x(), event.y())) {
            this.setSelectedIndex(pressedIndex, true);
        }
        return true;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.isFocused() || !this.isEnabled() || this.tabs.isEmpty()) return false;
        
        return switch (event.key()) {
            case GLFW.GLFW_KEY_LEFT -> {
                this.setSelectedIndex(this.selectedIndex <= 0 ? this.tabs.size() - 1 : this.selectedIndex - 1, true);
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                this.setSelectedIndex(this.selectedIndex >= this.tabs.size() - 1 ? 0 : this.selectedIndex + 1, true);
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                this.setSelectedIndex(0, true);
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                this.setSelectedIndex(this.tabs.size() - 1, true);
                yield true;
            }
            default -> false;
        };
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
        if (this.selectedIndex == normalizedIndex) return;
        
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
    /// Returns the tab index under the given position.
    ///
    /// @param mouseX pointer x position
    /// @param mouseY pointer y position
    ///
    /// @return tab index, or {@code -1} when no tab is hit
    ///
    private int tabIndexAt(double mouseX, double mouseY) {
        for (int tabIndex = 0; tabIndex < this.tabBounds.size(); tabIndex++) {
            if (this.tabBounds.get(tabIndex).contains(mouseX, mouseY)) {
                return tabIndex;
            }
        }
        return -1;
    }
    
    ///
    /// Clamps one selected index to the available tab range.
    ///
    /// @param selectedIndex requested selected index
    ///
    /// @return normalized selected index
    ///
    private int normalizeIndex(int selectedIndex) {
        if (this.tabs.isEmpty()) {
            return -1;
        }
        return Mth.clamp(selectedIndex, 0, this.tabs.size() - 1);
    }
}
