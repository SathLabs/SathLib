package dev.satherov.sathlib.core.event.client;

import lombok.RequiredArgsConstructor;

import dev.satherov.sathlib.client.model.conditional.ConditionalPredicate;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@RequiredArgsConstructor(onConstructor_ = @ApiStatus.Internal)
public class SLRegisterConditionRulesEvent extends Event implements IModBusEvent {
    
    private final Map<Identifier, MapCodec<? extends ConditionalPredicate>> rules;
    
    public void register(Identifier id, MapCodec<? extends ConditionalPredicate> codec) {
        if (this.rules.putIfAbsent(id, codec) != null) throw new IllegalStateException("Conditional predicate with Identifier '" + id + "' is already registered");
    }
}
