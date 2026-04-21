package dev.satherov.sathlib.client.render.model;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/// Predicate-aware connected-texture blockstate model that can swap atlases
/// while preserving the connected-texture lookup.
///
/// This is the runtime representation produced by
/// {@link SLPredicateConnectedTextureModelBuilder}. Each target can choose from
/// multiple variant atlases through ordered predicate selectors, while still
/// computing the same connected-texture mask per face.
///
/// @param baseModel wrapped base blockstate variant
/// @param targets   connected-texture targets evaluated at runtime
@NothingNull
public record SLPredicateConnectedTextureModel(Variant baseModel, List<SLConnectedTextureTarget> targets) implements CustomUnbakedBlockStateModel {
    
    /// Registered loader id for the predicate-aware connected-texture model.
    public static final Identifier ID = SathLib.id("predicate_connected_texture");
    /// Codec used to serialize and deserialize the model definition.
    public static final MapCodec<SLPredicateConnectedTextureModel> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Variant.CODEC.fieldOf("base_model").forGetter(SLPredicateConnectedTextureModel::baseModel),
            SLConnectedTextureTarget.predicateCodec().listOf().fieldOf("targets").forGetter(SLPredicateConnectedTextureModel::targets)
    ).apply(instance, SLPredicateConnectedTextureModel::new));
    
    /// Creates a predicate-aware connected-texture model definition.
    ///
    /// The target list is copied defensively so the record remains immutable
    /// even if the caller mutates the original list.
    public SLPredicateConnectedTextureModel {
        targets = List.copyOf(targets);
    }
    
    /// Bakes the model into a runtime wrapper around the configured base model.
    ///
    /// @param modelBakery model baker provided by the client model pipeline
    ///
    /// @return baked blockstate model
    @Override
    public BlockStateModel bake(ModelBaker modelBakery) {
        return SLConnectedTextureBlockStateModel.bake(this.baseModel, this.targets, modelBakery, () -> SLPredicateConnectedTextureModel.ID + "[" + this.baseModel.modelLocation() + "]");
    }
    
    /// Resolves dependencies of the wrapped base variant.
    ///
    /// @param resolver model dependency resolver
    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.baseModel.resolveDependencies(resolver);
    }
    
    /// Returns the codec registered for this custom model type.
    ///
    /// @return model codec
    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return SLPredicateConnectedTextureModel.MAP_CODEC;
    }
}
