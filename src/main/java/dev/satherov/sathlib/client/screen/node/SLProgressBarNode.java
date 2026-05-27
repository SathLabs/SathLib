package dev.satherov.sathlib.client.screen.node;

import lombok.Builder;

import dev.satherov.sathlib.client.screen.UIRoot;
import dev.satherov.sathlib.client.screen.layout.SLMeasuredSize;
import dev.satherov.sathlib.client.screen.layout.SLModifier;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.client.screen.state.UIState;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

///
/// Built-in themed progress bar node.
///
/// Progress bars can be configured directly or bound to observable state and are
/// rendered every frame without requiring manual drawing code in the screen.
///
public class SLProgressBarNode extends UILeafNode<SLProgressBarNode> {
    
    private float progress;
    private @Nullable Component overlayText;
    private @Nullable UIState<Float> progressState;
    private @Nullable UIState<Component> overlayTextState;
    private @Nullable Runnable unsubscribeProgressState;
    private @Nullable Runnable unsubscribeOverlayState;
    
    ///
    /// Creates an unbound progress bar node.
    ///
    public SLProgressBarNode() {
        this(SLModifier.none(), 0.0F, null, null, null);
    }
    
    ///
    /// Creates a fully configured progress bar.
    ///
    /// @param modifier         node modifier
    /// @param progress         normalized progress value
    /// @param overlayText      optional overlay text
    /// @param progressState    optional observable progress binding
    /// @param overlayTextState optional observable overlay binding
    ///
    protected SLProgressBarNode(
            SLModifier modifier,
            float progress,
            @Nullable Component overlayText,
            @Nullable UIState<Float> progressState,
            @Nullable UIState<Component> overlayTextState
    ) {
        super(modifier);
        this.progress = progress;
        this.overlayText = overlayText;
        this.progressState = progressState;
        this.overlayTextState = overlayTextState;
        
        if (progressState != null) {
            this.progress = progressState.get();
        }
        if (overlayTextState != null) {
            this.overlayText = overlayTextState.get();
        }
    }
    
    ///
    /// Creates a builder-backed progress bar while normalizing omitted values to
    /// the framework defaults.
    ///
    /// @param modifier         node modifier
    /// @param progress         normalized progress value
    /// @param overlayText      optional overlay text
    /// @param progressState    optional observable progress binding
    /// @param overlayTextState optional observable overlay binding
    ///
    /// @return configured progress bar node
    ///
    @Builder
    public static SLProgressBarNode of(
            SLModifier modifier,
            Float progress,
            Component overlayText,
            UIState<Float> progressState,
            UIState<Component> overlayTextState
    ) {
        return new SLProgressBarNode(
                Objects.requireNonNullElse(modifier, SLModifier.none()),
                Objects.requireNonNullElse(progress, 0.0F),
                overlayText,
                progressState,
                overlayTextState
        );
    }
    
    ///
    /// Sets the normalized progress value.
    ///
    /// @param progress progress in {@code [0, 1]}
    ///
    /// @return this progress bar for fluent runtime setup
    ///
    public SLProgressBarNode progress(float progress) {
        this.progress = progress;
        return this;
    }
    
    ///
    /// Sets optional overlay text rendered over the bar.
    ///
    /// @param overlayText overlay label, or {@code null}
    ///
    /// @return this progress bar for fluent runtime setup
    ///
    public SLProgressBarNode overlay(@Nullable Component overlayText) {
        this.overlayText = overlayText;
        this.invalidateLayout();
        return this;
    }
    
    ///
    /// Binds progress to observable state.
    ///
    /// @param progressState observable normalized progress state
    ///
    /// @return this progress bar for fluent runtime setup
    ///
    public SLProgressBarNode bindProgress(UIState<Float> progressState) {
        this.progressState = progressState;
        this.progress = progressState.get();
        return this;
    }
    
    ///
    /// Binds overlay text to observable state.
    ///
    /// @param overlayTextState observable overlay text state
    ///
    /// @return this progress bar for fluent runtime setup
    ///
    public SLProgressBarNode bindOverlay(UIState<Component> overlayTextState) {
        this.overlayTextState = overlayTextState;
        this.overlayText = overlayTextState.get();
        this.invalidateLayout();
        return this;
    }
    
    @Override
    protected void onAttached(UIRoot root) {
        if (this.progressState != null) this.unsubscribeProgressState = this.progressState.listen(value -> this.progress = value);
        if (this.overlayTextState != null) {
            this.unsubscribeOverlayState = this.overlayTextState.listen(value -> {
                this.overlayText = value;
                this.invalidateLayout();
            });
        }
    }
    
    @Override
    protected void onDetached() {
        if (this.unsubscribeProgressState != null) {
            this.unsubscribeProgressState.run();
            this.unsubscribeProgressState = null;
        }
        if (this.unsubscribeOverlayState != null) {
            this.unsubscribeOverlayState.run();
            this.unsubscribeOverlayState = null;
        }
    }
    
    @Override
    protected SLMeasuredSize measureContent(Font font, int availableWidth, int availableHeight) {
        int preferredWidth = 96;
        int preferredHeight = 14;
        if (this.overlayText != null) preferredWidth = Math.max(preferredWidth, font.width(this.overlayText) + 12);
        return new SLMeasuredSize(preferredWidth, preferredHeight);
    }
    
    @Override
    protected void renderSelf(SLRenderContext context) {
        context.theme().renderProgressBar(context, this.getBounds(), this.progress, this.overlayText, this.isEnabled());
    }
}
