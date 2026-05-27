package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.Fluid;

import org.jspecify.annotations.NonNull;

import java.util.Optional;

///
/// Utility for fluid-related stuff
///
@UtilityClass
public class SLFluidUtils {
    
    ///
    /// Retrieves the tint color of the given fluid stack
    ///
    /// @param stack The fluid stack to retrieve the tint color for
    ///
    /// @return The tint color of the fluid stack, or `0xFFFFFFFF` if no tint is available
    ///
    public static int getTintColor(@NonNull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        Minecraft mc = Minecraft.getInstance();
        ModelManager manager = mc.getModelManager();
        FluidStateModelSet set = manager.getFluidStateModelSet();
        FluidModel model = set.get(fluid.defaultFluidState());
        FluidTintSource tint = model.fluidTintSource();
        if (tint == null) return 0xFFFFFFFF;
        return tint.colorAsStack(stack);
    }
    
    ///
    /// Retrieves the still fluid sprite of the given fluid.
    ///
    /// @param stack The fluid stack to retrieve the sprite for
    ///
    /// @return The still fluid sprite of the fluid stack, or empty if no sprite is available
    ///
    public static Optional<TextureAtlasSprite> getStillFluidSprite(@NonNull FluidStack stack) {
        final Fluid fluid = stack.getFluid();
        final Minecraft mc = Minecraft.getInstance();
        final ModelManager manager = mc.getModelManager();
        final FluidStateModelSet set = manager.getFluidStateModelSet();
        final FluidModel model = set.get(fluid.defaultFluidState());
        final Material.Baked material = model.stillMaterial();
        final TextureAtlasSprite sprite = material.sprite();
        // noinspection OptionalOfNullableMisuse
        return Optional.ofNullable(sprite).filter(s -> s.atlasLocation() != MissingTextureAtlasSprite.getLocation());
    }
}
