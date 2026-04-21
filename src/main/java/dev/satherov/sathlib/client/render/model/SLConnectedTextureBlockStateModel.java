package dev.satherov.sathlib.client.render.model;

import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Runtime blockstate model wrapper that remaps quad UVs to the correct
/// connected-texture atlas region based on neighboring blocks.
///
/// The wrapped base model still provides all geometry. This class only
/// computes per-target connectivity masks and swaps UV regions or variant
/// sprites for quads that match a connected-texture target.
public final class SLConnectedTextureBlockStateModel extends DelegateBlockStateModel {
    
    private final List<BakedTarget> targets;
    
    private SLConnectedTextureBlockStateModel(BlockStateModel delegate, List<BakedTarget> targets) {
        super(delegate);
        this.targets = List.copyOf(targets);
    }
    
    /// Bakes a connected-texture blockstate model wrapper.
    ///
    /// If no targets are supplied, this method returns the baked base model
    /// directly and avoids adding any runtime wrapper layer.
    ///
    /// @param baseModel base model variant
    /// @param targets   connected-texture targets to evaluate at runtime
    /// @param baker     model baker used for dependent models and sprites
    /// @param debugName debug label supplier
    ///
    /// @return baked blockstate model
    public static BlockStateModel bake(Variant baseModel, List<SLConnectedTextureTarget> targets, ModelBaker baker, ModelDebugName debugName) {
        BlockStateModel delegate = new SingleVariant(baseModel.bake(baker));
        if (targets.isEmpty()) return delegate;
        
        MaterialBaker materialBaker = baker.materials();
        List<BakedTarget> bakedTargets = new ArrayList<>(targets.size());
        for (int index = 0; index < targets.size(); index++) {
            SLConnectedTextureTarget target = targets.get(index);
            bakedTargets.add(BakedTarget.bake(index, target, materialBaker, () -> debugName.debugName() + "#" + target.id()));
        }
        return new SLConnectedTextureBlockStateModel(delegate, bakedTargets);
    }
    
    /// Creates a geometry cache key that includes the base model's key plus the
    /// resolved connected-texture render data for the block.
    ///
    /// @param level  render view used for neighbor lookups
    /// @param pos    block position being rendered
    /// @param state  block state being rendered
    /// @param random random source provided by the renderer
    ///
    /// @return geometry cache key, or {@code null} if the delegate returns one
    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return new GeometryKey(this.delegate.createGeometryKey(level, pos, state, random), this.resolveRenderData(level, pos, state));
    }
    
    /// Collects model parts from the wrapped model and retextures any quads that
    /// belong to a connected-texture target.
    ///
    /// @param level  render view used for neighbor lookups
    /// @param pos    block position being rendered
    /// @param state  block state being rendered
    /// @param random random source provided by the renderer
    /// @param parts  destination list for baked parts
    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        List<BlockStateModelPart> baseParts = new ArrayList<>();
        this.delegate.collectParts(level, pos, state, random, baseParts);
        if (baseParts.isEmpty() || this.targets.isEmpty()) {
            parts.addAll(baseParts);
            return;
        }
        
        SLConnectedTextureRenderData renderData = this.resolveRenderData(level, pos, state);
        for (BlockStateModelPart basePart : baseParts) {
            parts.add(this.transform(basePart, renderData));
        }
    }
    
    private BlockStateModelPart transform(BlockStateModelPart part, SLConnectedTextureRenderData renderData) {
        QuadCollection.Builder quadBuilder = new QuadCollection.Builder();
        boolean changed = false;
        
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : part.getQuads(direction)) {
                BakedQuad transformed = this.transformQuad(quad, renderData);
                quadBuilder.addCulledFace(direction, transformed);
                changed |= transformed != quad;
            }
        }
        
        for (BakedQuad quad : part.getQuads(null)) {
            BakedQuad transformed = this.transformQuad(quad, renderData);
            quadBuilder.addUnculledFace(transformed);
            changed |= transformed != quad;
        }
        
        if (!changed) return part;
        return new SLConnectedTexturePart(quadBuilder.build(), part.useAmbientOcclusion(), part.particleMaterial());
    }
    
    private BakedQuad transformQuad(BakedQuad quad, SLConnectedTextureRenderData renderData) {
        BakedTarget target = this.findTarget(quad);
        if (target == null) return quad;
        
        @SLConnectedTextureMask int mask = renderData.mask(target.index(), quad.direction());
        String variant = renderData.variant(target.index());
        TextureAtlasSprite sprite = target.variantSprite(variant);
        SLConnectedTextureRegion region = target.layout().regionForMask(mask);
        return SLConnectedTextureQuadUtils.retexture(quad, sprite, region);
    }
    
    private @Nullable BakedTarget findTarget(BakedQuad quad) {
        for (BakedTarget target : this.targets) {
            if (target.matches(quad)) return target;
        }
        return null;
    }
    
    private SLConnectedTextureRenderData resolveRenderData(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        SLConnectedTextureContext current = new SLConnectedTextureContext(level, pos, state, level.getModelData(pos));
        String[] variants = new String[this.targets.size()];
        int[][] masks = new int[this.targets.size()][Direction.values().length];
        
        for (int targetIndex = 0; targetIndex < this.targets.size(); targetIndex++) {
            BakedTarget target = this.targets.get(targetIndex);
            variants[targetIndex] = this.resolveVariant(current, target);
            for (Direction face : Direction.values()) {
                masks[targetIndex][face.ordinal()] = target.appliesTo(face) ? this.computeMask(current, target, face) : target.fallbackMask();
            }
        }
        
        return new SLConnectedTextureRenderData(variants, masks);
    }
    
    private String resolveVariant(SLConnectedTextureContext current, BakedTarget target) {
        return target.target().resolveVariant(current);
    }
    
    private @SLConnectedTextureMask int computeMask(SLConnectedTextureContext current, BakedTarget target, Direction face) {
        boolean right = this.connects(current, target, face, SLConnectedTextureDirection.RIGHT);
        boolean top = this.connects(current, target, face, SLConnectedTextureDirection.TOP);
        boolean left = this.connects(current, target, face, SLConnectedTextureDirection.LEFT);
        boolean bottom = this.connects(current, target, face, SLConnectedTextureDirection.BOTTOM);
        
        @SLConnectedTextureMask int mask = SLConnectedTextureMasks.NONE;
        if (right) mask = SLConnectedTextureMasks.add(mask, SLConnectedTextureDirection.RIGHT.bit());
        if (top && right && this.connects(current, target, face, SLConnectedTextureDirection.TOP_RIGHT)) mask = SLConnectedTextureMasks.add(mask, SLConnectedTextureDirection.TOP_RIGHT.bit());
        if (top) mask = SLConnectedTextureMasks.add(mask, SLConnectedTextureDirection.TOP.bit());
        if (top && left && this.connects(current, target, face, SLConnectedTextureDirection.TOP_LEFT)) mask = SLConnectedTextureMasks.add(mask, SLConnectedTextureDirection.TOP_LEFT.bit());
        if (left) mask = SLConnectedTextureMasks.add(mask, SLConnectedTextureDirection.LEFT.bit());
        if (bottom && left && this.connects(current, target, face, SLConnectedTextureDirection.BOTTOM_LEFT)) mask = SLConnectedTextureMasks.add(mask, SLConnectedTextureDirection.BOTTOM_LEFT.bit());
        if (bottom) mask = SLConnectedTextureMasks.add(mask, SLConnectedTextureDirection.BOTTOM.bit());
        if (bottom && right && this.connects(current, target, face, SLConnectedTextureDirection.BOTTOM_RIGHT)) mask = SLConnectedTextureMasks.add(mask, SLConnectedTextureDirection.BOTTOM_RIGHT.bit());
        return mask;
    }
    
    private boolean connects(SLConnectedTextureContext current, BakedTarget target, Direction face, SLConnectedTextureDirection localDirection) {
        BlockPos neighborPos = current.pos().offset(localDirection.offset(face));
        BlockState neighborState = current.level().getBlockState(neighborPos);
        ModelData neighborData = current.level().getModelData(neighborPos);
        SLConnectedTextureContext neighbor = new SLConnectedTextureContext(current.level(), neighborPos, neighborState, neighborData);
        
        SLConnectedTextureRule rule = target.target().connectionRule();
        return rule.test(current, neighbor, face, target.target()) && rule.test(neighbor, current, face, target.target());
    }
    
    private record GeometryKey(@Nullable Object delegateKey, SLConnectedTextureRenderData renderData) { }
    
    private record BakedTarget(
            int index,
            TextureAtlasSprite matchSprite,
            Map<String, TextureAtlasSprite> variantSprites,
            SLConnectedTextureTarget target
    ) {
        
        private static BakedTarget bake(int index, SLConnectedTextureTarget target, MaterialBaker materialBaker, ModelDebugName debugName) {
            TextureAtlasSprite matchSprite = materialBaker.get(new Material(target.matchTexture()), debugName).sprite();
            Map<String, TextureAtlasSprite> variants = new LinkedHashMap<>();
            for (Map.Entry<String, Identifier> entry : target.variantTextures().entrySet()) {
                variants.put(entry.getKey(), materialBaker.get(new Material(entry.getValue()), debugName).sprite());
            }
            variants.putIfAbsent(SLConnectedTextureTarget.DEFAULT_VARIANT, matchSprite);
            return new BakedTarget(index, matchSprite, Map.copyOf(variants), target);
        }
        
        private boolean appliesTo(Direction face) {
            return this.target.appliesTo(face);
        }
        
        private boolean matches(BakedQuad quad) {
            return this.appliesTo(quad.direction()) && this.matchSprite == quad.materialInfo().sprite();
        }
        
        private TextureAtlasSprite variantSprite(String variant) {
            return this.variantSprites.getOrDefault(variant, this.variantSprites.get(SLConnectedTextureTarget.DEFAULT_VARIANT));
        }
        
        private SLConnectedTextureLayout layout() {
            return this.target.layout();
        }
        
        private @SLConnectedTextureMask int fallbackMask() {
            return this.target.fallbackMask();
        }
    }
}
