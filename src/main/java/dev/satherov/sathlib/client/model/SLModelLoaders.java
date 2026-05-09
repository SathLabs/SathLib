package dev.satherov.sathlib.client.model;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.client.model.conditional.ConditionalBlockModel;
import dev.satherov.sathlib.client.model.conditional.ConditionalRules;
import dev.satherov.sathlib.client.model.connected.ConnectedTextureBlockModel;
import dev.satherov.sathlib.client.model.connected.ConnectionRules;
import dev.satherov.sathlib.core.event.client.SLRegisterConditionRulesEvent;
import dev.satherov.sathlib.core.event.client.SLRegisterConnectionRulesEvent;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

@UtilityClass
public class SLModelLoaders {
    
    public static void register(final IEventBus bus) {
        
        bus.addListener(RegisterBlockStateModels.class, event -> {
            event.registerModel(ConnectedTextureBlockModel.ID, ConnectedTextureBlockModel.Unbaked.MAP_CODEC);
            event.registerModel(ConditionalBlockModel.ID, ConditionalBlockModel.Unbaked.MAP_CODEC);
        });
        
        bus.addListener(SLRegisterConnectionRulesEvent.class, event -> {
            event.register(ConnectionRules.ALWAYS, ConnectionRules.Always.CODEC);
            event.register(ConnectionRules.NEVER, ConnectionRules.Never.CODEC);
            event.register(ConnectionRules.ALL, ConnectionRules.All.CODEC);
            event.register(ConnectionRules.ANY, ConnectionRules.Any.CODEC);
            event.register(ConnectionRules.NOT, ConnectionRules.Not.CODEC);
            event.register(ConnectionRules.SAME_BLOCK, ConnectionRules.SameBlock.CODEC);
            event.register(ConnectionRules.SAME_STATE, ConnectionRules.SameState.CODEC);
            event.register(ConnectionRules.SAME_STATE_PROPERTY, ConnectionRules.SameStateProperty.CODEC);
            event.register(ConnectionRules.ORIGIN_STATE_PROPERTY, ConnectionRules.OriginStateProperty.CODEC);
            event.register(ConnectionRules.NEIGHBOR_STATE_PROPERTY, ConnectionRules.NeighborStateProperty.CODEC);
            event.register(ConnectionRules.SAME_MODEL_PROPERTY, ConnectionRules.SameModelProperty.CODEC);
            event.register(ConnectionRules.ORIGIN_MODEL_PROPERTY, ConnectionRules.OriginModelProperty.CODEC);
            event.register(ConnectionRules.NEIGHBOUR_MODEL_PROPERTY, ConnectionRules.NeighbourModelProperty.CODEC);
        });
        
        bus.addListener(SLRegisterConditionRulesEvent.class, event -> {
            event.register(ConditionalRules.ALWAYS, ConditionalRules.Always.CODEC);
            event.register(ConditionalRules.ANY, ConditionalRules.Any.CODEC);
            event.register(ConditionalRules.ALL, ConditionalRules.All.CODEC);
            event.register(ConditionalRules.NOT, ConditionalRules.Not.CODEC);
            event.register(ConditionalRules.STATE, ConditionalRules.State.CODEC);
            event.register(ConditionalRules.MODEL_PROPERTY, ConditionalRules.ModelProperties.CODEC);
        });
    }
}
