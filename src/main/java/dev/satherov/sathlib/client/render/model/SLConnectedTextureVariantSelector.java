package dev.satherov.sathlib.client.render.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/// Ordered predicate selecting a variant id for a connected-texture target.
///
/// Selectors are evaluated in declaration order. The first selector whose
/// predicate matches the current block context provides the active variant id.
/// If none match, the target falls back to
/// {@value SLConnectedTextureTarget#DEFAULT_VARIANT} or the first declared
/// variant.
///
/// @param variant   variant id to apply when the predicate matches
/// @param predicate predicate evaluated against the currently rendered block
public record SLConnectedTextureVariantSelector(String variant, SLConnectedTextureRule predicate) {
    
    /// Codec for connected-texture variant selectors.
    public static final Codec<SLConnectedTextureVariantSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("variant").forGetter(SLConnectedTextureVariantSelector::variant),
            SLConnectedTextureRule.CODEC.fieldOf("predicate").forGetter(SLConnectedTextureVariantSelector::predicate)
    ).apply(instance, SLConnectedTextureVariantSelector::new));
    
    /// Creates a variant selector.
    ///
    /// @param variant   variant id to select
    /// @param predicate predicate deciding when to select it
    ///
    /// @return selector instance
    public static SLConnectedTextureVariantSelector of(String variant, SLConnectedTextureRule predicate) {
        return new SLConnectedTextureVariantSelector(variant, predicate);
    }
    
    /// Tests whether this selector matches the current block context.
    ///
    /// The selector predicate is evaluated in a current-only mode, so the
    /// same context is supplied as both {@code current} and {@code neighbor}.
    /// This keeps variant selectors compatible with the general
    /// {@link SLConnectedTextureRule} abstraction while still allowing them to
    /// focus on the rendered block.
    ///
    /// @param context render context of the currently rendered block
    /// @param target  target being evaluated
    ///
    /// @return {@code true} when this selector should win
    public boolean matches(SLConnectedTextureContext context, SLConnectedTextureTarget target) {
        return this.predicate.test(context, context, net.minecraft.core.Direction.NORTH, target);
    }
}
