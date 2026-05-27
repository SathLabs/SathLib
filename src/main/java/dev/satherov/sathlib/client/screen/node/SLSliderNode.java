package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.layout.SLAxis;
import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.state.UIState;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

///
/// Built-in themed slider node with horizontal or vertical orientation.
///
/// The generic node owns a normalized value in {@code [0, 1]} by default, but
/// subclasses can override the value accessors to bind the slider to custom
/// models while reusing the shared pointer handling and handle rendering.
///
public class SLSliderNode extends UILeafNode<SLSliderNode> {
    
    private static final int DEFAULT_HORIZONTAL_WIDTH = 96;
    private static final int DEFAULT_HORIZONTAL_HEIGHT = 14;
    private static final int DEFAULT_VERTICAL_WIDTH = 14;
    private static final int DEFAULT_VERTICAL_HEIGHT = 96;
    private static final int HANDLE_SIZE = 6;
    private static final float KEYBOARD_STEP = 0.05F;
    private static final float FINE_KEYBOARD_STEP = 0.01F;
    private static final float SCROLL_STEP = 0.05F;
    
    private SLAxis axis;
    private boolean inverted;
    private float value;
    private @Nullable UIState<Float> valueState;
    private @Nullable Consumer<Float> onValueChanged;
    private @Nullable Consumer<Float> onCommit;
    
    ///
    /// Creates a horizontal slider with default settings.
    ///
    public SLSliderNode() {
        this(SLModifier.none(), SLAxis.HORIZONTAL, false, 0.0F, null, null, null);
    }
    
    ///
    /// Creates a fully configured slider.
    ///
    /// @param modifier       node modifier
    /// @param axis           slider axis
    /// @param inverted       whether the logical minimum is rendered at the end
    ///                       of the axis instead of the start
    /// @param value          initial normalized value
    /// @param valueState     optional external value state
    /// @param onValueChanged optional live change callback
    /// @param onCommit       optional commit callback fired on mouse release
    ///
    protected SLSliderNode(
            SLModifier modifier,
            SLAxis axis,
            boolean inverted,
            float value,
            @Nullable UIState<Float> valueState,
            @Nullable Consumer<Float> onValueChanged,
            @Nullable Consumer<Float> onCommit
    ) {
        super(modifier);
        this.axis = Objects.requireNonNull(axis);
        this.inverted = inverted;
        this.value = Mth.clamp(value, 0.0F, 1.0F);
        this.valueState = valueState;
        this.onValueChanged = onValueChanged;
        this.onCommit = onCommit;
    }
    
    ///
    /// Creates a builder-backed slider while normalizing omitted values to the
    /// framework defaults.
    ///
    /// @param modifier       node modifier
    /// @param axis           slider axis
    /// @param inverted       whether the logical minimum is rendered at the end
    ///                       of the axis instead of the start
    /// @param value          initial normalized value
    /// @param valueState     optional external value state
    /// @param onValueChanged optional live change callback
    /// @param onCommit       optional commit callback fired on mouse release
    ///
    /// @return configured slider node
    ///
    @Builder(builderMethodName = "sliderBuilder")
    public static SLSliderNode of(
            SLModifier modifier,
            SLAxis axis,
            Boolean inverted,
            Float value,
            UIState<Float> valueState,
            Consumer<Float> onValueChanged,
            Consumer<Float> onCommit
    ) {
        return new SLSliderNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(axis, SLAxis.HORIZONTAL),
                Objects.requireNonNullElse(inverted, false),
                Objects.requireNonNullElse(value, 0.0F),
                valueState,
                onValueChanged,
                onCommit
        );
    }
    
    ///
    /// Returns the current normalized slider value.
    ///
    /// @return current slider value in {@code [0, 1]}
    ///
    public final float getValue() {
        return Mth.clamp(this.getSliderFraction(), 0.0F, 1.0F);
    }
    
    ///
    /// Returns the slider axis.
    ///
    /// @return slider axis
    ///
    public final SLAxis getAxis() {
        return this.axis;
    }
    
    ///
    /// Returns whether the slider is inverted along its axis.
    ///
    /// @return {@code true} when inverted
    ///
    public final boolean isInverted() {
        return this.inverted;
    }
    
    ///
    /// Sets the slider axis.
    ///
    /// @param axis new axis
    ///
    /// @return this slider
    ///
    public SLSliderNode axis(SLAxis axis) {
        SLAxis normalizedAxis = Objects.requireNonNullElse(axis, SLAxis.HORIZONTAL);
        if (this.axis == normalizedAxis) return this;
        
        this.axis = normalizedAxis;
        this.invalidateLayout();
        return this;
    }
    
    ///
    /// Sets whether the slider is inverted along its axis.
    ///
    /// @param inverted new inversion flag
    ///
    /// @return this slider
    ///
    public SLSliderNode inverted(boolean inverted) {
        this.inverted = inverted;
        return this;
    }
    
    ///
    /// Sets the slider value without firing callbacks.
    ///
    /// @param value new normalized value
    ///
    /// @return this slider
    ///
    public SLSliderNode setValueSilently(float value) {
        this.writeSliderFraction(Mth.clamp(value, 0.0F, 1.0F));
        return this;
    }
    
    ///
    /// Sets the slider value and fires the live-change callback when it
    /// changed.
    ///
    /// @param value new normalized value
    ///
    /// @return this slider
    ///
    public SLSliderNode value(float value) {
        this.updateValue(value, true);
        return this;
    }
    
    ///
    /// Binds the slider to external state.
    ///
    /// @param valueState external slider state
    ///
    /// @return this slider
    ///
    public SLSliderNode bindValue(UIState<Float> valueState) {
        this.valueState = Objects.requireNonNull(valueState);
        return this;
    }
    
    ///
    /// Sets the live-change callback.
    ///
    /// @param onValueChanged callback invoked after each value change
    ///
    /// @return this slider
    ///
    public SLSliderNode onValueChanged(@Nullable Consumer<Float> onValueChanged) {
        this.onValueChanged = onValueChanged;
        return this;
    }
    
    ///
    /// Sets the commit callback.
    ///
    /// @param onCommit callback invoked on mouse release
    ///
    /// @return this slider
    ///
    public SLSliderNode onCommit(@Nullable Consumer<Float> onCommit) {
        this.onCommit = onCommit;
        return this;
    }
    
    @Override
    protected boolean isInputTarget() {
        return true;
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        if (this.axis == SLAxis.VERTICAL) return new SLMeasuredSize(SLSliderNode.DEFAULT_VERTICAL_WIDTH, SLSliderNode.DEFAULT_VERTICAL_HEIGHT);
        return new SLMeasuredSize(SLSliderNode.DEFAULT_HORIZONTAL_WIDTH, SLSliderNode.DEFAULT_HORIZONTAL_HEIGHT);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        SLBounds bounds = this.getBounds();
        SLBounds trackBounds = this.trackBounds(bounds);
        float value = this.getValue();
        context.fill(
                new SLBounds(bounds.x() + 1, bounds.y() + 1, bounds.width(), bounds.height()),
                dev.satherov.sathlib.util.SLColorUtils.multiplyAlpha(context.theme().colors().panelShadow(), 0.85F)
        );
        context.fillVerticalGradient(
                bounds,
                dev.satherov.sathlib.util.SLColorUtils.lerp(0.18F, context.theme().colors().insetFill(), context.theme().colors().panelFillTop()),
                context.theme().colors().insetFill()
        );
        this.renderTrack(context, trackBounds, value);
        context.outline(bounds, this.isHovered() || this.isPressed() ? context.theme().colors().insetStrong() : context.theme().colors().insetBorder());
        this.renderHandle(context, bounds, trackBounds, value);
    }
    
    @Override
    public boolean mousePressed(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isEnabled() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        this.updatePointerValue(event.x(), event.y(), true);
        this.setPressedState(true);
        return true;
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (!this.isEnabled() || !this.isPressed() || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        this.updatePointerValue(event.x(), event.y(), true);
        return true;
    }
    
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasPressed = this.isPressed();
        this.setPressedState(false);
        if (!wasPressed || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        
        this.updatePointerValue(event.x(), event.y(), true);
        this.commitCurrentValue();
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isEnabled() || scrollY == 0.0D) return false;
        
        float delta = scrollY > 0.0D ? SLSliderNode.SCROLL_STEP : -SLSliderNode.SCROLL_STEP;
        this.updateValue(this.getValue() + delta, true);
        this.commitCurrentValue();
        return true;
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.isEnabled()) return false;
        
        float step = event.hasShiftDown() ? SLSliderNode.FINE_KEYBOARD_STEP : SLSliderNode.KEYBOARD_STEP;
        float delta = switch (event.key()) {
            case GLFW.GLFW_KEY_LEFT -> this.axis == SLAxis.HORIZONTAL ? -step : 0.0F;
            case GLFW.GLFW_KEY_RIGHT -> this.axis == SLAxis.HORIZONTAL ? step : 0.0F;
            case GLFW.GLFW_KEY_UP -> this.axis == SLAxis.VERTICAL ? -step : 0.0F;
            case GLFW.GLFW_KEY_DOWN -> this.axis == SLAxis.VERTICAL ? step : 0.0F;
            default -> Float.NaN;
        };
        
        if (!Float.isNaN(delta)) {
            this.updateValue(this.getValue() + (this.inverted ? -delta : delta), true);
            this.commitCurrentValue();
            return true;
        }
        
        return switch (event.key()) {
            case GLFW.GLFW_KEY_HOME -> {
                this.updateValue(0.0F, true);
                this.commitCurrentValue();
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                this.updateValue(1.0F, true);
                this.commitCurrentValue();
                yield true;
            }
            default -> false;
        };
    }
    
    ///
    /// Returns the raw backing value used by this slider.
    ///
    /// Subclasses can override this to bind the generic slider interaction to
    /// custom state or view models.
    ///
    /// @return raw slider value
    ///
    protected float getSliderFraction() {
        if (this.valueState != null) {
            Float stateValue = this.valueState.get();
            if (stateValue != null) {
                return stateValue;
            }
        }
        return this.value;
    }
    
    ///
    /// Applies a normalized value to the slider's backing store.
    ///
    /// Subclasses can override this to forward writes into custom state or view
    /// models.
    ///
    /// @param value normalized slider value
    ///
    protected void writeSliderFraction(float value) {
        float normalizedValue = Mth.clamp(value, 0.0F, 1.0F);
        if (this.valueState != null) {
            this.valueState.set(normalizedValue);
            return;
        }
        this.value = normalizedValue;
    }
    
    ///
    /// Renders the inside of the slider track.
    ///
    /// Subclasses can override this to draw custom track gradients while
    /// keeping the shared outline, pointer handling, and handle styling.
    ///
    /// @param context     active render context
    /// @param trackBounds inner track bounds
    /// @param value       current normalized value
    ///
    protected void renderTrack(SLRenderContext context, SLBounds trackBounds, float value) {
        context.fillVerticalGradient(
                trackBounds,
                dev.satherov.sathlib.util.SLColorUtils.lerp(0.10F, context.theme().colors().panelFillBottom(), context.theme().colors().panelFillTop()),
                context.theme().colors().panelFillBottom()
        );
        SLBounds fillBounds = this.fillBounds(trackBounds, value);
        if (fillBounds.width() > 0 && fillBounds.height() > 0) {
            int fillTop = dev.satherov.sathlib.util.SLColorUtils.lerp(0.24F, context.theme().accentColor(), context.theme().colors().white());
            int fillBottom = dev.satherov.sathlib.util.SLColorUtils.lerp(0.42F, context.theme().colors().panelFillBottom(), context.theme().accentColor());
            context.fillVerticalGradient(fillBounds, fillTop, fillBottom);
            if (this.axis == SLAxis.HORIZONTAL) {
                context.fill(new SLBounds(fillBounds.x(), fillBounds.y(), fillBounds.width(), 1), 0x32FFFFFF);
            } else {
                context.fill(new SLBounds(fillBounds.x(), fillBounds.y(), 1, fillBounds.height()), 0x24FFFFFF);
            }
        }
    }
    
    ///
    /// Renders the draggable handle.
    ///
    /// @param context     active render context
    /// @param bounds      outer slider bounds
    /// @param trackBounds inner track bounds
    /// @param value       current normalized value
    ///
    protected void renderHandle(SLRenderContext context, SLBounds bounds, SLBounds trackBounds, float value) {
        float renderedValue = this.renderedValue(value);
        if (this.axis == SLAxis.HORIZONTAL) {
            int handleTravel = Math.max(0, bounds.width() - SLSliderNode.HANDLE_SIZE);
            int handleX = bounds.x() + Math.round(handleTravel * renderedValue);
            SLBounds handleBounds = new SLBounds(handleX, bounds.y() + 1, SLSliderNode.HANDLE_SIZE, Math.max(1, bounds.height() - 2));
            context.fillVerticalGradient(
                    handleBounds,
                    dev.satherov.sathlib.util.SLColorUtils.lerp(0.24F, context.theme().colors().panelInset(), context.theme().colors().white()),
                    context.theme().colors().panelInset()
            );
            context.outline(handleBounds, context.theme().colors().insetStrong());
            return;
        }
        
        int handleTravel = Math.max(0, bounds.height() - SLSliderNode.HANDLE_SIZE);
        int handleY = bounds.y() + Math.round(handleTravel * renderedValue);
        SLBounds handleBounds = new SLBounds(bounds.x() + 1, handleY, Math.max(1, bounds.width() - 2), SLSliderNode.HANDLE_SIZE);
        context.fillVerticalGradient(
                handleBounds,
                dev.satherov.sathlib.util.SLColorUtils.lerp(0.24F, context.theme().colors().panelInset(), context.theme().colors().white()),
                context.theme().colors().panelInset()
        );
        context.outline(handleBounds, context.theme().colors().insetStrong());
    }
    
    ///
    /// Returns the inner track bounds after the default one-pixel frame inset.
    ///
    /// @param bounds outer slider bounds
    ///
    /// @return inner track bounds
    ///
    protected final SLBounds trackBounds(SLBounds bounds) {
        return bounds.inset(1);
    }
    
    ///
    /// Resolves the rendered axis position for the current value.
    ///
    /// @param trackBounds inner track bounds
    /// @param value       current normalized value
    ///
    /// @return resolved x or y position depending on the slider axis
    ///
    protected final int sliderPosition(SLBounds trackBounds, float value) {
        float renderedValue = this.renderedValue(value);
        int trackLength = this.axis == SLAxis.HORIZONTAL ? trackBounds.width() : trackBounds.height();
        int offset = Math.round(Math.max(0, trackLength - 1) * renderedValue);
        return this.axis == SLAxis.HORIZONTAL ? trackBounds.x() + offset : trackBounds.y() + offset;
    }
    
    ///
    /// Returns the fill bounds for the generic themed slider track.
    ///
    /// @param trackBounds inner track bounds
    /// @param value       current normalized value
    ///
    /// @return accent-fill bounds
    ///
    protected final SLBounds fillBounds(SLBounds trackBounds, float value) {
        int fillLength = Math.round((this.axis == SLAxis.HORIZONTAL ? trackBounds.width() : trackBounds.height()) * Mth.clamp(value, 0.0F, 1.0F));
        if (fillLength <= 0) return SLBounds.EMPTY;
        
        if (this.axis == SLAxis.HORIZONTAL) {
            if (this.inverted) return new SLBounds(trackBounds.right() - fillLength, trackBounds.y(), fillLength, trackBounds.height());
            return new SLBounds(trackBounds.x(), trackBounds.y(), fillLength, trackBounds.height());
        }
        
        if (this.inverted) return new SLBounds(trackBounds.x(), trackBounds.bottom() - fillLength, trackBounds.width(), fillLength);
        return new SLBounds(trackBounds.x(), trackBounds.y(), trackBounds.width(), fillLength);
    }
    
    ///
    /// Updates the slider value from the current pointer location.
    ///
    /// @param mouseX pointer x
    /// @param mouseY pointer y
    /// @param notify whether to fire the live-change callback
    ///
    private void updatePointerValue(double mouseX, double mouseY, boolean notify) {
        SLBounds trackBounds = this.trackBounds(this.getBounds());
        double rawValue = this.axis == SLAxis.HORIZONTAL
                ? (mouseX - trackBounds.x()) / Math.max(1.0D, trackBounds.width() - 1.0D)
                : (mouseY - trackBounds.y()) / Math.max(1.0D, trackBounds.height() - 1.0D);
        float normalizedValue = Mth.clamp((float) rawValue, 0.0F, 1.0F);
        this.updateValue(this.inverted ? 1.0F - normalizedValue : normalizedValue, notify);
    }
    
    ///
    /// Applies a value change and optionally notifies the live-change callback.
    ///
    /// @param value  new normalized value
    /// @param notify whether to fire the live-change callback
    ///
    private void updateValue(float value, boolean notify) {
        float oldValue = this.getValue();
        float normalizedValue = Mth.clamp(value, 0.0F, 1.0F);
        this.writeSliderFraction(normalizedValue);
        float newValue = this.getValue();
        if (notify && this.onValueChanged != null && Float.compare(oldValue, newValue) != 0) {
            this.onValueChanged.accept(newValue);
        }
    }
    
    ///
    /// Fires the commit callback with the current value.
    ///
    private void commitCurrentValue() {
        if (this.onCommit != null) this.onCommit.accept(this.getValue());
    }
    
    ///
    /// Resolves the rendered fraction after inversion is applied.
    ///
    /// @param value logical slider value
    ///
    /// @return rendered fraction along the track
    ///
    private float renderedValue(float value) {
        return this.inverted ? 1.0F - Mth.clamp(value, 0.0F, 1.0F) : Mth.clamp(value, 0.0F, 1.0F);
    }
}
