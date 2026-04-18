package dev.satherov.sathlib.client.screen.render;

import dev.satherov.sathlib.client.screen.layout.SLBounds;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayDeque;
import java.util.Deque;

///
/// Small clip-stack wrapper over Minecraft's GUI scissor support.
///
/// A render context creates one clip stack for a render pass and nodes push or
/// pop clips as needed while drawing.
///
/// - centralize scissor usage behind one UI-facing API
/// - keep nested clipping balanced and easy to reason about
///
/// This class intentionally only exposes push and pop. Add helpers when a real
/// clipping pattern appears multiple times.
///
public final class SLClipStack {
    
    private final GuiGraphicsExtractor graphics;
    private final Deque<SLBounds> clips = new ArrayDeque<>();
    
    ///
    /// Creates a new clip stack for the supplied graphics extractor.
    ///
    /// @param graphics underlying graphics extractor
    ///
    public SLClipStack(GuiGraphicsExtractor graphics) {
        this.graphics = graphics;
    }
    
    ///
    /// Pushes a new clip rectangle.
    ///
    /// @param bounds clip bounds
    ///
    public void push(SLBounds bounds) {
        this.graphics.enableScissor(bounds.x(), bounds.y(), bounds.right(), bounds.bottom());
        this.clips.push(bounds);
    }
    
    ///
    /// Pops the current clip rectangle when one is active.
    ///
    public void pop() {
        if (this.clips.isEmpty()) return;
        this.graphics.disableScissor();
        this.clips.pop();
    }
}
