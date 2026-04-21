package dev.satherov.sathlib.client.render.model;

import net.minecraft.resources.Identifier;

/// Layout describing how a connected-texture atlas maps a neighbor mask to a
/// specific region inside the texture.
public interface SLConnectedTextureLayout {
    
    /// Returns the serialized identifier for this layout.
    ///
    /// @return layout identifier
    Identifier id();
    
    /// Resolves the atlas region for the supplied connection mask.
    ///
    /// @param mask normalized or raw connected-texture mask
    ///
    /// @return atlas region for the mask
    SLConnectedTextureRegion regionForMask(@SLConnectedTextureMask int mask);
}
