package dev.satherov.sathlib.common.properties;

///
/// Allows cycling a value to the next or previous value in a sequence.
///
/// @param <T> Type of the value to cycle.
///
/// @see PropertyEnum
/// @see BlockItemProperty
///
@FunctionalInterface
public interface PropertyCycler<T> {
    
    ///
    /// Cycles the value to the next or previous value in a sequence.
    ///
    /// @param forward  Whether to cycle forward or backward.
    /// @param original The original value.
    ///
    /// @return The new value.
    ///
    T cycle(boolean forward, T original);
    
    
    ///
    /// A cycler that cycles a boolean value by inverting the current value.
    ///
    PropertyCycler<Boolean> BOOLEAN = (_, val) -> !val;
    
    ///
    /// A cycler that cycles an enum value by cycling to the next value in the enum.
    /// Wraps around to the beginning of the enum if the end is reached.
    ///
    /// @param type The enum class.
    /// @param <E>  The type of the enum.
    ///
    /// @return the property cycler.
    ///
    static <E extends Enum<E> & PropertyEnum> PropertyCycler<E> enumCycler(Class<E> type) {
        return (forward, value) -> {
            E[] values = type.getEnumConstants();
            int next = value.ordinal() + (forward ? 1 : -1);
            if (next < 0) next += values.length;
            return values[next % values.length];
        };
    }
    
    ///
    /// A cycler that cycles a short value by incrementing or decrementing it.
    /// Wraps around to the minimum or maximum value if the end is reached.
    ///
    /// @param min The minimum value.
    /// @param max The maximum value.
    ///
    /// @return the property cycler.
    ///
    static PropertyCycler<Short> numberCycler(short min, short max) {
        return (forward, val) -> {
            short next = (short) (val + (forward ? 1 : -1));
            if (next > max) return min;
            if (next < min) return max;
            return next;
        };
    }
    
    ///
    /// A cycler that cycles an integer value by incrementing or decrementing it.
    /// Wraps around to the minimum or maximum value if the end is reached.
    ///
    /// @param min The minimum value.
    /// @param max The maximum value.
    ///
    /// @return the property cycler.
    ///
    static PropertyCycler<Integer> numberCycler(int min, int max) {
        return (forward, val) -> {
            int next = val + (forward ? 1 : -1);
            if (next > max) return min;
            if (next < min) return max;
            return next;
        };
    }
    
    ///
    /// A cycler that cycles a long value by incrementing or decrementing it.
    /// Wraps around to the minimum or maximum value if the end is reached.
    ///
    /// @param min The minimum value.
    /// @param max The maximum value.
    ///
    /// @return the property cycler.
    ///
    static PropertyCycler<Long> numberCycler(long min, long max) {
        return (forward, val) -> {
            long next = val + (forward ? 1 : -1);
            if (next > max) return min;
            if (next < min) return max;
            return next;
        };
    }
    
    ///
    /// A cycler that cycles a float value by incrementing or decrementing it.
    /// Wraps around to the minimum or maximum value if the end is reached.
    ///
    /// @param min The minimum value.
    /// @param max The maximum value.
    ///
    /// @return the property cycler.
    ///
    static PropertyCycler<Float> numberCycler(float min, float max) {
        return (forward, val) -> {
            float next = val + (forward ? 1 : -1);
            if (next > max) return min;
            if (next < min) return max;
            return next;
        };
    }
    
    ///
    /// A cycler that cycles a double value by incrementing or decrementing it.
    /// Wraps around to the minimum or maximum value if the end is reached.
    ///
    /// @param min The minimum value.
    /// @param max The maximum value.
    ///
    /// @return the property cycler.
    ///
    static PropertyCycler<Double> numberCycler(double min, double max) {
        return (forward, val) -> {
            double next = val + (forward ? 1 : -1);
            if (next > max) return min;
            if (next < min) return max;
            return next;
        };
    }
}
