package dev.satherov.sathlib.client.model;

import dev.satherov.sathlib.client.model.connected.ConnectedTextureBlockModel;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

public final class SLModelLoaders {
    
    private SLModelLoaders() { }
    
    public static void register(final IEventBus bus) {
        bus.addListener(RegisterBlockStateModels.class, event -> {
            event.registerModel(ConnectedTextureBlockModel.ID, ConnectedTextureBlockModel.Unbaked.MAP_CODEC);
        });
    }
}
