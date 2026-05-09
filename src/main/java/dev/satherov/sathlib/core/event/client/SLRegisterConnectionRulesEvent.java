package dev.satherov.sathlib.core.event.client;

import lombok.RequiredArgsConstructor;

import dev.satherov.sathlib.client.model.connected.ConnectionPredicate;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@RequiredArgsConstructor(onConstructor_ = @ApiStatus.Internal)
public class SLRegisterConnectionRulesEvent extends Event implements IModBusEvent {
    
    private final Map<Identifier, MapCodec<? extends ConnectionPredicate>> rules;
    
    public void register(Identifier id, MapCodec<? extends ConnectionPredicate> codec) {
        if (this.rules.putIfAbsent(id, codec) != null) throw new IllegalStateException("Connection predicate with Identifier '" + id + "' is already registered");
    }
}
