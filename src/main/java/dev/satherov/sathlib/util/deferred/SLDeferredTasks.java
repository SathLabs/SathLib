package dev.satherov.sathlib.util.deferred;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.SathLib;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BooleanSupplier;

///
/// Global registry for deferred server tasks.
///
@UtilityClass
public class SLDeferredTasks {
    
    private static final List<SLDeferredTask> TASKS = new ArrayList<>();
    private static final Queue<SLDeferredTask> PENDING_TASKS = new ConcurrentLinkedQueue<>();
    
    private static int nextTaskIndex = 0;
    
    ///
    /// Queues a task to start ticking on the server thread.
    ///
    /// @param task Task to register
    /// @param <T>  Task type
    ///
    /// @return Registered task
    ///
    public static <T extends SLDeferredTask> T register(T task) {
        Objects.requireNonNull(task, "task");
        SLDeferredTasks.PENDING_TASKS.add(task);
        return task;
    }
    
    ///
    /// Ticks the currently registered tasks while the server still has time left in this tick.
    ///
    /// @param server  Running server
    /// @param hasTime Time budget supplier
    ///
    public static void tick(MinecraftServer server, BooleanSupplier hasTime) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(hasTime, "hasTime");
        
        SLDeferredTasks.drainPending();
        
        if (SLDeferredTasks.TASKS.isEmpty()) {
            SLDeferredTasks.nextTaskIndex = 0;
            return;
        }
        
        int remaining = SLDeferredTasks.TASKS.size();
        int index = Math.floorMod(SLDeferredTasks.nextTaskIndex, SLDeferredTasks.TASKS.size());
        
        while (remaining > 0 && !SLDeferredTasks.TASKS.isEmpty() && hasTime.getAsBoolean()) {
            if (index >= SLDeferredTasks.TASKS.size()) {
                index = 0;
            }
            
            SLDeferredTask task = SLDeferredTasks.TASKS.get(index);
            if (task.isDone()) {
                SLDeferredTasks.remove(index, server, SLDeferredTask.RemovalReason.COMPLETED);
                remaining--;
                continue;
            }
            
            try {
                task.tick(server);
            } catch (Throwable throwable) {
                SathLib.log.error("Deferred task {} failed and will be removed", task, throwable);
                SLDeferredTasks.remove(index, server, SLDeferredTask.RemovalReason.FAILED);
                remaining--;
                continue;
            }
            
            remaining--;
            if (task.isDone()) {
                SLDeferredTasks.remove(index, server, SLDeferredTask.RemovalReason.COMPLETED);
                continue;
            }
            
            index++;
        }
        
        SLDeferredTasks.drainPending();
        SLDeferredTasks.nextTaskIndex = SLDeferredTasks.TASKS.isEmpty() ? 0 : index % SLDeferredTasks.TASKS.size();
    }
    
    ///
    /// Clears all active and pending tasks.
    ///
    /// @param server Server being shut down
    ///
    public static void clear(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        
        for (SLDeferredTask task : SLDeferredTasks.TASKS) {
            task.onRemoved(server, SLDeferredTask.RemovalReason.CLEARED);
        }
        SLDeferredTasks.TASKS.clear();
        
        SLDeferredTask pending;
        while ((pending = SLDeferredTasks.PENDING_TASKS.poll()) != null) {
            pending.onRemoved(server, SLDeferredTask.RemovalReason.CLEARED);
        }
        
        SLDeferredTasks.nextTaskIndex = 0;
    }
    
    ///
    /// Amount of active and pending tasks.
    ///
    /// @return Registered task count
    ///
    public static int size() {
        return SLDeferredTasks.TASKS.size() + SLDeferredTasks.PENDING_TASKS.size();
    }
    
    private static void drainPending() {
        SLDeferredTask task;
        while ((task = SLDeferredTasks.PENDING_TASKS.poll()) != null) {
            SLDeferredTasks.TASKS.add(task);
        }
    }
    
    private static void remove(int index, MinecraftServer server, SLDeferredTask.RemovalReason reason) {
        SLDeferredTask task = SLDeferredTasks.TASKS.remove(index);
        task.onRemoved(server, reason);
        
        if (SLDeferredTasks.TASKS.isEmpty()) {
            SLDeferredTasks.nextTaskIndex = 0;
        } else if (index < SLDeferredTasks.nextTaskIndex) {
            SLDeferredTasks.nextTaskIndex--;
        }
    }
}
