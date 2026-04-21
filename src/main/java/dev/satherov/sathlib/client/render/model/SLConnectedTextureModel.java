package dev.satherov.sathlib.client.render.model;

import dev.satherov.sathlib.SathLib;

import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/// Simple connected-texture blockstate model using a single atlas per target.
///
/// This is the runtime representation produced by
/// {@link SLConnectedTextureModelBuilder}. Each target keeps one atlas and one
/// connection rule, so texture changes come entirely from the computed
/// connectivity mask.
///
/// @param baseModel wrapped base blockstate variant
/// @param targets   connected-texture targets evaluated at runtime
public record SLConnectedTextureModel(Variant baseModel, List<SLConnectedTextureTarget> targets) implements CustomUnbakedBlockStateModel {
    
    /// Registered loader id for the simple connected-texture model.
    public static final Identifier ID = SathLib.id("connected_texture");
    /// Codec used to serialize and deserialize the model definition.
    public static final MapCodec<SLConnectedTextureModel> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Variant.CODEC.fieldOf("base_model").forGetter(SLConnectedTextureModel::baseModel),
            SLConnectedTextureTarget.simpleCodec().listOf().fieldOf("targets").forGetter(SLConnectedTextureModel::targets)
    ).apply(instance, SLConnectedTextureModel::new));
    
    /// Creates a simple connected-texture model definition.
    ///
    /// The target list is copied defensively so the record remains immutable
    /// even if the caller mutates the original list.
    public SLConnectedTextureModel {
        targets = List.copyOf(targets);
    }
    
    /// Bakes the model into a runtime wrapper around the configured base model.
    ///
    /// @param modelBakery model baker provided by the client model pipeline
    ///
    /// @return baked blockstate model
    @Override
    public BlockStateModel bake(ModelBaker modelBakery) {
        return SLConnectedTextureBlockStateModel.bake(this.baseModel, this.targets, modelBakery, () -> "sathlib:connected_texture[" + this.baseModel.modelLocation() + "]");
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
        return SLConnectedTextureModel.MAP_CODEC;
    }
}
