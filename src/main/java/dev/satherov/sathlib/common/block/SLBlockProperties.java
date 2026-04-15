package dev.satherov.sathlib.common.block;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

///
/// {@link BlockBehaviour.Properties} extension with some extra helpers.
///
@NothingNull
public class SLBlockProperties extends BlockBehaviour.Properties {
    
    ///
    /// Creates a new {@link SLBlockProperties} instance.
    ///
    /// @return New {@link SLBlockProperties} instance.
    ///
    public static SLBlockProperties create() {
        return new SLBlockProperties();
    }
    
    ///
    /// Creates a new {@link SLBlockProperties} instance with
    /// the {@link ResourceKey} created from the given {@link Identifier}.
    ///
    /// @param identifier Identifier of the block.
    ///
    /// @return New {@link SLBlockProperties} instance.
    ///
    public static SLBlockProperties create(Identifier identifier) {
        return new SLBlockProperties().setId(identifier);
    }
    
    ///
    /// Creates a new {@link SLBlockProperties} instance with
    /// the given {@link ResourceKey}.
    ///
    /// @param key Resource key of the block.
    ///
    /// @return New {@link SLBlockProperties} instance.
    ///
    public static SLBlockProperties create(ResourceKey<Block> key) {
        return new SLBlockProperties().setId(key);
    }
    
    ///
    /// Sets the map color of the block to the given {@link DyeColor}.
    /// Affects the color at which the given block is shown on a map.
    ///
    /// Defaults to {@link MapColor#NONE}
    ///
    /// @param mapColor {@link DyeColor} representing the map color of the block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties mapColor(DyeColor mapColor) {
        super.mapColor(mapColor);
        return this;
    }
    
    ///
    /// Sets the map color of the block to the given {@link MapColor}.
    /// Affects the color at which the given block is shown on a map.
    ///
    /// Defaults to {@link MapColor#NONE}
    ///
    /// @param mapColor {@link MapColor} representing the map color of the block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties mapColor(MapColor mapColor) {
        super.mapColor(mapColor);
        return this;
    }
    
    ///
    /// Sets the map color of the block to the given {@link MapColor}
    /// using the given factory supplying the {@link BlockState}.
    /// Affects the color at which the given block is shown on a map.
    ///
    /// Defaults to {@link MapColor#NONE}
    ///
    /// @param mapColor Factory to construct the {@link MapColor} of the block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties mapColor(Function<BlockState, MapColor> mapColor) {
        super.mapColor(mapColor);
        return this;
    }
    
    ///
    /// Sets the {@code hasCollision} property of the block to false.
    /// {@code false} means entities can pass through the block.
    ///
    /// Defaults to {@code true}.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties noCollision() {
        super.noCollision();
        return this;
    }
    
    ///
    /// Sets the {@code canOcclude} property of the block to false.
    /// {@code false} means the block will block light.
    ///
    /// Defaults to {@code true}.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties noOcclusion() {
        super.noOcclusion();
        return this;
    }
    
    ///
    /// Sets the entity friction value of this block.
    /// Higher value means slower acceleration and faster stopping.
    ///
    /// Defaults to {@code 0.6F}.
    ///
    /// @param friction The friction value of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties friction(float friction) {
        super.friction(friction);
        return this;
    }
    
    ///
    /// Sets the entity delta speed multiplier of this block.
    /// Higher value means faster acceleration and faster overall movement.
    ///
    /// Defaults to {@code 1.0F}.
    ///
    /// @param speedFactor The speed factor of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties speedFactor(float speedFactor) {
        super.speedFactor(speedFactor);
        return this;
    }
    
    ///
    /// Sets the entity jump multiplier of this block.
    /// Higher value means higher jump height.
    ///
    /// Defaults to {@code 1.0F}.
    ///
    /// @param jumpFactor The jump factor of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties jumpFactor(float jumpFactor) {
        super.jumpFactor(jumpFactor);
        return this;
    }
    
    ///
    /// Sets the sound type of this block.
    /// Affects the sound type played when a player
    /// mines this block or walks over it.
    ///
    /// Defaults to {@link SoundType#STONE}.
    ///
    /// @param soundType The sound type of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties sound(SoundType soundType) {
        super.sound(soundType);
        return this;
    }
    
    ///
    /// Sets the light emission of this block
    /// using the given factory supplying the {@link BlockState}.
    /// Higher value means higher light level emission.
    ///
    /// Defaults to {@code 0}. Max value is {@code 15}.
    ///
    /// @param lightEmission Factory to construct the light emission of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties lightLevel(ToIntFunction<BlockState> lightEmission) {
        super.lightLevel(lightEmission);
        return this;
    }
    
    ///
    /// Sets the light emission of this block.
    /// Higher value means higher light level emission.
    ///
    /// Defaults to {@code 0}. Max value is {@code 15}.
    ///
    /// @param lightLevel The light emission of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    public SLBlockProperties lightLevel(int lightLevel) {
        return this.lightLevel(_ -> lightLevel);
    }
    
    ///
    /// Sets the strength of this block, defined by its
    /// destroy-time and explosion-resistance.
    ///
    /// Destroy-time defaults to {@code 0.0F}
    /// Explosion-resistance defaults to {@code 0.0F}
    ///
    /// @param destroyTime         The destroy-time of this block.
    /// @param explosionResistance The explosion-resistance of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    /// @see SLBlockProperties#destroyTime(float)
    /// @see SLBlockProperties#explosionResistance(float)
    ///
    @Override
    public SLBlockProperties strength(float destroyTime, float explosionResistance) {
        super.strength(destroyTime, explosionResistance);
        return this;
    }
    
    ///
    /// Sets the strength of this block by setting both its
    /// destroy-time and explosion-resistance to the same value.
    ///
    /// Destroy-time defaults to {@code 0.0F}
    /// Explosion-resistance defaults to {@code 0.0F}
    ///
    /// @param strength The strength of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    /// @see SLBlockProperties#destroyTime(float)
    /// @see SLBlockProperties#explosionResistance(float)
    ///
    @Override
    public SLBlockProperties strength(float strength) {
        super.strength(strength);
        return this;
    }
    
    ///
    /// Sets the destroy-time of this block.
    /// Higher value means longer destroy time.
    ///
    /// {@code -1} to make the block unbreakable.
    ///
    /// {@code 0} to make the block breakable in one-hit.
    ///
    /// Defaults to {@code 0.0F}
    ///
    @Override
    public SLBlockProperties destroyTime(float destroyTime) {
        super.destroyTime(destroyTime);
        return this;
    }
    
    ///
    /// Sets the explosion-resistance of this block.
    /// Higher value means this block absorbs more damage from an explosion.
    ///
    /// Defaults to {@code 0.0F}. Min value is {@code 0.0F}.
    ///
    /// @param explosionResistance Explosion resistance of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties explosionResistance(float explosionResistance) {
        super.explosionResistance(explosionResistance);
        return this;
    }
    
    ///
    /// Marks this block as being breakable in one-hit,
    /// even without any tools.
    /// Effectively calls {@link SLBlockProperties#strength(float)} with value {@code 0}.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties instabreak() {
        super.instabreak();
        return this;
    }
    
    ///
    /// Marks this block as receiving random-tick updates.
    /// Must be set for {@link Block#randomTick(BlockState, ServerLevel, BlockPos, RandomSource)} to function.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties randomTicks() {
        super.randomTicks();
        return this;
    }
    
    ///
    /// Marks this block as having a dynamic shape and prevents model
    /// relevant information from being cached. Forced the game to
    /// recalculate collision shapes every time they're checked.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties dynamicShape() {
        super.dynamicShape();
        return this;
    }
    
    ///
    /// Sets this resource key of this block's loot table to empty. Effectively
    /// calls {@link SLBlockProperties#overrideLootTable(Optional)} with value {@code Optional.empty()}.
    /// Cheaper than generating an empty loot table.
    ///
    /// Defaults to {@code Optional.of(ResourceKey.create(Registries.LOOT_TABLE, id.identifier().withPrefix("blocks/")))}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties noLootTable() {
        super.noLootTable();
        return this;
    }
    
    ///
    /// Overrides the default loot table by setting the resource key of this block's loot table to the given optional {@link ResourceKey}.
    ///
    /// Defaults to {@code Optional.of(ResourceKey.create(Registries.LOOT_TABLE, id.identifier().withPrefix("blocks/")))}
    ///
    /// @param table Optional resource key of the loot table.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties overrideLootTable(Optional<ResourceKey<LootTable>> table) {
        super.overrideLootTable(table);
        return this;
    }
    
    ///
    /// Overrides the default loot table by setting the resource key of this block's loot table to the given {@link ResourceKey}.
    ///
    /// Defaults to {@code Optional.of(ResourceKey.create(Registries.LOOT_TABLE, id.identifier().withPrefix("blocks/")))}
    ///
    /// @param table Resource key of the loot table.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    public SLBlockProperties overrideLootTable(ResourceKey<LootTable> table) {
        super.overrideLootTable(Optional.of(table));
        return this;
    }
    
    ///
    /// Overrides the default loot table by setting the resource key of this block's loot table to the given {@link Identifier}.
    ///
    /// Defaults to {@code Optional.of(ResourceKey.create(Registries.LOOT_TABLE, id.identifier().withPrefix("blocks/")))}
    ///
    /// @param table Identifier of the loot table.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    public SLBlockProperties overrideLootTable(Identifier table) {
        super.overrideLootTable(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, table)));
        return this;
    }
    
    ///
    /// Marks this block as being able to catch on fire.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties ignitedByLava() {
        super.ignitedByLava();
        return this;
    }
    
    ///
    /// Marks this block as being a liquid. Used for certain checks such as the water heightmap or
    /// by buckets when checking if the given state can be picked up.
    ///
    /// Should normally only be used when extending {@link LiquidBlock}.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties liquid() {
        super.liquid();
        return this;
    }
    
    ///
    /// Legacy property for forcing the {@link BlockState#isSolid()} to always return true
    /// instead of calculating it from the collision shape.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties forceSolidOn() {
        super.forceSolidOn();
        return this;
    }
    
    ///
    /// Legacy property for forcing the {@link BlockState#isSolid()} to always return false
    /// instead of calculating it from the collision shape.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public SLBlockProperties forceSolidOff() {
        super.forceSolidOff();
        return this;
    }
    
    ///
    /// Sets the {@link PushReaction} of this block.
    ///
    /// Defaults to {@link PushReaction#NORMAL}
    ///
    /// @param pushReaction The {@link PushReaction} of this block.
    ///
    @Override
    public SLBlockProperties pushReaction(PushReaction pushReaction) {
        super.pushReaction(pushReaction);
        return this;
    }
    
    ///
    /// Marks this block as air. Used for optimizing chunk storage and querying,
    /// as well as checking if a block is unoccupied.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties air() {
        super.air();
        return this;
    }
    
    ///
    /// Overrides the default spawn check.
    /// If the check returns true an entity can spawn on top of this block.
    ///
    /// Defaults to {@code (state, level, pos, entityType) -> state.isFaceSturdy(level, pos, Direction.UP) && state.getLightEmission(level, pos) < 14}
    ///
    /// @param isValidSpawn The spawn check predicate.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn) {
        super.isValidSpawn(isValidSpawn);
        return this;
    }
    
    ///
    /// Overrides the default conductivity check.
    /// A conductive block will pass a redstone signal through it.
    ///
    /// Defaults to {@code (state, level, pos) -> state.isCollisionShapeFullBlock(level, pos)}
    ///
    /// @param isRedstoneConductor The conductivity check predicate.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties isRedstoneConductor(BlockBehaviour.StatePredicate isRedstoneConductor) {
        super.isRedstoneConductor(isRedstoneConductor);
        return this;
    }
    
    ///
    /// Overrides the default suffocation check.
    /// If the check returns true, an entity with its head inside this block will suffocate.
    ///
    /// Defaults to {@code (state, level, pos) -> state.blocksMotion() && state.isCollisionShapeFullBlock(level, pos)}
    ///
    /// @param isSuffocating The suffocation check predicate.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties isSuffocating(BlockBehaviour.StatePredicate isSuffocating) {
        super.isSuffocating(isSuffocating);
        return this;
    }
    
    ///
    /// Overrides the default view blocking check.
    /// If the check returns true, blocks visibility on the client side.
    /// Used for calculating ambient occlusion.
    ///
    /// Defaults to {@code (state, level, pos) -> state.blocksMotion() && state.isCollisionShapeFullBlock(level, pos)}
    ///
    /// @param isViewBlocking The view blocking check predicate.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties isViewBlocking(BlockBehaviour.StatePredicate isViewBlocking) {
        super.isViewBlocking(isViewBlocking);
        return this;
    }
    
    ///
    /// Marks this block for post-processing after being generated. This ticks a fluid or
    /// updates a state from its neighbors.
    ///
    /// Defaults to {@code (state, level, pos) -> false}
    ///
    /// @param postProcess The post-processing check predicate.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    /// @see LevelChunk#postProcessGeneration(ServerLevel)
    ///
    @Override
    public BlockBehaviour.Properties postProcess(BlockBehaviour.PostProcess postProcess) {
        super.postProcess(postProcess);
        return this;
    }
    
    ///
    /// Marks this block to render as if it were emitting light.
    ///
    /// Defaults to {@code (state, level, pos) -> false}
    ///
    /// @param emissiveRendering The emissive rendering check predicate.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    /// @see LevelRenderer#getLightCoords(LevelRenderer.BrightnessGetter, BlockAndLightGetter, BlockState, BlockPos)
    ///
    @Override
    public SLBlockProperties emissiveRendering(BlockBehaviour.StatePredicate emissiveRendering) {
        super.emissiveRendering(emissiveRendering);
        return this;
    }
    
    ///
    /// Marks this block as requiring the correct tool for it to drop.
    /// Without this set the block can be mined by hand.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties requiresCorrectToolForDrops() {
        super.requiresCorrectToolForDrops();
        return this;
    }
    
    ///
    /// Sets the {@link BlockBehaviour.OffsetType} of this block.
    /// This affects placement on the block and will slightly shift the placement
    /// off-center, such as with saplings or flowers.
    ///
    /// Defaults to {@code null}
    ///
    /// @param offsetType The {@link BlockBehaviour.OffsetType} of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties offsetType(BlockBehaviour.OffsetType offsetType) {
        super.offsetType(offsetType);
        return this;
    }
    
    ///
    /// Sets the {@code spawnTerrainParticles} property of this block to false.
    /// Terrain particles appear, for example, when running over a block,
    /// falling onto a block, hitting a block, etc.
    ///
    /// Defaults to {@code true}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties noTerrainParticles() {
        super.noTerrainParticles();
        return this;
    }
    
    ///
    /// Sets the required {@link FeatureFlag}s for this block to show up in game.
    ///
    /// Defaults to {@link FeatureFlags#VANILLA_SET}
    ///
    /// @param requiredFeatures The required {@link FeatureFlag}s for this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties requiredFeatures(FeatureFlag... requiredFeatures) {
        super.requiredFeatures(requiredFeatures);
        return this;
    }
    
    ///
    /// Sets the {@link NoteBlockInstrument} of this block.
    /// Adjusts the noise a note block makes while this block is placed under it.
    ///
    /// Defaults to {@link NoteBlockInstrument#HARP}
    ///
    /// @param instrument The {@link NoteBlockInstrument} of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties instrument(NoteBlockInstrument instrument) {
        super.instrument(instrument);
        return this;
    }
    
    ///
    /// Marks this block as being able to be replaced by another block.
    /// Allows players to place another block into a space occupied by this block.
    ///
    /// Defaults to {@code false}
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties replaceable() {
        super.replaceable();
        return this;
    }
    
    ///
    /// Sets the {@link ResourceKey} of this block.
    ///
    /// Defaults to {@code null}
    ///
    /// @param key The {@link ResourceKey} of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties setId(ResourceKey<Block> key) {
        super.setId(key);
        return this;
    }
    
    ///
    /// Sets the {@link Identifier} to construct the {@link ResourceKey} of this block from.
    ///
    /// Defaults to {@code null}
    ///
    /// @param identifier The {@link Identifier} to construct the {@link ResourceKey} from.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    public SLBlockProperties setId(Identifier identifier) {
        super.setId(ResourceKey.create(Registries.BLOCK, identifier));
        return this;
    }
    
    ///
    /// Overwrites the default description translation key of this block.
    ///
    /// Defaults to {@code id -> Util.makeDescriptionId("block", id.identifier())}
    ///
    /// @param descriptionId The description translation key of this block.
    ///
    /// @return This {@link SLBlockProperties} instance.
    ///
    @Override
    public SLBlockProperties overrideDescription(String descriptionId) {
        super.overrideDescription(descriptionId);
        return this;
    }
    
    ///
    /// Marks this block as a fluid. Effectively calls {@link SLBlockProperties#noCollision()},
    /// {@link SLBlockProperties#replaceable()} and {@link SLBlockProperties#liquid()}.
    ///
    public SLBlockProperties fluid() {
        return this.noCollision().replaceable().liquid();
    }
    
    ///
    /// Always true {@link BlockBehaviour.StateArgumentPredicate}
    ///
    /// @return true
    ///
    public static <T> boolean always(BlockState state, BlockGetter getter, BlockPos pos, T arg) {
        return true;
    }
    
    ///
    /// Always false {@link BlockBehaviour.StateArgumentPredicate}
    ///
    /// @return false
    ///
    public static <T> boolean never(BlockState state, BlockGetter getter, BlockPos pos, T arg) {
        return false;
    }
    
    ///
    /// Always true {@link BlockBehaviour.StatePredicate}
    ///
    /// @return true
    ///
    public static boolean always(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }
    
    ///
    /// Always false {@link BlockBehaviour.StatePredicate}
    ///
    /// @return false
    ///
    public static boolean never(BlockState state, BlockGetter getter, BlockPos pos) {
        return false;
    }
}