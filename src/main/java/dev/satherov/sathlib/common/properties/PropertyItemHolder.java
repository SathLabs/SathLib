package dev.satherov.sathlib.common.properties;

import net.minecraft.world.item.ItemStack;

///
/// Holder for an item stack
///
/// @param stack the item stack
///
/// @see PropertyBlockHolder
///
public record PropertyItemHolder(ItemStack stack) implements PropertyHolder { }
