package dev.satherov.sathlib.client.screen.style;

import dev.satherov.sathlib.client.screen.layout.SLBounds;
import dev.satherov.sathlib.client.screen.render.SLRenderContext;
import dev.satherov.sathlib.common.menu.slot.SLSlotVisuals;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

///
/// Defines the shared look of built-in SathLib UI components.
///
/// A screen chooses one skin for its UI root. Built-in nodes delegate their
/// visuals to that skin every frame.
///
/// - render common widgets consistently
/// - centralize version-sensitive or style-sensitive drawing decisions
///
/// Custom screens can provide alternate skin implementations without changing
/// node behavior.
///
public interface UITheme {
    
    ///
    /// Renders a panel background and border.
    ///
    /// @param context render context
    /// @param bounds  target bounds
    ///
    void renderPanel(SLRenderContext context, SLBounds bounds);
    
    ///
    /// Renders a button with the given interaction state.
    ///
    /// @param context render context
    /// @param bounds  target bounds
    /// @param text    button label
    /// @param hovered whether the button is hovered
    /// @param pressed whether the button is pressed
    /// @param enabled whether the button is enabled
    ///
    void renderButton(
            SLRenderContext context,
            SLBounds bounds,
            Component text,
            boolean hovered,
            boolean pressed,
            boolean enabled
    );
    
    ///
    /// Renders a progress bar with an optional overlay label.
    ///
    /// @param context  render context
    /// @param bounds   target bounds
    /// @param progress normalized progress in {@code [0, 1]}
    /// @param overlay  optional overlay text
    /// @param enabled  whether the bar should render as enabled
    ///
    void renderProgressBar(
            SLRenderContext context,
            SLBounds bounds,
            float progress,
            @Nullable Component overlay,
            boolean enabled
    );
    
    ///
    /// Renders the shared frame for one menu slot.
    ///
    /// @param context render context
    /// @param bounds  resolved slot frame bounds
    /// @param visuals client-neutral slot visual hints
    /// @param hovered whether the slot is currently hovered
    /// @param active  whether the slot is currently active
    ///
    void renderSlotFrame(
            SLRenderContext context,
            SLBounds bounds,
            SLSlotVisuals visuals,
            boolean hovered,
            boolean active
    );
    
    ///
    /// Returns the default label text color.
    ///
    /// @param enabled whether the label should use its enabled color
    ///
    /// @return ARGB text color
    ///
    int labelColor(boolean enabled);
    
    ///
    /// Returns the accent color used by helper nodes and example screens.
    ///
    /// @return ARGB accent color
    ///
    int accentColor();
}
