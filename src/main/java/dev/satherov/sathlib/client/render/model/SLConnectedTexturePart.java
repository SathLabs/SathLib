package dev.satherov.sathlib.client.render.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import org.jspecify.annotations.Nullable;

import java.util.List;

/// Block-model part containing already transformed connected-texture quads.
///
/// @param quads               baked quad collection for the part
/// @param useAmbientOcclusion whether ambient occlusion should be applied
/// @param particleMaterial    particle material for the part
public record SLConnectedTexturePart(
        QuadCollection quads,
        boolean useAmbientOcclusion,
        Material.Baked particleMaterial
) implements BlockStateModelPart {
    
    /// Returns the quads for the requested cull face.
    ///
    /// @param direction cull face, or {@code null} for unculled quads
    ///
    /// @return quad list for the requested face bucket
    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.getQuads(direction);
    }
    
    /// Returns the packed material flags of the underlying quad collection.
    ///
    /// @return material flags used by the renderer
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.quads.materialFlags();
    }
}
