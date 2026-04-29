package dev.satherov.sathlib.client.model.connected;

import dev.satherov.sathlib.SathLib;

import net.minecraft.resources.Identifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import org.jspecify.annotations.Nullable;

public interface SLConnectedTextureRule extends ConnectedTexturePredicate {
    
    Identifier TYPE_ALL = SathLib.id("all");
    Identifier TYPE_ANY = SathLib.id("any");
    Identifier TYPE_NOT = SathLib.id("not");
    Identifier TYPE_SAME_BLOCK = SathLib.id("same_block");
    Identifier TYPE_SAME_APPEARANCE_BLOCK = SathLib.id("same_appearance_block");
    Identifier TYPE_SAME_STATE = SathLib.id("same_state");
    Identifier TYPE_ORIGIN_STATE = SathLib.id("origin_state");
    Identifier TYPE_NEIGHBOR_STATE = SathLib.id("neighbor_state");
    Identifier TYPE_ALWAYS = SathLib.id("always");
    Identifier TYPE_NEVER = SathLib.id("never");
    
    Codec<SLConnectedTextureRule> CODEC = Codec.recursive("sathlib_connected_texture_rule", self -> Identifier.CODEC.partialDispatch(
            "type",
            rule -> DataResult.success(rule.type()),
            type -> {
                final MapCodec<? extends SLConnectedTextureRule> codec = SLConnectedTextureRule.codec(type, self);
                return codec != null
                        ? DataResult.success(codec)
                        : DataResult.<MapCodec<? extends SLConnectedTextureRule>>error(() -> "Unknown connected texture rule type: " + type);
            }
    ));
    
    Identifier type();
    
    @Nullable
    private static MapCodec<? extends SLConnectedTextureRule> codec(final Identifier type, final Codec<SLConnectedTextureRule> self) {
        if (SLConnectedTextureRule.TYPE_ALL.equals(type)) return SLConnectedTextureRules.All.CODEC(self);
        if (SLConnectedTextureRule.TYPE_ANY.equals(type)) return SLConnectedTextureRules.Any.CODEC(self);
        if (SLConnectedTextureRule.TYPE_NOT.equals(type)) return SLConnectedTextureRules.Not.CODEC(self);
        if (SLConnectedTextureRule.TYPE_SAME_BLOCK.equals(type)) return SLConnectedTextureRules.SameBlock.CODEC;
        if (SLConnectedTextureRule.TYPE_SAME_APPEARANCE_BLOCK.equals(type)) return SLConnectedTextureRules.SameAppearanceBlock.CODEC;
        if (SLConnectedTextureRule.TYPE_SAME_STATE.equals(type)) return SLConnectedTextureRules.SameState.CODEC;
        if (SLConnectedTextureRule.TYPE_ORIGIN_STATE.equals(type)) return SLConnectedTextureRules.OriginState.CODEC;
        if (SLConnectedTextureRule.TYPE_NEIGHBOR_STATE.equals(type)) return SLConnectedTextureRules.NeighborState.CODEC;
        if (SLConnectedTextureRule.TYPE_ALWAYS.equals(type)) return SLConnectedTextureRules.Always.CODEC;
        if (SLConnectedTextureRule.TYPE_NEVER.equals(type)) return SLConnectedTextureRules.Never.CODEC;
        return null;
    }
}
