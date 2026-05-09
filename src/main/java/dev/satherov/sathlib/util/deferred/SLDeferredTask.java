package dev.satherov.sathlib.util.deferred;

import net.minecraft.server.MinecraftServer;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

///
/// A unit of server work that can be executed over multiple ticks.
///
public interface SLDeferredTask {
    
    ///
    /// Creates a functional deferred task from an executor and a completion condition.
    ///
    /// @param executor Work to execute every tick
    /// @param isDone   Completion condition
    ///
    /// @return Constructed deferred task
    ///
    static SLDeferredTask of(Consumer<MinecraftServer> executor, BooleanSupplier isDone) {
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(isDone, "isDone");
        
        return new SLDeferredTask() {
            @Override
            public void tick(MinecraftServer server) {
                executor.accept(server);
            }
            
            @Override
            public boolean isDone() {
                return isDone.getAsBoolean();
            }
            
            @Override
            public String name() {
                return super.toString();
            }
        };
    }
    
    ///
    /// Executes one step of this task.
    ///
    /// @param server Running server
    ///
    void tick(MinecraftServer server);
    
    ///
    /// Whether this task has completed and should be removed from the scheduler.
    ///
    /// @return {@code true} if this task no longer needs ticking
    ///
    boolean isDone();
    
    ///
    /// Called when the task is removed from the scheduler.
    ///
    /// @param server Current server, may be {@code null} if unavailable
    /// @param reason Reason for removal
    ///
    default void onRemoved(@Nullable MinecraftServer server, RemovalReason reason) { }
    
    ///
    /// Name of the deferred task, can be used for debugging purposes.
    ///
    /// @return Task name
    ///
    String name();
    
    ///
    /// Reason why a deferred task was removed from the scheduler.
    ///
    enum RemovalReason {
        ///
        /// The task reported completion through {@link SLDeferredTask#isDone()}.
        ///
        COMPLETED,
        ///
        /// The scheduler was cleared explicitly.
        ///
        CLEARED,
        ///
        /// The task failed while executing.
        ///
        FAILED
    }
}
