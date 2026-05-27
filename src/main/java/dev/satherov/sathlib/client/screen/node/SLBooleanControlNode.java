package dev.satherov.sathlib.client.screen.node;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.state.UIState;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

///
/// Shared state and input behavior for built-in boolean controls.
///
/// Subclasses provide their own measurement and rendering while reusing one
/// retained-mode implementation for checked state, bindings, and activation.
///
/// @param <S> concrete control subtype
///
abstract class SLBooleanControlNode<S extends SLBooleanControlNode<S>> extends UILeafNode<S> {
    
    private Component text;
    private boolean checked;
    private @Nullable UIState<Boolean> checkedState;
    private @Nullable Consumer<Boolean> onValueChanged;
    private @Nullable Consumer<Boolean> onCommit;
    private @Nullable Runnable unsubscribeCheckedState;
    
    ///
    /// Creates a boolean control with explicit configuration.
    ///
    /// @param modifier       node modifier
    /// @param text           label text
    /// @param checked        initial checked state
    /// @param checkedState   optional observable checked binding
    /// @param onValueChanged optional live-change callback
    /// @param onCommit       optional commit callback
    ///
    protected SLBooleanControlNode(
            SLModifier modifier,
            Component text,
            boolean checked,
            @Nullable UIState<Boolean> checkedState,
            @Nullable Consumer<Boolean> onValueChanged,
            @Nullable Consumer<Boolean> onCommit
    ) {
        super(modifier);
        this.text = Objects.requireNonNullElse(text, Component.empty());
        this.checked = checked;
        this.checkedState = checkedState;
        this.onValueChanged = onValueChanged;
        this.onCommit = onCommit;
        
        if (checkedState != null) {
            this.checked = Boolean.TRUE.equals(checkedState.get());
        }
    }
    
    ///
    /// Returns the label text.
    ///
    /// @return current label text
    ///
    protected final Component text() {
        return this.text;
    }
    
    ///
    /// Returns whether the control is currently checked.
    ///
    /// @return checked flag
    ///
    protected final boolean checked() {
        return this.checked;
    }
    
    ///
    /// Sets the control label.
    ///
    /// @param text new label text
    ///
    /// @return this control
    ///
    public final S text(Component text) {
        this.text = Objects.requireNonNullElse(text, Component.empty());
        this.invalidateLayout();
        return this.self();
    }
    
    ///
    /// Sets the checked state without requiring external bindings.
    ///
    /// @param checked new checked state
    ///
    /// @return this control
    ///
    public final S checked(boolean checked) {
        this.applyCheckedState(checked, true, false);
        return this.self();
    }
    
    ///
    /// Binds the checked state to observable data.
    ///
    /// @param checkedState observable checked state
    ///
    /// @return this control
    ///
    public final S bindChecked(UIState<Boolean> checkedState) {
        this.checkedState = Objects.requireNonNull(checkedState);
        this.checked = Boolean.TRUE.equals(checkedState.get());
        return this.self();
    }
    
    ///
    /// Sets the live-change callback.
    ///
    /// @param onValueChanged callback invoked after state changes
    ///
    /// @return this control
    ///
    public final S onValueChanged(@Nullable Consumer<Boolean> onValueChanged) {
        this.onValueChanged = onValueChanged;
        return this.self();
    }
    
    ///
    /// Sets the commit callback.
    ///
    /// @param onCommit callback invoked after one activation completes
    ///
    /// @return this control
    ///
    public final S onCommit(@Nullable Consumer<Boolean> onCommit) {
        this.onCommit = onCommit;
        return this.self();
    }
    
    @Override
    protected final boolean isInputTarget() {
        return true;
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        if (this.checkedState != null) {
            this.unsubscribeCheckedState = this.checkedState.listen(value -> this.checked = Boolean.TRUE.equals(value));
        }
    }
    
    @Override
    protected void onDetached() {
        if (this.unsubscribeCheckedState != null) {
            this.unsubscribeCheckedState.run();
            this.unsubscribeCheckedState = null;
        }
    }
    
    @Override
    public final boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isEnabled() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public final boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        this.setPressedState(false);
        if (!wasPressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        
        if (this.getBounds().contains(event.x(), event.y())) {
            this.toggleFromInput();
        }
        return true;
    }
    
    @Override
    public final boolean keyPressed(KeyEvent event) {
        if (!this.isFocused() || !this.isEnabled()) {
            return false;
        }
        
        return switch (event.key()) {
            case GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                this.toggleFromInput();
                yield true;
            }
            default -> false;
        };
    }
    
    ///
    /// Applies a new checked value.
    ///
    /// @param checked notify value
    /// @param notify  whether to fire the live-change callback
    /// @param commit  whether to fire the commit callback
    ///
    protected final void applyCheckedState(boolean checked, boolean notify, boolean commit) {
        if (this.checked == checked) {
            return;
        }
        
        this.checked = checked;
        if (this.checkedState != null) {
            this.checkedState.set(checked);
        }
        if (notify && this.onValueChanged != null) {
            this.onValueChanged.accept(checked);
        }
        if (commit && this.onCommit != null) {
            this.onCommit.accept(checked);
        }
    }
    
    ///
    /// Toggles the current checked value due to direct user input.
    ///
    private void toggleFromInput() {
        this.applyCheckedState(!this.checked, true, true);
    }
}
