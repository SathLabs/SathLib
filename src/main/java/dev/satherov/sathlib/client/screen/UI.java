package dev.satherov.sathlib.client.screen;

import dev.satherov.sathlib.client.screen.layout.SLAlignment;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLLength;
import dev.satherov.sathlib.client.screen.layout.SLScalar;
import dev.satherov.sathlib.client.screen.node.SLButtonNode;
import dev.satherov.sathlib.client.screen.node.SLColumnNode;
import dev.satherov.sathlib.client.screen.node.SLFlowNode;
import dev.satherov.sathlib.client.screen.node.SLLabelNode;
import dev.satherov.sathlib.client.screen.node.SLMenuSlotGridNode;
import dev.satherov.sathlib.client.screen.node.SLPanelNode;
import dev.satherov.sathlib.client.screen.node.SLProgressBarNode;
import dev.satherov.sathlib.client.screen.node.SLRowNode;
import dev.satherov.sathlib.client.screen.node.SLStackNode;
import dev.satherov.sathlib.client.screen.node.UIContainerNode;
import dev.satherov.sathlib.client.screen.node.UINode;
import dev.satherov.sathlib.client.screen.state.UIState;
import dev.satherov.sathlib.common.menu.SLMenu;
import dev.satherov.sathlib.common.menu.logic.SLSlotSemantic;
import dev.satherov.sathlib.common.menu.logic.SLSlotSemantics;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

///
/// Fluent builder DSL for SathLib retained-mode UI trees.
///
/// Builders are short-lived setup objects. They collect semantic layout and
/// widget configuration and then produce runtime {@link UINode} instances when
/// {@code build()} is called.
///
/// - keep screen code readable
/// - hide repetitive node setup
/// - expose the same semantic layout vocabulary everywhere
///
/// Add more builder entry points here as new built-in nodes are added to the
/// framework.
///
public final class UI {
    
    private UI() { }
    
    ///
    /// Creates a column builder.
    ///
    /// @return new column builder
    ///
    public static ColumnBuilder column() {
        return new ColumnBuilder();
    }
    
    ///
    /// Creates a row builder.
    ///
    /// @return new row builder
    ///
    public static RowBuilder row() {
        return new RowBuilder();
    }
    
    ///
    /// Creates a stack builder.
    ///
    /// @return new stack builder
    ///
    public static StackBuilder stack() {
        return new StackBuilder();
    }
    
    ///
    /// Creates a panel builder.
    ///
    /// @return new panel builder
    ///
    public static PanelBuilder panel() {
        return new PanelBuilder();
    }
    
    ///
    /// Creates a label builder.
    ///
    /// @return new label builder
    ///
    public static LabelBuilder label() {
        return new LabelBuilder();
    }
    
    ///
    /// Creates a button builder.
    ///
    /// @return new button builder
    ///
    public static ButtonBuilder button() {
        return new ButtonBuilder();
    }
    
    ///
    /// Creates a progress bar builder.
    ///
    /// @return new progress bar builder
    ///
    public static ProgressBarBuilder progressBar() {
        return new ProgressBarBuilder();
    }
    
    ///
    /// Creates a slot grid builder for one semantic slot group.
    ///
    /// @param menu     backing menu
    /// @param semantic semantic slot group to position
    ///
    /// @return new slot grid builder
    ///
    public static SlotGridBuilder slots(SLMenu menu, SLSlotSemantic semantic) {
        return new SlotGridBuilder(menu.getSlots(semantic));
    }
    
    ///
    /// Creates a single-slot builder for one semantic slot group.
    ///
    /// @param menu     backing menu
    /// @param semantic semantic slot group to position
    ///
    /// @return new slot grid builder
    ///
    public static SlotGridBuilder slot(SLMenu menu, SLSlotSemantic semantic) {
        return UI.slots(menu, semantic).columns(1);
    }
    
    ///
    /// Creates a standard 3x9 player inventory slot grid builder.
    ///
    /// @param menu backing menu
    ///
    /// @return new slot grid builder
    ///
    public static SlotGridBuilder playerInventory(SLMenu menu) {
        return UI.slots(menu, SLSlotSemantics.PLAYER_INVENTORY).columns(9);
    }
    
    ///
    /// Creates a standard 9-slot hotbar grid builder.
    ///
    /// @param menu backing menu
    ///
    /// @return new slot grid builder
    ///
    public static SlotGridBuilder hotbar(SLMenu menu) {
        return UI.slots(menu, SLSlotSemantics.PLAYER_HOTBAR).columns(9);
    }
    
    ///
    /// Shared builder base that applies semantic layout configuration.
    ///
    /// Concrete builders inherit from this type for one build pass.
    ///
    /// - store semantic layout properties
    /// - apply those properties to a runtime node during build
    ///
    /// Extend this class when adding new widget builders.
    ///
    /// @param <N> runtime node type built by this builder
    /// @param <B> concrete builder type used for fluent chaining
    ///
    public abstract static class Builder<N extends UINode<N>, B extends Builder<N, B>> {
        
        private SLLength width = SLLength.content();
        private SLLength height = SLLength.content();
        private SLInsets margin = SLInsets.zero();
        private @Nullable SLInsets padding;
        private SLAlignment horizontalAlignment = SLAlignment.START;
        private SLAlignment verticalAlignment = SLAlignment.START;
        private SLScalar offsetX = SLScalar.zero();
        private SLScalar offsetY = SLScalar.zero();
        
        protected Builder() { }
        
        ///
        /// Returns the concrete builder type for fluent chaining.
        ///
        /// @return concrete builder
        ///
        @SuppressWarnings("unchecked")
        protected final B self() {
            return (B) this;
        }
        
        ///
        /// Sets width behavior.
        ///
        /// @param width width behavior
        ///
        /// @return this builder
        ///
        public final B width(SLLength width) {
            this.width = width;
            return this.self();
        }
        
        ///
        /// Sets height behavior.
        ///
        /// @param height height behavior
        ///
        /// @return this builder
        ///
        public final B height(SLLength height) {
            this.height = height;
            return this.self();
        }
        
        ///
        /// Sets both dimensions.
        ///
        /// @param width  width behavior
        /// @param height height behavior
        ///
        /// @return this builder
        ///
        public final B size(SLLength width, SLLength height) {
            this.width = width;
            this.height = height;
            return this.self();
        }
        
        ///
        /// Fills both dimensions.
        ///
        /// @return this builder
        ///
        public final B fill() {
            this.width = SLLength.fill();
            this.height = SLLength.fill();
            return this.self();
        }
        
        ///
        /// Fills only the width.
        ///
        /// @return this builder
        ///
        public final B fillWidth() {
            this.width = SLLength.fill();
            return this.self();
        }
        
        ///
        /// Fills only the height.
        ///
        /// @return this builder
        ///
        public final B fillHeight() {
            this.height = SLLength.fill();
            return this.self();
        }
        
        ///
        /// Sets uniform fixed margin.
        ///
        /// @param pixels margin in pixels
        ///
        /// @return this builder
        ///
        public final B margin(int pixels) {
            this.margin = SLInsets.all(pixels);
            return this.self();
        }
        
        ///
        /// Sets semantic margin.
        ///
        /// @param margin semantic margin
        ///
        /// @return this builder
        ///
        public final B margin(SLInsets margin) {
            this.margin = margin;
            return this.self();
        }
        
        ///
        /// Sets uniform fixed padding.
        ///
        /// @param pixels padding in pixels
        ///
        /// @return this builder
        ///
        public final B padding(int pixels) {
            this.padding = SLInsets.all(pixels);
            return this.self();
        }
        
        ///
        /// Sets semantic padding.
        ///
        /// @param padding semantic padding
        ///
        /// @return this builder
        ///
        public final B padding(SLInsets padding) {
            this.padding = padding;
            return this.self();
        }
        
        ///
        /// Sets both alignments to the same value.
        ///
        /// @param alignment shared alignment
        ///
        /// @return this builder
        ///
        public final B align(SLAlignment alignment) {
            this.horizontalAlignment = alignment;
            this.verticalAlignment = alignment;
            return this.self();
        }
        
        ///
        /// Sets horizontal and vertical alignment independently.
        ///
        /// @param horizontalAlignment horizontal alignment
        /// @param verticalAlignment   vertical alignment
        ///
        /// @return this builder
        ///
        public final B align(SLAlignment horizontalAlignment, SLAlignment verticalAlignment) {
            this.horizontalAlignment = horizontalAlignment;
            this.verticalAlignment = verticalAlignment;
            return this.self();
        }
        
        ///
        /// Sets fixed offsets applied after alignment.
        ///
        /// @param x x offset in pixels
        /// @param y y offset in pixels
        ///
        /// @return this builder
        ///
        public final B offset(int x, int y) {
            this.offsetX = SLScalar.pixels(x);
            this.offsetY = SLScalar.pixels(y);
            return this.self();
        }
        
        ///
        /// Sets semantic offsets applied after alignment.
        ///
        /// @param offsetX x offset
        /// @param offsetY y offset
        ///
        /// @return this builder
        ///
        public final B offset(SLScalar offsetX, SLScalar offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            return this.self();
        }
        
        ///
        /// Applies the common layout properties to a runtime node.
        ///
        /// @param node runtime node
        ///
        protected final void applyCommon(N node) {
            node.size(this.width, this.height)
                    .margin(this.margin)
                    .align(this.horizontalAlignment, this.verticalAlignment)
                    .offset(this.offsetX, this.offsetY);
            if (this.padding != null) node.padding(this.padding);
        }
        
        ///
        /// Builds the runtime node.
        ///
        /// @return new runtime node
        ///
        public abstract N build();
    }
    
    ///
    /// Builder base for container nodes with child collections.
    ///
    /// Concrete container builders gather child nodes until build time.
    ///
    /// - store ordered children
    /// - apply child lists to runtime containers
    ///
    /// Extend this class for new container builder types.
    ///
    /// @param <N> runtime container node type built by this builder
    /// @param <B> concrete builder type used for fluent chaining
    ///
    public abstract static class ContainerBuilder<N extends UIContainerNode<N>, B extends ContainerBuilder<N, B>> extends Builder<N, B> {
        
        private final List<UINode<?>> children = new ArrayList<>();
        
        protected ContainerBuilder() { }
        
        ///
        /// Adds a runtime child node.
        ///
        /// @param child child node
        ///
        /// @return this builder
        ///
        public final B child(UINode<?> child) {
            this.children.add(child);
            return this.self();
        }
        
        ///
        /// Applies collected children to a runtime container.
        ///
        /// @param node runtime container
        ///
        protected final void applyChildren(N node) {
            for (UINode<?> child : this.children) {
                node.addChild(child);
            }
        }
    }
    
    ///
    /// Builder base for flow containers.
    ///
    /// Flow builders store gap and main-axis alignment until build time.
    ///
    /// - expose flow-specific semantic settings
    ///
    /// Extend this class for new flow builder variants.
    ///
    /// @param <N> runtime flow node type built by this builder
    /// @param <B> concrete builder type used for fluent chaining
    ///
    public abstract static class FlowBuilder<N extends SLFlowNode<N>, B extends FlowBuilder<N, B>> extends ContainerBuilder<N, B> {
        
        private SLScalar gap = SLScalar.zero();
        private SLAlignment mainAxisAlignment = SLAlignment.START;
        
        protected FlowBuilder() { }
        
        ///
        /// Sets a fixed child gap.
        ///
        /// @param pixels gap in pixels
        ///
        /// @return this builder
        ///
        public final B gap(int pixels) {
            this.gap = SLScalar.pixels(pixels);
            return this.self();
        }
        
        ///
        /// Sets a semantic child gap.
        ///
        /// @param gap semantic gap
        ///
        /// @return this builder
        ///
        public final B gap(SLScalar gap) {
            this.gap = gap;
            return this.self();
        }
        
        ///
        /// Sets main-axis alignment for the whole flow.
        ///
        /// @param alignment main-axis alignment
        ///
        /// @return this builder
        ///
        public final B mainAxisAlignment(SLAlignment alignment) {
            this.mainAxisAlignment = alignment;
            return this.self();
        }
        
        ///
        /// Applies flow-specific properties.
        ///
        /// @param node runtime flow container
        ///
        protected final void applyFlow(N node) {
            node.gap(this.gap).mainAxisAlignment(this.mainAxisAlignment);
        }
    }
    
    ///
    /// Builder for {@link SLColumnNode}.
    ///
    public static final class ColumnBuilder extends FlowBuilder<SLColumnNode, ColumnBuilder> {
        
        private ColumnBuilder() { }
        
        @Override
        public SLColumnNode build() {
            SLColumnNode node = new SLColumnNode();
            this.applyCommon(node);
            this.applyFlow(node);
            this.applyChildren(node);
            return node;
        }
    }
    
    ///
    /// Builder for {@link SLRowNode}.
    ///
    public static final class RowBuilder extends FlowBuilder<SLRowNode, RowBuilder> {
        
        private RowBuilder() { }
        
        @Override
        public SLRowNode build() {
            SLRowNode node = new SLRowNode();
            this.applyCommon(node);
            this.applyFlow(node);
            this.applyChildren(node);
            return node;
        }
    }
    
    ///
    /// Builder for {@link SLStackNode}.
    ///
    public static final class StackBuilder extends ContainerBuilder<SLStackNode, StackBuilder> {
        
        private StackBuilder() { }
        
        @Override
        public SLStackNode build() {
            SLStackNode node = new SLStackNode();
            this.applyCommon(node);
            this.applyChildren(node);
            return node;
        }
    }
    
    ///
    /// Builder for {@link SLPanelNode}.
    ///
    public static final class PanelBuilder extends FlowBuilder<SLPanelNode, PanelBuilder> {
        
        private PanelBuilder() { }
        
        @Override
        public SLPanelNode build() {
            SLPanelNode node = new SLPanelNode();
            this.applyCommon(node);
            this.applyFlow(node);
            this.applyChildren(node);
            return node;
        }
    }
    
    ///
    /// Builder for {@link SLLabelNode}.
    ///
    public static final class LabelBuilder extends Builder<SLLabelNode, LabelBuilder> {
        
        private Component text = Component.empty();
        private @Nullable Integer color;
        private boolean shadow;
        private @Nullable UIState<Component> textState;
        
        private LabelBuilder() { }
        
        ///
        /// Sets the label text.
        ///
        /// @param text label text
        ///
        /// @return this builder
        ///
        public LabelBuilder text(Component text) {
            this.text = text;
            return this;
        }
        
        ///
        /// Sets the label color.
        ///
        /// @param color ARGB text color
        ///
        /// @return this builder
        ///
        public LabelBuilder color(int color) {
            this.color = color;
            return this;
        }
        
        ///
        /// Enables or disables text shadow.
        ///
        /// @param shadow whether to render a shadow
        ///
        /// @return this builder
        ///
        public LabelBuilder shadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }
        
        ///
        /// Binds the label text to observable state.
        ///
        /// @param textState observable text state
        ///
        /// @return this builder
        ///
        public LabelBuilder bindText(UIState<Component> textState) {
            this.textState = textState;
            return this;
        }
        
        @Override
        public SLLabelNode build() {
            SLLabelNode node = new SLLabelNode()
                    .text(this.text)
                    .shadow(this.shadow);
            if (this.color != null) {
                node.color(this.color);
            }
            if (this.textState != null) {
                node.bindText(this.textState);
            }
            this.applyCommon(node);
            return node;
        }
    }
    
    ///
    /// Builder for {@link SLButtonNode}.
    ///
    public static final class ButtonBuilder extends Builder<SLButtonNode, ButtonBuilder> {
        
        private Component text = Component.empty();
        private @Nullable Consumer<SLButtonNode> onPress;
        private @Nullable UIState<Component> textState;
        private @Nullable UIState<Boolean> enabledState;
        
        private ButtonBuilder() { }
        
        ///
        /// Sets the button label.
        ///
        /// @param text button label
        ///
        /// @return this builder
        ///
        public ButtonBuilder text(Component text) {
            this.text = text;
            return this;
        }
        
        ///
        /// Sets the button activation callback.
        ///
        /// @param onPress activation callback
        ///
        /// @return this builder
        ///
        public ButtonBuilder onPress(Consumer<SLButtonNode> onPress) {
            this.onPress = onPress;
            return this;
        }
        
        ///
        /// Binds the button label to observable state.
        ///
        /// @param textState observable label state
        ///
        /// @return this builder
        ///
        public ButtonBuilder bindText(UIState<Component> textState) {
            this.textState = textState;
            return this;
        }
        
        ///
        /// Binds the enabled flag to observable state.
        ///
        /// @param enabledState observable enabled state
        ///
        /// @return this builder
        ///
        public ButtonBuilder bindEnabled(UIState<Boolean> enabledState) {
            this.enabledState = enabledState;
            return this;
        }
        
        @Override
        public SLButtonNode build() {
            SLButtonNode node = new SLButtonNode().text(this.text);
            if (this.onPress != null) node.onPress(this.onPress);
            if (this.textState != null) node.bindText(this.textState);
            if (this.enabledState != null) node.bindEnabled(this.enabledState);
            this.applyCommon(node);
            return node;
        }
    }
    
    ///
    /// Builder for {@link SLProgressBarNode}.
    ///
    public static final class ProgressBarBuilder extends Builder<SLProgressBarNode, ProgressBarBuilder> {
        
        private float progress;
        private @Nullable Component overlay;
        private @Nullable UIState<Float> progressState;
        private @Nullable UIState<Component> overlayState;
        
        private ProgressBarBuilder() { }
        
        ///
        /// Sets the progress value.
        ///
        /// @param progress normalized progress value
        ///
        /// @return this builder
        ///
        public ProgressBarBuilder progress(float progress) {
            this.progress = progress;
            return this;
        }
        
        ///
        /// Sets overlay text.
        ///
        /// @param overlay overlay text, or {@code null}
        ///
        /// @return this builder
        ///
        public ProgressBarBuilder overlay(@Nullable Component overlay) {
            this.overlay = overlay;
            return this;
        }
        
        ///
        /// Binds progress to observable state.
        ///
        /// @param progressState observable progress state
        ///
        /// @return this builder
        ///
        public ProgressBarBuilder bindProgress(UIState<Float> progressState) {
            this.progressState = progressState;
            return this;
        }
        
        ///
        /// Binds overlay text to observable state.
        ///
        /// @param overlayState observable overlay text state
        ///
        /// @return this builder
        ///
        public ProgressBarBuilder bindOverlay(UIState<Component> overlayState) {
            this.overlayState = overlayState;
            return this;
        }
        
        @Override
        public SLProgressBarNode build() {
            SLProgressBarNode node = new SLProgressBarNode().progress(this.progress).overlay(this.overlay);
            if (this.progressState != null) node.bindProgress(this.progressState);
            if (this.overlayState != null) node.bindOverlay(this.overlayState);
            this.applyCommon(node);
            return node;
        }
    }
    
    ///
    /// Builder for {@link SLMenuSlotGridNode}.
    ///
    public static final class SlotGridBuilder extends Builder<SLMenuSlotGridNode, SlotGridBuilder> {
        
        private final List<Slot> slots;
        private int columns = 1;
        private SLScalar gap = SLScalar.zero();
        
        private SlotGridBuilder(List<Slot> slots) {
            this.slots = slots;
        }
        
        ///
        /// Sets the grid column count.
        ///
        /// @param columns grid column count
        ///
        /// @return this builder
        ///
        public SlotGridBuilder columns(int columns) {
            this.columns = Math.max(1, columns);
            return this;
        }
        
        ///
        /// Sets a fixed gap between slot frames.
        ///
        /// @param pixels extra slot gap
        ///
        /// @return this builder
        ///
        public SlotGridBuilder gap(int pixels) {
            this.gap = SLScalar.pixels(pixels);
            return this;
        }
        
        ///
        /// Sets a semantic gap between slot frames.
        ///
        /// @param gap semantic gap
        ///
        /// @return this builder
        ///
        public SlotGridBuilder gap(SLScalar gap) {
            this.gap = gap;
            return this;
        }
        
        @Override
        public SLMenuSlotGridNode build() {
            SLMenuSlotGridNode node = new SLMenuSlotGridNode(this.slots, this.columns);
            this.applyCommon(node);
            node.gap(this.gap);
            return node;
        }
    }
}
