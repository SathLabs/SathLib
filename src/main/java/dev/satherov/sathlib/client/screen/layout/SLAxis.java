package dev.satherov.sathlib.client.screen.layout;

///
/// Identifies a horizontal or vertical layout axis.
///
/// Axis values are used transiently during flow layout measurement and
/// assignment.
///
/// - describe which direction a container flows along
/// - help layout code choose the opposite cross axis
///
/// This enum is closed.
///
public enum SLAxis {
    HORIZONTAL,
    VERTICAL,
    ;
    
    ///
    /// Returns the opposite axis.
    ///
    /// @return opposite axis
    ///
    public SLAxis opposite() {
        return this == SLAxis.HORIZONTAL ? SLAxis.VERTICAL : SLAxis.HORIZONTAL;
    }
}
