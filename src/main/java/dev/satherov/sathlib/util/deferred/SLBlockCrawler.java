package dev.satherov.sathlib.util.deferred;

import lombok.Builder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javax.annotation.Nonnull;

///
/// Breadth-first block crawler that advances through the deferred task scheduler.
///
@Builder
@SuppressWarnings("doclint:missing")
public class SLBlockCrawler implements SLDeferredTask {
    
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Function<SLBlockCrawler, Boolean> NEVER_STOP = _ -> false;
    
    @Nonnull
    private final Deque<BlockPos> queue;
    
    @Nonnull
    private final Set<BlockPos> visited;
    
    @Nonnull
    private final ServerLevel level;
    
    @Nonnull
    private final BiConsumer<BlockPos, BlockState> consumer;
    
    @Nonnull
    @Builder.Default
    private final BiPredicate<BlockPos, BlockState> predicate = (_, _) -> true;
    
    @Builder.Default
    private final int iterations = 1;
    
    @Builder.Default
    private final Function<SLBlockCrawler, Boolean> stopCondition = SLBlockCrawler.NEVER_STOP;
    
    ///
    /// Starts a crawler builder with the origin position already queued and visited.
    ///
    /// @param level  level the crawler reads from
    /// @param origin starting block position
    ///
    /// @return preconfigured crawler builder
    ///
    public static SLBlockCrawlerBuilder builder(ServerLevel level, BlockPos origin) {
        final Collection<BlockPos> singleton = Collections.singleton(origin);
        return new SLBlockCrawlerBuilder().level(level).queue(new ArrayDeque<>(singleton)).visited(new HashSet<>(singleton));
    }
    
    @Override
    public void tick(MinecraftServer server) {
        if (this.isDone()) return;
        
        for (int i = 0; i < this.iterations; i++) {
            if (this.isDone()) break;
            
            BlockPos current = this.queue.pollFirst();
            if (current == null) break;
            
            for (Direction direction : SLBlockCrawler.DIRECTIONS) {
                BlockPos next = current.relative(direction).immutable();
                if (!this.visited.add(next)) continue;
                
                BlockState state = this.level.getBlockState(next);
                if (!this.predicate.test(next, state)) continue;
                
                this.consumer.accept(next, state);
                this.queue.addLast(next);
            }
        }
    }
    
    @Override
    public boolean isDone() {
        return this.queue.isEmpty() || this.stopCondition.apply(this);
    }
    
    ///
    /// Returns how many positions have been visited so far.
    ///
    /// @return visited position count
    ///
    public int visitedCount() {
        return this.visited.size();
    }
    
    ///
    /// Returns how many positions are currently queued for traversal.
    ///
    /// @return queued position count
    ///
    public int queuedCount() {
        return this.queue.size();
    }
}
