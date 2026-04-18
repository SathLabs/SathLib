package dev.satherov.sathlib.client.screen.layout;

///
/// Immutable measured size result from the UI measure pass.
///
/// Nodes compute measured sizes when the UI root is dirty or the viewport
/// changes.
///
/// - carry width and height results between measure and layout
///
/// This is a value type and is not intended for inheritance.
///
/// @param width  measured width
/// @param height measured height
///
public record SLMeasuredSize(int width, int height) {
    
    public static final SLMeasuredSize ZERO = new SLMeasuredSize(0, 0);
}
