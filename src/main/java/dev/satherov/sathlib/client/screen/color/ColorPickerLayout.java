package dev.satherov.sathlib.client.screen.color;

///
/// Shared layout metrics for the retained-mode color picker.
///
/// The picker still needs a handful of fixed measurements because its core
/// controls are literal color surfaces, but the values live in one place rather
/// than leaking throughout the screen.
///
public final class ColorPickerLayout {
    
    /// Outer content padding for the main panel.
    public static final int PANEL_PADDING = 16;
    /// Gap between the picker title and the first control row.
    public static final int TITLE_GAP = 10;
    /// Size of the saturation/value surface.
    public static final int SATURATION_VALUE_SIZE = 184;
    /// Width of the vertical hue slider.
    public static final int HUE_WIDTH = 18;
    /// Gap between the left color controls and the right utility column.
    public static final int COLUMN_GAP = 14;
    /// Width of the right utility column.
    public static final int SIDEBAR_WIDTH = 124;
    /// Size of the preview swatch frame.
    public static final int PREVIEW_SIZE = 82;
    /// Inner fill size of the preview swatch.
    public static final int PREVIEW_FILL_SIZE = 72;
    /// Height of one text field.
    public static final int FIELD_HEIGHT = 18;
    /// Height of one channel slider.
    public static final int SLIDER_HEIGHT = 10;
    /// Gap between stacked RGB sliders.
    public static final int SLIDER_GAP = 14;
    /// Gap between the preview, sliders, and hex field.
    public static final int SECTION_GAP = 16;
    
    private ColorPickerLayout() { }
}
