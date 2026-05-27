package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.state.UIState;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

///
/// Built-in text label node.
///
/// Labels measure themselves from the active font, can optionally subscribe to a
/// state value when attached, and render plain text every frame.
///
public class SLLabelNode extends UILeafNode<SLLabelNode> {
    
    private Component text;
    private @Nullable Integer color;
    private boolean shadow;
    private @Nullable UIState<Component> textState;
    private @Nullable Runnable unsubscribeTextState;
    
    ///
    /// Creates an empty label node.
    ///
    public SLLabelNode() {
        this(SLModifier.none(), Component.empty(), null, false, null);
    }
    
    ///
    /// Creates a fully configured label.
    ///
    /// @param modifier  node modifier
    /// @param text      label text
    /// @param color     optional fixed text color
    /// @param shadow    whether to render a shadow
    /// @param textState optional observable text binding
    ///
    protected SLLabelNode(
            SLModifier modifier,
            Component text,
            @Nullable Integer color,
            boolean shadow,
            @Nullable UIState<Component> textState
    ) {
        super(modifier);
        this.text = Objects.requireNonNull(text);
        this.color = color;
        this.shadow = shadow;
        this.textState = textState;
        if (textState != null) {
            this.text = textState.get();
        }
    }
    
    ///
    /// Creates a builder-backed label while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param modifier  node modifier
    /// @param text      label text
    /// @param color     optional fixed text color
    /// @param shadow    whether to render a shadow
    /// @param textState optional observable text binding
    ///
    /// @return configured label node
    ///
    @Builder
    public static SLLabelNode of(
            SLModifier modifier,
            Component text,
            Integer color,
            Boolean shadow,
            UIState<Component> textState
    ) {
        return new SLLabelNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(text, Component.empty()),
                color,
                Objects.requireNonNullElse(shadow, false),
                textState
        );
    }
    
    ///
    /// Sets the displayed text.
    ///
    /// @param text new label text
    ///
    /// @return this label for fluent runtime setup
    ///
    public SLLabelNode text(Component text) {
        this.text = Objects.requireNonNullElse(text, Component.empty());
        this.invalidateLayout();
        return this;
    }
    
    ///
    /// Sets a fixed label color.
    ///
    /// @param color ARGB text color
    ///
    /// @return this label for fluent runtime setup
    ///
    public SLLabelNode color(int color) {
        this.color = color;
        return this;
    }
    
    ///
    /// Enables or disables text shadow.
    ///
    /// @param shadow whether to render a shadow
    ///
    /// @return this label for fluent runtime setup
    ///
    public SLLabelNode shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }
    
    ///
    /// Binds the label text to an observable state.
    ///
    /// @param textState observable text state
    ///
    /// @return this label for fluent runtime setup
    ///
    public SLLabelNode bindText(UIState<Component> textState) {
        this.textState = textState;
        this.text = textState.get();
        this.invalidateLayout();
        return this;
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        if (this.textState != null) {
            this.unsubscribeTextState = this.textState.listen(value -> {
                this.text = value;
                this.invalidateLayout();
            });
        }
    }
    
    @Override
    protected void onDetached() {
        if (this.unsubscribeTextState != null) {
            this.unsubscribeTextState.run();
            this.unsubscribeTextState = null;
        }
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        return new SLMeasuredSize(font.width(this.text), font.lineHeight);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        int textColor = this.color != null ? this.color : context.theme().labelColor(this.isEnabled());
        int textY = bounds.y() + ((bounds.height() - context.font().lineHeight) / 2);
        context.text(this.text, bounds.x(), textY, textColor, this.shadow);
    }
}
