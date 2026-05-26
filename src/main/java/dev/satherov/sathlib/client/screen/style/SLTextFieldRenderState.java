package dev.satherov.sathlib.client.screen.style;

import org.jspecify.annotations.Nullable;

///
/// Immutable visual state used while rendering a themed text field.
///
/// @param value          current text value
/// @param placeholder    optional placeholder shown when the field is empty
/// @param cursor         current cursor position
/// @param selectionStart selection start index
/// @param selectionEnd   selection end index
/// @param focused        whether the field currently owns focus
/// @param enabled        whether the field is enabled
///
public record SLTextFieldRenderState(
        String value,
        @Nullable String placeholder,
        int cursor,
        int selectionStart,
        int selectionEnd,
        boolean focused,
        boolean enabled
) {
    
    ///
    /// Returns whether the render state currently contains a selection range.
    ///
    /// @return {@code true} when the field has a selection
    ///
    public boolean hasSelection() {
        return this.selectionStart != this.selectionEnd;
    }
}
