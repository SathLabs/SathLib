package dev.satherov.sathlib.client.screen.node;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

import java.util.List;

///
/// Optional hover-tooltip contract for retained-mode UI nodes.
///
/// Screens can inspect the currently hovered node and, when it implements this
/// interface, show the returned tooltip lines through the normal screen tooltip
/// path.
///
public interface SLTooltipProvider {
    
    ///
    /// Returns the tooltip lines that should be shown while the node is
    /// hovered.
    ///
    /// @return tooltip lines, or {@code null} for no tooltip
    ///
    @Nullable List<Component> getTooltipLines();
}
