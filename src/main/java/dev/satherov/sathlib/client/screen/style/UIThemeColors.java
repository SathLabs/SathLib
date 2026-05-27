package dev.satherov.sathlib.client.screen.style;

///
/// Shared color palette exposed by a UI theme.
///
/// @param scrim           fullscreen backdrop color
/// @param panelShadow     outer panel shadow color
/// @param panelFillTop    primary surface fill at the top edge
/// @param panelFillBottom primary surface fill at the bottom edge
/// @param panelInset      inner panel fill
/// @param panelBorder     default panel border color
/// @param insetFill       sunken content fill
/// @param insetBorder     default inset border color
/// @param insetStrong     emphasized border or handle color
/// @param accent          primary accent color
/// @param accentMuted     muted accent or secondary track color
/// @param textPrimary     primary text color
/// @param textMuted       muted text or placeholder color
/// @param textDisabled    disabled text color
/// @param selection       text selection highlight color
/// @param white           bright contrast color for handles and carets
///
public record UIThemeColors(
        int scrim,
        int panelShadow,
        int panelFillTop,
        int panelFillBottom,
        int panelInset,
        int panelBorder,
        int insetFill,
        int insetBorder,
        int insetStrong,
        int accent,
        int accentMuted,
        int textPrimary,
        int textMuted,
        int textDisabled,
        int selection,
        int white
) { }
