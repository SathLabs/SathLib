package dev.satherov.sathlib.client.render.model;

import net.neoforged.neoforge.client.model.quad.MutableQuad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;

/// UV remapping helpers for connected-texture quads.
public final class SLConnectedTextureQuadUtils {
    
    private SLConnectedTextureQuadUtils() { }
    
    /// Retextures a quad into the supplied atlas region.
    ///
    /// @param quad         source quad
    /// @param targetSprite sprite to sample from
    /// @param region       region inside the target sprite atlas
    ///
    /// @return retextured quad
    public static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite targetSprite, SLConnectedTextureRegion region) {
        MutableQuad mutableQuad = new MutableQuad().setFrom(quad);
        TextureAtlasSprite sourceSprite = quad.materialInfo().sprite();
        float sourceU0 = sourceSprite.getU0();
        float sourceV0 = sourceSprite.getV0();
        float sourceWidth = sourceSprite.getU1() - sourceU0;
        float sourceHeight = sourceSprite.getV1() - sourceV0;
        
        mutableQuad.setSprite(targetSprite, quad.materialInfo().layer(), quad.materialInfo().itemRenderType());
        for (int vertex = 0; vertex < BakedQuad.VERTEX_COUNT; vertex++) {
            float normalizedU = (mutableQuad.u(vertex) - sourceU0) / sourceWidth;
            float normalizedV = (mutableQuad.v(vertex) - sourceV0) / sourceHeight;
            float targetU = region.u0() + normalizedU * region.width();
            float targetV = region.v0() + normalizedV * region.height();
            mutableQuad.setUv(vertex, targetSprite.getU(targetU), targetSprite.getV(targetV));
        }
        return mutableQuad.toBakedQuad();
    }
}
