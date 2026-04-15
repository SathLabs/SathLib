package dev.satherov.sathlib.common.item;

import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

@NothingNull
public class SLItem extends Item {
    
    public SLItem(SLItemProperties properties) {
        super(properties);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) { }
    
    @Override
    @SuppressWarnings("deprecation")
    public Holder.Reference<Item> builtInRegistryHolder() {
        return super.builtInRegistryHolder();
    }
}
