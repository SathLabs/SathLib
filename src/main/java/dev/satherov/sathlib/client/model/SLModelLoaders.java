package dev.satherov.sathlib.client.model;

import dev.satherov.sathlib.client.model.connected.ConnectedTextureBlockModel;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

///
/// Registers custom blockstate model loaders used by SathLib.
///
public final class SLModelLoaders {
    
    private SLModelLoaders() { }
    
    ///
    /// Registers all custom model loaders on the supplied event bus.
    ///
    /// @param bus mod event bus receiving loader registrations
    ///
    public static void register(final IEventBus bus) {
        bus.addListener(RegisterBlockStateModels.class, event -> {
            event.registerModel(ConnectedTextureBlockModel.ID, ConnectedTextureBlockModel.Unbaked.MAP_CODEC);
        });
    }
}
