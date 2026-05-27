package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLInsets;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.state.UIState;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

///
/// Built-in clickable button node.
///
/// Buttons measure themselves from their label, optionally bind text or enabled
/// state, react to mouse input, and delegate their visuals to the active theme.
///
public class SLButtonNode extends UILeafNode<SLButtonNode> {
    
    private Component text;
    private @Nullable Consumer<SLButtonNode> onPress;
    private @Nullable UIState<Component> textState;
    private @Nullable UIState<Boolean> enabledState;
    private @Nullable Runnable unsubscribeTextState;
    private @Nullable Runnable unsubscribeEnabledState;
    
    ///
    /// Creates a button node with the default built-in padding.
    ///
    public SLButtonNode() {
        this(SLModifier.none().withPadding(SLInsets.symmetric(10, 5)), Component.empty(), null, null, null);
    }
    
    ///
    /// Creates a fully configured button.
    ///
    /// @param modifier     node modifier
    /// @param text         button text
    /// @param onPress      optional activation callback
    /// @param textState    optional observable text binding
    /// @param enabledState optional observable enabled binding
    ///
    protected SLButtonNode(
            SLModifier modifier,
            Component text,
            @Nullable Consumer<SLButtonNode> onPress,
            @Nullable UIState<Component> textState,
            @Nullable UIState<Boolean> enabledState
    ) {
        super(modifier);
        this.text = Objects.requireNonNull(text);
        this.onPress = onPress;
        this.textState = textState;
        this.enabledState = enabledState;
        
        if (textState != null) {
            this.text = textState.get();
        }
        if (enabledState != null) {
            this.setEnabled(enabledState.get());
        }
    }
    
    ///
    /// Creates a builder-backed button while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param modifier     node modifier
    /// @param text         button text
    /// @param onPress      optional activation callback
    /// @param textState    optional observable text binding
    /// @param enabledState optional observable enabled binding
    ///
    /// @return configured button node
    ///
    @Builder
    public static SLButtonNode of(
            SLModifier modifier,
            Component text,
            Consumer<SLButtonNode> onPress,
            UIState<Component> textState,
            UIState<Boolean> enabledState
    ) {
        return new SLButtonNode(
                Objects.requireNonNullElse(modifier, SLModifier.none().withPadding(SLInsets.symmetric(10, 5))),
                Objects.requireNonNullElse(text, Component.empty()),
                onPress,
                textState,
                enabledState
        );
    }
    
    ///
    /// Sets the button label.
    ///
    /// @param text new button label
    ///
    /// @return this button for fluent runtime setup
    ///
    public SLButtonNode text(Component text) {
        this.text = Objects.requireNonNullElse(text, Component.empty());
        this.invalidateLayout();
        return this;
    }
    
    ///
    /// Sets the button callback.
    ///
    /// @param onPress callback invoked when activated
    ///
    /// @return this button for fluent runtime setup
    ///
    public SLButtonNode onPress(Consumer<SLButtonNode> onPress) {
        this.onPress = onPress;
        return this;
    }
    
    ///
    /// Binds the button label to observable state.
    ///
    /// @param textState observable text state
    ///
    /// @return this button for fluent runtime setup
    ///
    public SLButtonNode bindText(UIState<Component> textState) {
        this.textState = textState;
        this.text = textState.get();
        this.invalidateLayout();
        return this;
    }
    
    ///
    /// Binds the button enabled flag to observable state.
    ///
    /// @param enabledState observable enabled state
    ///
    /// @return this button for fluent runtime setup
    ///
    public SLButtonNode bindEnabled(UIState<Boolean> enabledState) {
        this.enabledState = enabledState;
        this.setEnabled(enabledState.get());
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
        if (this.enabledState != null) {
            this.unsubscribeEnabledState = this.enabledState.listen(this::setEnabled);
        }
    }
    
    @Override
    protected void onDetached() {
        if (this.unsubscribeTextState != null) {
            this.unsubscribeTextState.run();
            this.unsubscribeTextState = null;
        }
        if (this.unsubscribeEnabledState != null) {
            this.unsubscribeEnabledState.run();
            this.unsubscribeEnabledState = null;
        }
    }
    
    @Override
    protected boolean isInputTarget() {
        return true;
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        return new SLMeasuredSize(font.width(this.text), font.lineHeight);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        context.theme().renderButton(context, this.getBounds(), this.text, this.isHovered(), this.isPressed(), this.isEnabled());
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isEnabled() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        this.setPressedState(false);
        if (!wasPressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        
        if (this.getBounds().contains(event.x(), event.y()) && this.isEnabled() && this.onPress != null) {
            this.onPress.accept(this);
        }
        return true;
    }
}
