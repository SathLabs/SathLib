package dev.satherov.sathlib.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import dev.satherov.sathlib.common.blockentity.AreaBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongConsumer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(staticName = "create")
public final class AreaTracker<T extends AreaBlockEntity<T>> {
    
    private final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<ObjectOpenHashSet<AreaEntry<T>>>> areas = new HashMap<>();
    private final Map<T, AreaEntry<T>> entries = new IdentityHashMap<>();
    
    private @Nullable AreaEntry<T> cached;
    
    private static void forEachIntersectingChunk(BoundingBox area, LongConsumer action) {
        final int minChunkX = SectionPos.blockToSectionCoord(area.minX());
        final int maxChunkX = SectionPos.blockToSectionCoord(area.maxX());
        final int minChunkZ = SectionPos.blockToSectionCoord(area.minZ());
        final int maxChunkZ = SectionPos.blockToSectionCoord(area.maxZ());
        
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                action.accept(ChunkPos.pack(chunkX, chunkZ));
            }
        }
    }
    
    public synchronized void register(T entity) {
        this.update(entity, entity.getArea());
    }
    
    public synchronized void update(T entity, BoundingBox localArea) {
        final Level level = entity.getLevel();
        
        if (level == null) {
            AreaTracker.log.warn("Tried to register area without level at {}", entity.getBlockPos());
            return;
        }
        
        final BlockPos pos = entity.getBlockPos().immutable();
        final BoundingBox area = localArea.moved(pos.getX(), pos.getY(), pos.getZ());
        final AreaEntry<T> entry = new AreaEntry<>(level.dimension(), pos, area, entity);
        final AreaEntry<T> previous = this.entries.get(entity);
        if (entry.equals(previous)) return;
        
        if (previous != null) this.removeFromChunks(previous);
        this.entries.put(entity, entry);
        this.addToChunks(entry);
        
        this.cached = null;
    }
    
    public synchronized void unregister(T entity) {
        final AreaEntry<T> entry = this.entries.remove(entity);
        if (entry == null) return;
        
        this.removeFromChunks(entry);
        if (this.cached == entry) this.cached = null;
    }
    
    public synchronized Optional<T> find(Level level, Vec3i target) {
        if (this.cached != null && this.cached.isValid(level, target)) {
            return Optional.of(this.cached.entity());
        }
        
        final Long2ObjectOpenHashMap<ObjectOpenHashSet<AreaEntry<T>>> chunks = this.areas.get(level.dimension());
        
        if (chunks == null) return Optional.empty();
        
        final long chunkKey = ChunkPos.pack(
                SectionPos.blockToSectionCoord(target.getX()),
                SectionPos.blockToSectionCoord(target.getZ())
        );
        
        final ObjectOpenHashSet<AreaEntry<T>> entries = chunks.get(chunkKey);
        if (entries == null || entries.isEmpty()) return Optional.empty();
        
        AreaEntry<T> closest = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (AreaEntry<T> entry : entries) {
            if (!entry.isValid(target)) continue;
            final double distance = entry.pos().distSqr(target);
            
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = entry;
            }
        }
        
        if (closest == null) return Optional.empty();
        this.cached = closest;
        return Optional.of(closest.entity());
    }
    
    private void addToChunks(AreaEntry<T> entry) {
        final Long2ObjectOpenHashMap<ObjectOpenHashSet<AreaEntry<T>>> chunks = this.areas.computeIfAbsent(entry.dimension(), _ -> new Long2ObjectOpenHashMap<>());
        AreaTracker.forEachIntersectingChunk(entry.area(), chunkKey -> chunks.computeIfAbsent(chunkKey, _ -> new ObjectOpenHashSet<>()).add(entry));
    }
    
    private void removeFromChunks(@NotNull AreaEntry<T> entry) {
        final Long2ObjectOpenHashMap<ObjectOpenHashSet<AreaEntry<T>>> chunks = this.areas.get(entry.dimension());
        if (chunks == null) return;
        
        AreaTracker.forEachIntersectingChunk(entry.area(), chunkKey -> {
            final ObjectOpenHashSet<AreaEntry<T>> chunkEntries = chunks.get(chunkKey);
            if (chunkEntries == null) return;
            
            chunkEntries.remove(entry);
            if (chunkEntries.isEmpty()) chunks.remove(chunkKey);
        });
        
        if (chunks.isEmpty()) this.areas.remove(entry.dimension());
    }
    
    private record AreaEntry<T extends AreaBlockEntity<T>>(ResourceKey<Level> dimension, BlockPos pos, BoundingBox area, T entity) {
        
        private boolean isValid(Level level, Vec3i target) {
            return this.dimension().equals(level.dimension()) && this.isValid(target);
        }
        
        private boolean isValid(Vec3i target) {
            return !this.entity().isRemoved() && this.area().isInside(target);
        }
    }
}
