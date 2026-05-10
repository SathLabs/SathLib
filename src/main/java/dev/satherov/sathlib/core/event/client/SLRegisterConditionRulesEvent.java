package dev.satherov.sathlib.core.event.client;

import lombok.RequiredArgsConstructor;

import dev.satherov.sathlib.client.model.conditional.ConditionalPredicate;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

///
/// Mod-bus event used to register conditional block-model predicates.
///
@RequiredArgsConstructor(onConstructor_ = @ApiStatus.Internal)
@SuppressWarnings("doclint:missing")
public class SLRegisterConditionRulesEvent extends Event implements IModBusEvent {
    
    private final Map<Identifier, MapCodec<? extends ConditionalPredicate>> rules;
    
    ///
    /// Registers one conditional predicate codec.
    ///
    /// @param id    predicate identifier
    /// @param codec predicate codec
    ///
    public void register(Identifier id, MapCodec<? extends ConditionalPredicate> codec) {
        if (this.rules.putIfAbsent(id, codec) != null) throw new IllegalStateException("Conditional predicate with Identifier '" + id + "' is already registered");
    }
}
