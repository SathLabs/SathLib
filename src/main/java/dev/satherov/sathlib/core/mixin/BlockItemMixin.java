package dev.satherov.sathlib.core.mixin;

import dev.satherov.sathlib.common.block.SLBlock;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

@NothingNull
@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
    
    public BlockItemMixin(Properties properties) {
        super(properties);
    }
    
    @Unique
    private BlockItem sathlib$self() {
        return (BlockItem) (Object) this;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flags) {
        if (this.sathlib$self().getBlock() instanceof SLBlock block) block.appendHoverText(stack, context, display, builder, flags);
    }
}
