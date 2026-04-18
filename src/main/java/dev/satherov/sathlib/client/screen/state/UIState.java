package dev.satherov.sathlib.client.screen.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

///
/// Minimal observable value used by screens and UI nodes.
///
/// States usually live alongside a screen or view-model object and are observed
/// by one or more nodes for the lifetime of that screen.
///
/// - store the current value
/// - notify listeners when the value changes
///
/// Keep this class intentionally small. Add helpers only when multiple screens
/// need the same state behavior.
///
/// @param <T> stored value type
///
public final class UIState<T> {
    
    private final List<Consumer<T>> listeners = new ArrayList<>();
    private T value;
    
    ///
    /// Creates a new state holder with an initial value.
    ///
    /// @param value initial state value
    ///
    public UIState(T value) {
        this.value = value;
    }
    
    ///
    /// Returns the current value.
    ///
    /// @return current state value
    ///
    public T get() {
        return this.value;
    }
    
    ///
    /// Updates the value and notifies listeners when it changed.
    ///
    /// @param value new state value
    ///
    public void set(T value) {
        if (Objects.equals(this.value, value)) return;
        this.value = value;
        this.notifyListeners();
    }
    
    ///
    /// Subscribes a listener and returns an unsubscribe hook.
    ///
    /// @param listener listener invoked after each value change
    ///
    /// @return callback that removes the listener
    ///
    public Runnable listen(Consumer<T> listener) {
        this.listeners.add(listener);
        return () -> this.listeners.remove(listener);
    }
    
    private void notifyListeners() {
        List<Consumer<T>> snapshot = List.copyOf(this.listeners);
        for (Consumer<T> listener : snapshot) {
            listener.accept(this.value);
        }
    }
}
