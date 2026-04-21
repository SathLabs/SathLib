package dev.satherov.sathlib.client.render.model;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Immutable definition of one connected-texture target within a model.
///
/// @param id               target identifier
/// @param matchTexture     base texture matched against source quads
/// @param variantTextures  atlas textures keyed by variant id
/// @param variantSelectors ordered selectors used to choose a variant
/// @param connectionRule   rule controlling block-to-block connectivity
/// @param faces            faces on which this target applies
/// @param layout           layout that maps bitmasks to sub-regions
/// @param fallbackMask     mask used when contextual connectivity cannot be
///                                                                         resolved
public record SLConnectedTextureTarget(
        String id,
        Identifier matchTexture,
        Map<String, Identifier> variantTextures,
        List<SLConnectedTextureVariantSelector> variantSelectors,
        SLConnectedTextureRule connectionRule,
        EnumSet<Direction> faces,
        SLConnectedTextureLayout layout,
        @SLConnectedTextureMask int fallbackMask
) {
    
    /// Default variant id.
    public static final String DEFAULT_VARIANT = "default";
    
    private static final Codec<EnumSet<Direction>> FACES_CODEC = Direction.CODEC.listOf().xmap(SLConnectedTextureTarget::toFaceSet, List::copyOf);
    private static final Codec<SLConnectedTextureLayout> LAYOUT_CODEC = Identifier.CODEC.comapFlatMap(SLConnectedTextureTarget::decodeLayout, SLConnectedTextureLayout::id);
    private static final Codec<SLConnectedTextureTarget> SIMPLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(SLConnectedTextureTarget::id),
            Identifier.CODEC.fieldOf("texture").forGetter(SLConnectedTextureTarget::matchTexture),
            SLConnectedTextureRule.CODEC.optionalFieldOf("connection_rule", SLConnectedTextureRules.sameBlock()).forGetter(SLConnectedTextureTarget::connectionRule),
            SLConnectedTextureTarget.FACES_CODEC.optionalFieldOf("faces", EnumSet.allOf(Direction.class)).forGetter(SLConnectedTextureTarget::faces),
            SLConnectedTextureTarget.LAYOUT_CODEC.optionalFieldOf("layout", SLBlob47ConnectedTextureLayout.INSTANCE).forGetter(SLConnectedTextureTarget::layout),
            Codec.INT.optionalFieldOf("fallback_mask", SLConnectedTextureMasks.NONE).forGetter(SLConnectedTextureTarget::fallbackMask)
    ).apply(instance, (id, texture, connectionRule, faces, layout, fallbackMask) -> new SLConnectedTextureTarget(
            id,
            texture,
            Map.of(SLConnectedTextureTarget.DEFAULT_VARIANT, texture),
            List.of(),
            connectionRule,
            faces,
            layout,
            fallbackMask
    )));
    private static final Codec<SLConnectedTextureTarget> PREDICATE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(SLConnectedTextureTarget::id),
            Identifier.CODEC.optionalFieldOf("match_texture").forGetter(SLConnectedTextureTarget::encodedMatchTexture),
            Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("variants").forGetter(SLConnectedTextureTarget::variantTextures),
            SLConnectedTextureVariantSelector.CODEC.listOf().optionalFieldOf("selectors", List.of()).forGetter(SLConnectedTextureTarget::variantSelectors),
            SLConnectedTextureRule.CODEC.optionalFieldOf("connection_rule", SLConnectedTextureRules.sameBlock()).forGetter(SLConnectedTextureTarget::connectionRule),
            SLConnectedTextureTarget.FACES_CODEC.optionalFieldOf("faces", EnumSet.allOf(Direction.class)).forGetter(SLConnectedTextureTarget::faces),
            SLConnectedTextureTarget.LAYOUT_CODEC.optionalFieldOf("layout", SLBlob47ConnectedTextureLayout.INSTANCE).forGetter(SLConnectedTextureTarget::layout),
            Codec.INT.optionalFieldOf("fallback_mask", SLConnectedTextureMasks.NONE).forGetter(SLConnectedTextureTarget::fallbackMask)
    ).apply(instance, SLConnectedTextureTarget::fromPredicateCodec));
    
    /// Creates a target definition.
    public SLConnectedTextureTarget {
        variantTextures = Map.copyOf(new LinkedHashMap<>(variantTextures));
        variantSelectors = List.copyOf(variantSelectors);
        faces = faces.isEmpty() ? EnumSet.allOf(Direction.class) : faces.clone();
        if (variantTextures.isEmpty()) {
            throw new IllegalArgumentException("Connected-texture targets must define at least one texture variant");
        }
    }
    
    /// Codec for simple connected-texture targets.
    ///
    /// @return target codec
    public static Codec<SLConnectedTextureTarget> simpleCodec() {
        return SLConnectedTextureTarget.SIMPLE_CODEC;
    }
    
    /// Codec for predicate-aware connected-texture targets.
    ///
    /// @return target codec
    public static Codec<SLConnectedTextureTarget> predicateCodec() {
        return SLConnectedTextureTarget.PREDICATE_CODEC;
    }
    
    /// Returns whether this target applies to the given face.
    ///
    /// @param face block face
    ///
    /// @return {@code true} when the target should be used
    public boolean appliesTo(Direction face) {
        return this.faces.contains(face);
    }
    
    /// Resolves the variant id for the supplied block context.
    ///
    /// @param context render context of the current block
    ///
    /// @return chosen variant id
    public String resolveVariant(SLConnectedTextureContext context) {
        for (SLConnectedTextureVariantSelector selector : this.variantSelectors) {
            if (selector.matches(context, this) && this.variantTextures.containsKey(selector.variant())) {
                return selector.variant();
            }
        }
        return this.variantTextures.containsKey(SLConnectedTextureTarget.DEFAULT_VARIANT) ? SLConnectedTextureTarget.DEFAULT_VARIANT : this.variantTextures.keySet().iterator().next();
    }
    
    private Optional<Identifier> encodedMatchTexture() {
        Identifier defaultTexture = this.variantTextures.get(SLConnectedTextureTarget.DEFAULT_VARIANT);
        if (this.matchTexture.equals(defaultTexture)) {
            return Optional.empty();
        }
        return Optional.of(this.matchTexture);
    }
    
    private static SLConnectedTextureTarget fromPredicateCodec(
            String id,
            Optional<Identifier> matchTexture,
            Map<String, Identifier> variants,
            List<SLConnectedTextureVariantSelector> selectors,
            SLConnectedTextureRule connectionRule,
            EnumSet<Direction> faces,
            SLConnectedTextureLayout layout,
            @SLConnectedTextureMask int fallbackMask
    ) {
        Identifier resolvedMatchTexture = matchTexture.orElseGet(() -> variants.getOrDefault(SLConnectedTextureTarget.DEFAULT_VARIANT, variants.values().iterator().next()));
        return new SLConnectedTextureTarget(id, resolvedMatchTexture, variants, selectors, connectionRule, faces, layout, fallbackMask);
    }
    
    private static EnumSet<Direction> toFaceSet(List<Direction> faces) {
        return faces.isEmpty() ? EnumSet.allOf(Direction.class) : EnumSet.copyOf(faces);
    }
    
    private static DataResult<SLConnectedTextureLayout> decodeLayout(Identifier layoutId) {
        if (SLBlob47ConnectedTextureLayout.ID.equals(layoutId)) {
            return DataResult.success(SLBlob47ConnectedTextureLayout.INSTANCE);
        }
        return DataResult.error(() -> "Unknown connected-texture layout " + layoutId);
    }
}
