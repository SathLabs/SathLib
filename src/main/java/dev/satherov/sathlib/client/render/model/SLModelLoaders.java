package dev.satherov.sathlib.client.render.model;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

/// Registers SathLib custom connected-texture blockstate models.
public final class SLModelLoaders {
    
    private SLModelLoaders() { }
    
    /// Registers the connected-texture model loaders on the supplied event bus.
    ///
    /// @param bus mod event bus
    public static void register(IEventBus bus) {
        bus.addListener(SLModelLoaders::onRegisterBlockStateModels);
    }
    
    private static void onRegisterBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(SLConnectedTextureModel.ID, SLConnectedTextureModel.MAP_CODEC);
        event.registerModel(SLPredicateConnectedTextureModel.ID, SLPredicateConnectedTextureModel.MAP_CODEC);
    }
}
