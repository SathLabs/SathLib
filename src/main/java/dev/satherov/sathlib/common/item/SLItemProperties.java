package dev.satherov.sathlib.common.item;

import dev.satherov.sathlib.common.block.SLBlockProperties;
import dev.satherov.sathlib.core.annotations.NothingNull;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.Supplier;

///
/// {@link Item.Properties} extension with some extra helpers.
///
@NothingNull
public class SLItemProperties extends Item.Properties {
    
    private SLItemProperties() { }
    
    ///
    /// Creates a new empty {@link SLItemProperties} instance.
    ///
    /// @return New {@link SLItemProperties} instance.
    ///
    public static SLItemProperties create() {
        return new SLItemProperties();
    }
    
    ///
    /// Creates a new {@link SLItemProperties} instance with the {@link ResourceKey}
    /// created from the given {@link Identifier}
    ///
    /// - Sets {@link Item.Properties#id} to `ResourceKey.create(Registries.ITEM, identifier)`
    ///
    /// @param identifier Identifier of the item.
    ///
    /// @return New {@link SLItemProperties} instance.
    ///
    public static SLItemProperties create(Identifier identifier) {
        return new SLItemProperties().setId(identifier);
    }
    
    ///
    /// Creates a new {@link SLItemProperties} instance with the given {@link ResourceKey}
    ///
    /// - Sets {@link Item.Properties#id} to `key`
    ///
    /// @param key Resource key of the item.
    ///
    /// @return New {@link SLItemProperties} instance.
    ///
    public static SLItemProperties create(ResourceKey<Item> key) {
        return new SLItemProperties().setId(key);
    }
    
    ///
    /// Disables repairing this item by combining two damaged copies.
    ///
    /// - Sets {@link Item#canCombineRepair} to `false`
    ///
    /// Defaults to `true`
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties setNoCombineRepair() {
        super.setNoCombineRepair();
        return this;
    }
    
    ///
    /// Affects the nutrition and saturation values of this item, if it can always be eaten and
    /// the behavior while consuming it, such as animation and duration.
    ///
    /// - Sets {@link DataComponents#FOOD} to `food`
    /// - Sets {@link DataComponents#CONSUMABLE} to {@link Consumables#DEFAULT_FOOD}
    ///
    /// Defaults to `nothing`
    ///
    /// @param food Food properties of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties food(FoodProperties food) {
        super.food(food);
        return this;
    }
    
    ///
    /// Affects the nutrition and saturation values of this item, if it can always be eaten and
    /// the behavior while consuming it, such as animation and duration.
    ///
    /// - Sets {@link DataComponents#FOOD} to `foodProperties`
    /// - Sets {@link DataComponents#CONSUMABLE} to `consumable`
    ///
    /// Defaults to `nothing`
    ///
    /// @param foodProperties Food properties of this item.
    /// @param consumable     Consumable behavior of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties food(FoodProperties foodProperties, Consumable consumable) {
        super.food(foodProperties, consumable);
        return this;
    }
    
    ///
    /// Affects into which item this item turns after being used, such as a potion
    /// turning into an empty bottle.
    ///
    /// - Sets {@link DataComponents#USE_REMAINDER} to `item`
    ///
    /// Defaults to `nothing`
    ///
    /// @param item The item this item converts into after use.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties usingConvertsTo(Item item) {
        super.usingConvertsTo(item);
        return this;
    }
    
    ///
    /// Affects how long this item is on cooldown after being used.
    ///
    /// - Sets the {@link DataComponents#USE_COOLDOWN} to `seconds`
    ///
    /// Defaults to `nothing`
    ///
    /// @param seconds Cooldown length in seconds.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties useCooldown(float seconds) {
        super.useCooldown(seconds);
        return this;
    }
    
    ///
    /// Affects how many of these items can be stacked in a single slot.
    ///
    /// If {@link SLItemProperties#durability(int)} is called or this item has the
    /// {@link DataComponents#DAMAGE} component, the stack size must be `1`!
    ///
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `maxStackSize`
    ///
    /// Defaults to `64` Max value is `99`
    ///
    /// @param maxStackSize Maximum stack size of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties stacksTo(int maxStackSize) {
        super.stacksTo(maxStackSize);
        return this;
    }
    
    /// Affects how many times this item can be damaged before breaking.
    /// Items which have durability cannot be stacked.
    ///
    /// - Sets {@link DataComponents#MAX_DAMAGE} to `maxDamage`
    /// - Sets {@link DataComponents#DAMAGE} to `0`
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    ///
    /// Defaults to `nothing`
    ///
    /// @param maxDamage Maximum durability of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties durability(int maxDamage) {
        super.durability(maxDamage);
        return this;
    }
    
    ///
    /// Affects the item that remains in the crafting grid after this item has been used,
    /// such as a water bucket turning into an empty bucket.
    ///
    /// - Sets {@link Item#craftingRemainingItem} to `craftingRemainingItem`
    ///
    /// @param craftingRemainingItem The {@link ItemStackTemplate} remaining in the crafting grid.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties craftRemainder(ItemStackTemplate craftingRemainingItem) {
        super.craftRemainder(craftingRemainingItem);
        return this;
    }
    
    ///
    /// Affects the item that remains in the crafting grid after this item has been used,
    /// such as a water bucket turning into an empty bucket.
    ///
    /// - Sets {@link Item#craftingRemainingItem} to `new ItemStackTemplate(craftingRemainingItem)`
    ///
    /// @param craftingRemainingItem The {@link Item} remaining in the crafting grid.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties craftRemainder(Item craftingRemainingItem) {
        super.craftRemainder(craftingRemainingItem);
        return this;
    }
    
    ///
    /// Sets the {@link Rarity} of this item. Affects the display color
    ///
    /// - Sets {@link DataComponents#RARITY} to `rarity`
    ///
    /// Defaults to {@link Rarity#COMMON}
    ///
    /// @param rarity Rarity of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties rarity(Rarity rarity) {
        super.rarity(rarity);
        return this;
    }
    
    ///
    /// Makes this item resistant to fire damage, making it unable to burn in lava or fire.
    ///
    /// - Sets {@link DataComponents#DAMAGE_RESISTANT} to {@link DamageTypeTags#IS_FIRE}
    ///
    /// Defaults to `nothing`
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties fireResistant() {
        super.fireResistant();
        return this;
    }
    
    ///
    /// Defines the {@link ResourceKey} of the {@link JukeboxSong} that plays when this
    /// item is placed in a jukebox.
    ///
    /// - Sets {@link DataComponents#JUKEBOX_PLAYABLE} to `song`
    ///
    /// Defaults to `nothing`
    ///
    /// @param song {@link ResourceKey} of the {@link JukeboxSong} that plays when this item is placed in a jukebox.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties jukeboxPlayable(ResourceKey<JukeboxSong> song) {
        super.jukeboxPlayable(song);
        return this;
    }
    
    ///
    /// Marks this item as enchantable with the given enchantability value.
    ///
    /// - Sets {@link DataComponents#ENCHANTABLE} to `value`
    ///
    /// Defaults to `nothing`
    ///
    /// @param value Enchantability value of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties enchantable(int value) {
        super.enchantable(value);
        return this;
    }
    
    ///
    /// Sets the {@link Item} which can be used to repeair this item in an anvil.
    ///
    /// - Sets {@link DataComponents#REPAIRABLE} to `repairItem`
    ///
    /// Defaults to `nothing`
    ///
    /// @param repairItem {@link Item} that can be used to repair this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties repairable(Item repairItem) {
        super.repairable(repairItem);
        return this;
    }
    
    ///
    /// Sets the {@link TagKey} which can be used to repeair this item in an anvil.
    ///
    /// - Sets {@link DataComponents#REPAIRABLE} to `repairItems`
    ///
    /// Defaults to `nothing`
    ///
    /// @param repairItems {@link TagKey} of items that can be used to repair this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties repairable(TagKey<Item> repairItems) {
        super.repairable(repairItems);
        return this;
    }
    
    ///
    /// Makes this item equippable in the given {@link EquipmentSlot}
    /// Allows swapping this item with another item already equipped in the given slot,
    /// such as a chestplate with an elytra.
    ///
    /// - Sets {@link DataComponents#EQUIPPABLE} to `slot`
    ///
    /// Defaults to `nothing`
    ///
    /// @param slot Equipment slot this item can be equipped in.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties equippable(EquipmentSlot slot) {
        super.equippable(slot);
        return this;
    }
    
    ///
    /// Makes this item equippable in the given {@link EquipmentSlot}
    /// Does not allow swapping this item with another item already equipped in the given slot.
    ///
    /// - Sets {@link DataComponents#EQUIPPABLE} to `slot`
    ///
    /// Defaults to `nothing`
    ///
    /// @param slot Equipment slot this item can be equipped in.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties equippableUnswappable(EquipmentSlot slot) {
        super.equippableUnswappable(slot);
        return this;
    }
    
    ///
    /// Applies tool properties to this item using the given parameters.
    ///
    /// - Sets {@link DataComponents#TOOL} from:
    ///     - {@link List#of()} from:
    ///         - {@link Tool.Rule#deniesDrops(HolderSet)} to {@link ToolMaterial#incorrectBlocksForDrops()}
    ///         - {@link Tool.Rule#minesAndDrops(HolderSet, float)} to `minesEfficiently` and {@link ToolMaterial#speed()}
    ///     - {@link Tool#defaultMiningSpeed()} to `1.0F`
    ///     - {@link Tool#damagePerBlock()} to `1`
    ///     - {@link Tool#canDestroyBlocksInCreative()} to `true`
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ToolMaterial#durability()}
    /// - Sets {@link DataComponents#DAMAGE} to `0`
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    /// - Sets {@link DataComponents#ENCHANTABLE} to {@link ToolMaterial#enchantmentValue()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ToolMaterial#repairItems()}
    /// - Sets {@link DataComponents#WEAPON} to:
    ///     - {@link Weapon#itemDamagePerAttack()} to `2`
    ///     - {@link Weapon#disableBlockingForSeconds()} to `disableBlockingSeconds`
    ///
    /// - Adds {@link Attributes#ATTACK_DAMAGE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to {@link ToolMaterial#attackDamageBonus()} `+` `attackDamageBaseline`
    /// - Adds {@link Attributes#ATTACK_SPEED} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to `attackSpeedBaseline`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material               Tool material of this item.
    /// @param minesEfficiently       {@link TagKey} of blocks that this item can mine efficiently. If a block has {@link SLBlockProperties#requiresCorrectToolForDrops()} it will not drop when mined with this tool unless included in this tag.
    /// @param attackDamageBaseline   Attack-damage of this item before material value or other bonus calculations.
    /// @param attackSpeedBaseline    Attack-speed of this item before material value or other bonus calculations.
    /// @param disableBlockingSeconds Time in seconds that this item will disable the ability to use a shield when hitting a player with a critical hit.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    /// @throws IllegalStateException if {@link DataComponents#MAX_STACK_SIZE} is greater than `1`
    ///
    @Override
    public SLItemProperties tool(
            ToolMaterial material,
            TagKey<Block> minesEfficiently,
            float attackDamageBaseline,
            float attackSpeedBaseline,
            float disableBlockingSeconds
    ) {
        super.tool(material, minesEfficiently, attackDamageBaseline, attackSpeedBaseline, disableBlockingSeconds);
        return this;
    }
    
    ///
    /// Applies tool properties to this item using the given parameters.
    /// Uses pickaxe-specific defaults.
    ///
    /// - Sets {@link DataComponents#TOOL} from:
    ///     - {@link List#of()} from:
    ///         - {@link Tool.Rule#deniesDrops(HolderSet)} to {@link ToolMaterial#incorrectBlocksForDrops()}
    ///         - {@link Tool.Rule#minesAndDrops(HolderSet, float)} to {@link BlockTags#MINEABLE_WITH_PICKAXE} and {@link ToolMaterial#speed()}
    ///     - {@link Tool#defaultMiningSpeed()} to `1.0F`
    ///     - {@link Tool#damagePerBlock()} to `1`
    ///     - {@link Tool#canDestroyBlocksInCreative()} to `true`
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ToolMaterial#durability()}
    /// - Sets {@link DataComponents#DAMAGE} to `0`
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    /// - Sets {@link DataComponents#ENCHANTABLE} to {@link ToolMaterial#enchantmentValue()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ToolMaterial#repairItems()}
    /// - Sets {@link DataComponents#WEAPON} to:
    ///     - {@link Weapon#itemDamagePerAttack()} to `2`
    ///     - {@link Weapon#disableBlockingForSeconds()} to `0.0F`
    ///
    /// - Adds {@link Attributes#ATTACK_DAMAGE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to {@link ToolMaterial#attackDamageBonus()} `+` `attackDamageBaseline`
    /// - Adds {@link Attributes#ATTACK_SPEED} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to `attackSpeedBaseline`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material             Tool material of this item.
    /// @param attackDamageBaseline Attack-damage of this item before material value or other bonus calculations.
    /// @param attackSpeedBaseline  Attack-speed of this item before material value or other bonus calculations.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties pickaxe(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline) {
        super.pickaxe(material, attackDamageBaseline, attackSpeedBaseline);
        return this;
    }
    
    ///
    /// Applies tool properties to this item using the given parameters.
    /// Uses axe-specific defaults.
    ///
    /// - Sets {@link DataComponents#TOOL} from:
    ///     - {@link List#of()} from:
    ///         - {@link Tool.Rule#deniesDrops(HolderSet)} to {@link ToolMaterial#incorrectBlocksForDrops()}
    ///         - {@link Tool.Rule#minesAndDrops(HolderSet, float)} to {@link BlockTags#MINEABLE_WITH_AXE} and {@link ToolMaterial#speed()}
    ///     - {@link Tool#defaultMiningSpeed()} to `1.0F`
    ///     - {@link Tool#damagePerBlock()} to `1`
    ///     - {@link Tool#canDestroyBlocksInCreative()} to `true`
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ToolMaterial#durability()}
    /// - Sets {@link DataComponents#DAMAGE} to `0`
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    /// - Sets {@link DataComponents#ENCHANTABLE} to {@link ToolMaterial#enchantmentValue()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ToolMaterial#repairItems()}
    /// - Sets {@link DataComponents#WEAPON} to:
    ///     - {@link Weapon#itemDamagePerAttack()} to `2`
    ///     - {@link Weapon#disableBlockingForSeconds()} to `5.0F`
    ///
    /// - Adds {@link Attributes#ATTACK_DAMAGE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to {@link ToolMaterial#attackDamageBonus()} `+` `attackDamageBaseline`
    /// - Adds {@link Attributes#ATTACK_SPEED} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to `attackSpeedBaseline`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material             Tool material of this item.
    /// @param attackDamageBaseline Attack-damage of this item before material value or other bonus calculations.
    /// @param attackSpeedBaseline  Attack-speed of this item before material value or other bonus calculations.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties axe(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline) {
        super.axe(material, attackDamageBaseline, attackSpeedBaseline);
        return this;
    }
    
    ///
    /// Applies tool properties to this item using the given parameters.
    /// Uses hoe-specific defaults.
    ///
    /// - Sets {@link DataComponents#TOOL} from:
    ///     - {@link List#of()} from:
    ///         - {@link Tool.Rule#deniesDrops(HolderSet)} to {@link ToolMaterial#incorrectBlocksForDrops()}
    ///         - {@link Tool.Rule#minesAndDrops(HolderSet, float)} to {@link BlockTags#MINEABLE_WITH_HOE} and {@link ToolMaterial#speed()}
    ///     - {@link Tool#defaultMiningSpeed()} to `1.0F`
    ///     - {@link Tool#damagePerBlock()} to `1`
    ///     - {@link Tool#canDestroyBlocksInCreative()} to `true`
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ToolMaterial#durability()}
    /// - Sets {@link DataComponents#DAMAGE} to `0`
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    /// - Sets {@link DataComponents#ENCHANTABLE} to {@link ToolMaterial#enchantmentValue()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ToolMaterial#repairItems()}
    /// - Sets {@link DataComponents#WEAPON} to:
    ///     - {@link Weapon#itemDamagePerAttack()} to `2`
    ///     - {@link Weapon#disableBlockingForSeconds()} to `0.0F`
    ///
    /// - Adds {@link Attributes#ATTACK_DAMAGE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to {@link ToolMaterial#attackDamageBonus()} `+` `attackDamageBaseline`
    /// - Adds {@link Attributes#ATTACK_SPEED} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to `attackSpeedBaseline`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material             Tool material of this item.
    /// @param attackDamageBaseline Attack-damage of this item before material value or other bonus calculations.
    /// @param attackSpeedBaseline  Attack-speed of this item before material value or other bonus calculations.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties hoe(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline) {
        super.hoe(material, attackDamageBaseline, attackSpeedBaseline);
        return this;
    }
    
    ///
    /// Applies tool properties to this item using the given parameters.
    /// Uses shovel-specific defaults.
    ///
    /// - Sets {@link DataComponents#TOOL} from:
    ///     - {@link List#of()} from:
    ///         - {@link Tool.Rule#deniesDrops(HolderSet)} to {@link ToolMaterial#incorrectBlocksForDrops()}
    ///         - {@link Tool.Rule#minesAndDrops(HolderSet, float)} to {@link BlockTags#MINEABLE_WITH_SHOVEL} and {@link ToolMaterial#speed()}
    ///     - {@link Tool#defaultMiningSpeed()} to `1.0F`
    ///     - {@link Tool#damagePerBlock()} to `1`
    ///     - {@link Tool#canDestroyBlocksInCreative()} to `true`
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ToolMaterial#durability()}
    /// - Sets {@link DataComponents#DAMAGE} to `0`
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    /// - Sets {@link DataComponents#ENCHANTABLE} to {@link ToolMaterial#enchantmentValue()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ToolMaterial#repairItems()}
    /// - Sets {@link DataComponents#WEAPON} from:
    ///     - {@link Weapon#itemDamagePerAttack()} to `2`
    ///     - {@link Weapon#disableBlockingForSeconds()} to `0.0F`
    ///
    /// - Adds {@link Attributes#ATTACK_DAMAGE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to {@link ToolMaterial#attackDamageBonus()} `+` `attackDamageBaseline`
    /// - Adds {@link Attributes#ATTACK_SPEED} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to `attackSpeedBaseline`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material             Tool material of this item.
    /// @param attackDamageBaseline Attack-damage of this item before material value or other bonus calculations.
    /// @param attackSpeedBaseline  Attack-speed of this item before material value or other bonus calculations.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties shovel(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline) {
        super.shovel(material, attackDamageBaseline, attackSpeedBaseline);
        return this;
    }
    
    ///
    /// Applies tool properties to this item using the given parameters.
    /// Uses sword-specific defaults.
    ///
    /// - Sets {@link DataComponents#TOOL} from:
    ///     - {@link List#of()} from:
    ///         - {@link Tool.Rule#minesAndDrops(HolderSet, float)} to {@link Blocks#COBWEB} and `15.0F`
    ///         - {@link Tool.Rule#overrideSpeed(HolderSet, float)} to {@link BlockTags#SWORD_INSTANTLY_MINES} and {@link Float#MAX_VALUE}
    ///         - {@link Tool.Rule#overrideSpeed(HolderSet, float)} to {@link BlockTags#SWORD_EFFICIENT} and `1.5F`
    ///     - {@link Tool#defaultMiningSpeed()} to `1.0F`
    ///     - {@link Tool#damagePerBlock()} to `2`
    ///     - {@link Tool#canDestroyBlocksInCreative()} to `false`
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ToolMaterial#durability()}
    /// - Sets {@link DataComponents#DAMAGE} to `0`
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    /// - Sets {@link DataComponents#ENCHANTABLE} to {@link ToolMaterial#enchantmentValue()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ToolMaterial#repairItems()}
    /// - Sets {@link DataComponents#WEAPON} from:
    ///     - {@link Weapon#itemDamagePerAttack()} to `1`
    ///     - {@link Weapon#disableBlockingForSeconds()} to `0.0F`
    ///
    /// - Adds {@link Attributes#ATTACK_DAMAGE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to {@link ToolMaterial#attackDamageBonus()} `+` `attackDamageBaseline`
    /// - Adds {@link Attributes#ATTACK_SPEED} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to `attackSpeedBaseline`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material             Tool material of this item.
    /// @param attackDamageBaseline Attack-damage of this item before material value or other bonus calculations.
    /// @param attackSpeedBaseline  Attack-speed of this item before material value or other bonus calculations.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties sword(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline) {
        super.sword(material, attackDamageBaseline, attackSpeedBaseline);
        return this;
    }
    
    ///
    /// Applies spear-specific tool properties to this item using the given parameters.
    ///
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ToolMaterial#durability()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ToolMaterial#repairItems()}
    /// - Sets {@link DataComponents#ENCHANTABLE} to {@link ToolMaterial#enchantmentValue()}
    /// - Sets {@link DataComponents#DAMAGE_TYPE} to {@link DamageTypes#SPEAR}
    /// - Sets {@link DataComponents#KINETIC_WEAPON} from:
    ///     - {@link KineticWeapon#contactCooldownTicks()} to `10`
    ///     - {@link KineticWeapon#delayTicks()} to `delay * 20`
    ///     - {@link KineticWeapon#dismountConditions()} from {@link KineticWeapon.Condition#ofAttackerSpeed(int, float)} to `dismountTime * 20` and `dismountThreshold`
    ///     - {@link KineticWeapon#knockbackConditions()} from {@link KineticWeapon.Condition#ofAttackerSpeed(int, float)} to `knockbackTime * 20` and `knockbackThreshold`
    ///     - {@link KineticWeapon#damageConditions()} from {@link KineticWeapon.Condition#ofRelativeSpeed(int, float)} to `damageTime * 20` and `damageThreshold`
    ///     - {@link KineticWeapon#forwardMovement()} to `0.38F`
    ///     - {@link KineticWeapon#damageMultiplier()} to `damageMultiplier`
    ///     - {@link KineticWeapon#sound()} to:
    ///         - {@link SoundEvents#SPEAR_WOOD_USE} if {@link ToolMaterial#WOOD} otherwise {@link SoundEvents#SPEAR_USE}
    ///     - {@link KineticWeapon#hitSound()} to:
    ///         - {@link SoundEvents#SPEAR_WOOD_HIT} if {@link ToolMaterial#WOOD} otherwise {@link SoundEvents#SPEAR_HIT}
    /// - Sets {@link DataComponents#PIERCING_WEAPON} from:
    ///     - {@link PiercingWeapon#dealsKnockback()} to `true`
    ///     - {@link PiercingWeapon#dismounts()} to `false`
    ///     - {@link PiercingWeapon#sound()} to:
    ///         - {@link SoundEvents#SPEAR_WOOD_ATTACK} if {@link ToolMaterial#WOOD} otherwise {@link SoundEvents#SPEAR_ATTACK}
    ///     - {@link PiercingWeapon#hitSound()} to:
    ///         - {@link SoundEvents#SPEAR_WOOD_HIT} if {@link ToolMaterial#WOOD} otherwise {@link SoundEvents#SPEAR_HIT}
    /// - Sets {@link DataComponents#ATTACK_RANGE} from:
    ///     - {@link AttackRange#minReach()} to `2.0F`
    ///     - {@link AttackRange#maxReach()} to `4.5F`
    ///     - {@link AttackRange#minCreativeReach()} to `2.0F`
    ///     - {@link AttackRange#maxCreativeReach()} to `6.5F`
    ///     - {@link AttackRange#hitboxMargin()} to `0.125F`
    ///     - {@link AttackRange#mobFactor()} to `0.5F`
    /// - Sets {@link DataComponents#MINIMUM_ATTACK_CHARGE} to `1.0F`
    /// - Sets {@link DataComponents#SWING_ANIMATION} from:
    ///     - {@link SwingAnimation#type()} to {@link SwingAnimationType#STAB}
    ///     - {@link SwingAnimation#duration()} to `attackDuration * 20`
    /// - Sets {@link DataComponents#USE_EFFECTS} from:
    ///     - {@link UseEffects#canSprint()} to `true`
    ///     - {@link UseEffects#interactVibrations()} to `false`
    ///     - {@link UseEffects#speedMultiplier()} to `1.0F`
    /// - Sets {@link DataComponents#WEAPON} from:
    ///     - {@link Weapon#itemDamagePerAttack()} to `1`
    ///     - {@link Weapon#disableBlockingForSeconds()} to `0.0F`
    ///
    /// - Adds {@link Attributes#ATTACK_DAMAGE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to {@link ToolMaterial#attackDamageBonus()}
    /// - Adds {@link Attributes#ATTACK_SPEED} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#MAINHAND} to `1.0F / attackDuration - 4.0`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material           Tool material of this item.
    /// @param attackDuration     Duration of the spear attack animation in seconds.
    /// @param damageMultiplier   Multiplier applied to damage when kinetic damage conditions are met.
    /// @param delay              Delay before the spear can deal contact damage (seconds).
    /// @param dismountTime       Time window for dismount checks (seconds).
    /// @param dismountThreshold  Minimum attacker speed required to dismount a target.
    /// @param knockbackTime      Time window for knockback checks (seconds).
    /// @param knockbackThreshold Minimum attacker speed required to apply knockback.
    /// @param damageTime         Time window for relative-speed damage checks (seconds).
    /// @param damageThreshold    Minimum relative speed required for kinetic damage.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties spear(
            ToolMaterial material,
            float attackDuration,
            float damageMultiplier,
            float delay,
            float dismountTime,
            float dismountThreshold,
            float knockbackTime,
            float knockbackThreshold,
            float damageTime,
            float damageThreshold
    ) {
        super.spear(
                material,
                attackDuration,
                damageMultiplier,
                delay,
                dismountTime,
                dismountThreshold,
                knockbackTime,
                knockbackThreshold,
                damageTime,
                damageThreshold
        );
        return this;
    }
    
    ///
    /// Makes this item behave like a spawn egg for the given entity type.
    ///
    /// - Sets {@link DataComponents#ENTITY_DATA} to `type`
    ///
    /// Defaults to `nothing`
    ///
    /// @param type Entity type spawned by this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties spawnEgg(EntityType<?> type) {
        super.spawnEgg(type);
        return this;
    }
    
    
    ///
    /// Applies humanoid-armor properties to this item using the given parameters.
    ///
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ArmorType#getDurability(int)} from:
    ///     - {@link ArmorMaterial#durability()}
    /// - Sets {@link DataComponents#ENCHANTABLE} to {@link ArmorMaterial#enchantmentValue()}
    /// - Sets {@link DataComponents#EQUIPPABLE} from:
    ///     - {@link Equippable#slot()} to {@link ArmorType#getSlot()}
    ///     - {@link Equippable#equipSound()} to {@link ArmorMaterial#equipSound()}
    ///     - {@link Equippable#assetId()} to {@link ArmorMaterial#assetId()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ArmorMaterial#repairIngredient()}
    ///
    /// - Sets attribute modifiers from {@link ArmorMaterial#createAttributes(ArmorType)} from:
    ///     - {@link Attributes#ARMOR} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to defense from {@link ArmorMaterial#defense()} for `type`
    ///     - {@link Attributes#ARMOR_TOUGHNESS} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to {@link ArmorMaterial#toughness()}
    ///     - {@link Attributes#KNOCKBACK_RESISTANCE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to {@link ArmorMaterial#knockbackResistance()} when greater than `0.0F`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material Armor material of this item.
    /// @param type     Armor type of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties humanoidArmor(ArmorMaterial material, ArmorType type) {
        super.humanoidArmor(material, type);
        return this;
    }
    
    ///
    /// Applies wolf-armor properties to this item using the given parameters.
    ///
    /// - Sets {@link DataComponents#MAX_DAMAGE} to {@link ArmorType#getDurability(int)} from:
    ///     - {@link ArmorType#BODY}
    ///     - {@link ArmorMaterial#durability()}
    /// - Sets {@link DataComponents#REPAIRABLE} to {@link ArmorMaterial#repairIngredient()}
    /// - Sets {@link DataComponents#EQUIPPABLE} from:
    ///     - {@link Equippable#slot()} to {@link EquipmentSlot#BODY}
    ///     - {@link Equippable#equipSound()} to {@link ArmorMaterial#equipSound()}
    ///     - {@link Equippable#assetId()} to {@link ArmorMaterial#assetId()}
    ///     - {@link Equippable#allowedEntities()} to {@link HolderSet#direct(Holder[])} from:
    ///         - {@link EntityType#WOLF}
    ///     - {@link Equippable#canBeSheared()} to `true`
    ///     - {@link Equippable#shearingSound()} to {@link SoundEvents#ARMOR_UNEQUIP_WOLF}
    /// - Sets {@link DataComponents#BREAK_SOUND} to {@link SoundEvents#WOLF_ARMOR_BREAK}
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    ///
    /// - Sets attribute modifiers from {@link ArmorMaterial#createAttributes(ArmorType)} with:
    ///     - {@link Attributes#ARMOR} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to defense from {@link ArmorMaterial#defense()} for {@link ArmorType#BODY}
    ///     - {@link Attributes#ARMOR_TOUGHNESS} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to {@link ArmorMaterial#toughness()}
    ///     - {@link Attributes#KNOCKBACK_RESISTANCE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to {@link ArmorMaterial#knockbackResistance()} when greater than `0.0F`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material Armor material of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties wolfArmor(ArmorMaterial material) {
        super.wolfArmor(material);
        return this;
    }
    
    ///
    /// Applies horse-armor properties to this item using the given parameters.
    ///
    /// - Sets {@link DataComponents#EQUIPPABLE} from:
    ///     - {@link Equippable#slot()} to {@link EquipmentSlot#BODY}
    ///     - {@link Equippable#equipSound()} to {@link SoundEvents#HORSE_ARMOR}
    ///     - {@link Equippable#assetId()} to {@link ArmorMaterial#assetId()}
    ///     - {@link Equippable#allowedEntities()} to entities from {@link EntityTypeTags#CAN_WEAR_HORSE_ARMOR}
    ///     - {@link Equippable#damageOnHurt()} to `false`
    ///     - {@link Equippable#canBeSheared()} to `true`
    ///     - {@link Equippable#shearingSound()} to {@link SoundEvents#HORSE_ARMOR_UNEQUIP}
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    ///
    /// - Sets attribute modifiers from {@link ArmorMaterial#createAttributes(ArmorType)} with:
    ///     - {@link Attributes#ARMOR} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to defense from {@link ArmorMaterial#defense()} for {@link ArmorType#BODY}
    ///     - {@link Attributes#ARMOR_TOUGHNESS} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to {@link ArmorMaterial#toughness()}
    ///     - {@link Attributes#KNOCKBACK_RESISTANCE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to {@link ArmorMaterial#knockbackResistance()} when greater than `0.0F`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material Armor material of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties horseArmor(ArmorMaterial material) {
        super.horseArmor(material);
        return this;
    }
    
    ///
    /// Applies nautilus-armor properties to this item using the given parameters.
    ///
    /// - Sets {@link DataComponents#EQUIPPABLE} from:
    ///     - {@link Equippable#slot()} to {@link EquipmentSlot#BODY}
    ///     - {@link Equippable#equipSound()} to {@link SoundEvents#ARMOR_EQUIP_NAUTILUS}
    ///     - {@link Equippable#assetId()} to {@link ArmorMaterial#assetId()}
    ///     - {@link Equippable#allowedEntities()} to {@link EntityTypeTags#CAN_WEAR_NAUTILUS_ARMOR}
    ///     - {@link Equippable#damageOnHurt()} to `false`
    ///     - {@link Equippable#equipOnInteract()} to `true`
    ///     - {@link Equippable#canBeSheared()} to `true`
    ///     - {@link Equippable#shearingSound()} to {@link SoundEvents#ARMOR_UNEQUIP_NAUTILUS}
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `1`
    ///
    /// - Sets attribute modifiers from {@link ArmorMaterial#createAttributes(ArmorType)} with:
    ///     - {@link Attributes#ARMOR} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to defense from {@link ArmorMaterial#defense()} for {@link ArmorType#BODY}
    ///     - {@link Attributes#ARMOR_TOUGHNESS} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to {@link ArmorMaterial#toughness()}
    ///     - {@link Attributes#KNOCKBACK_RESISTANCE} with {@link AttributeModifier.Operation#ADD_VALUE} at {@link EquipmentSlotGroup#bySlot(EquipmentSlot)} to {@link ArmorMaterial#knockbackResistance()} when greater than `0.0F`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material Armor material of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties nautilusArmor(ArmorMaterial material) {
        super.nautilusArmor(material);
        return this;
    }
    
    ///
    /// Sets the trim material this item provides when used as trim in the smithing table
    ///
    /// - Sets {@link DataComponents#PROVIDES_TRIM_MATERIAL} to `material`
    ///
    /// Defaults to `nothing`
    ///
    /// @param material Trim material provided by this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties trimMaterial(ResourceKey<TrimMaterial> material) {
        super.trimMaterial(material);
        return this;
    }
    
    ///
    /// Sets the required {@link FeatureFlag}s for this item to show up in game.
    ///
    /// Defaults to {@link FeatureFlags#VANILLA_SET}
    ///
    /// @param requiredFeatures Required {@link FeatureFlag}s for this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties requiredFeatures(FeatureFlag... requiredFeatures) {
        super.requiredFeatures(requiredFeatures);
        return this;
    }
    
    ///
    /// Sets the {@link ResourceKey} of this item.
    ///
    /// - Sets `id` to `key`
    ///
    /// Defaults to `null`
    ///
    /// @param key Resource key of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties setId(ResourceKey<Item> key) {
        super.setId(key);
        return this;
    }
    
    ///
    /// Sets the {@link Identifier} to construct the {@link ResourceKey} of this item from.
    ///
    /// - Sets `id` to `ResourceKey.create(Registries.ITEM, identifier)`
    ///
    /// Defaults to `null`
    ///
    /// @param identifier Identifier to construct the {@link ResourceKey} from.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    public SLItemProperties setId(Identifier identifier) {
        super.setId(ResourceKey.create(Registries.ITEM, identifier));
        return this;
    }
    
    ///
    /// Overrides the generated description id of this item. Description means
    /// the display name of the item in-game.
    ///
    /// - Sets `descriptionId` to `DependantName.fixed(descriptionId)`
    ///
    /// Defaults to `Util.makeDescriptionId("item", id.identifier())`
    ///
    /// @param descriptionId Description id of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties overrideDescription(String descriptionId) {
        super.overrideDescription(descriptionId);
        return this;
    }
    
    ///
    /// Overrides the generated description id of this item to use the block id.
    /// Description means the display name of the item in-game.
    ///
    /// - Sets `descriptionId` to {@link Item.Properties#BLOCK_DESCRIPTION_ID}
    ///
    /// Defaults to {@link Item.Properties#ITEM_DESCRIPTION_ID}
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties useBlockDescriptionPrefix() {
        super.useBlockDescriptionPrefix();
        return this;
    }
    
    ///
    /// Overrides the generated description id of this item to use the item id.
    /// Description means the display name of the item in-game.
    ///
    /// - Sets `descriptionId` to {@link Item.Properties#ITEM_DESCRIPTION_ID}
    ///
    /// Defaults to {@link Item.Properties#ITEM_DESCRIPTION_ID}
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties useItemDescriptionPrefix() {
        super.useItemDescriptionPrefix();
        return this;
    }
    
    ///
    /// Adds a data component to this item.
    ///
    /// - Adds `component` with `value` to {@link DataComponents#COMMON_ITEM_COMPONENTS}
    ///
    /// @param type  Data component type to add.
    /// @param value Value of the component.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public <T> SLItemProperties component(DataComponentType<T> type, T value) {
        super.component(type, value);
        return this;
    }
    
    ///
    /// Adds a data component to this item.
    ///
    /// - Adds `component` with `value` to {@link DataComponents#COMMON_ITEM_COMPONENTS}
    ///
    /// Defaults to `DataComponents.COMMON_ITEM_COMPONENTS`
    ///
    /// @param type  Data component type supplier to add.
    /// @param value Value of the component.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    public <T> SLItemProperties component(Supplier<? extends DataComponentType<T>> type, T value) {
        super.component(type, value);
        return this;
    }
    
    ///
    /// Adds a delayed data component to this item which gets resolved once the {@link HolderLookup.Provider} is available.
    ///
    /// - Adds `component` with `value` to {@link DataComponents#COMMON_ITEM_COMPONENTS}
    ///
    /// Defaults to `DataComponents.COMMON_ITEM_COMPONENTS`
    ///
    /// @param type        Data component type supplier to add.
    /// @param initializer Initializer for the component.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public <T> SLItemProperties delayedComponent(DataComponentType<T> type, DataComponentInitializers.SingleComponentInitializer<T> initializer) {
        super.delayedComponent(type, initializer);
        return this;
    }
    
    ///
    /// Adds a delayed data component to this item which gets resolved once the {@link HolderLookup.Provider} is available.
    ///
    /// - Adds `component` with `value` to {@link DataComponents#COMMON_ITEM_COMPONENTS}
    ///
    /// Defaults to `DataComponents.COMMON_ITEM_COMPONENTS`
    ///
    /// @param type        Data component type supplier to add.
    /// @param initializer Initializer for the component.
    /// @param <T>         Data component value type.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    public <T> SLItemProperties delayedComponent(Supplier<? extends DataComponentType<T>> type, DataComponentInitializers.SingleComponentInitializer<T> initializer) {
        super.delayedComponent(type.get(), initializer);
        return this;
    }
    
    ///
    /// Adds a delayed data component to this item which gets resolved once the {@link HolderLookup.Provider} is available.
    ///
    /// - Adds `component` with `value` to {@link DataComponents#COMMON_ITEM_COMPONENTS}
    ///
    /// Defaults to `DataComponents.COMMON_ITEM_COMPONENTS`
    ///
    /// @param type  Data component type to add.
    /// @param value Data component value supplier.
    /// @param <T>   Holder value type.
    ///
    @Override
    public <T> SLItemProperties delayedHolderComponent(DataComponentType<Holder<T>> type, ResourceKey<T> value) {
        super.delayedHolderComponent(type, value);
        return this;
    }
    
    ///
    /// Adds item attribute modifiers to this item.
    ///
    /// - Sets {@link DataComponents#ATTRIBUTE_MODIFIERS} to `attributes`
    ///
    /// Defaults to `nothing`
    ///
    /// @param attributes Attribute modifiers of this item.
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    @Override
    public SLItemProperties attributes(ItemAttributeModifiers attributes) {
        super.attributes(attributes);
        return this;
    }
    
    ///
    /// Marks this item as a bucket-like item.
    ///
    /// - Sets {@link DataComponents#MAX_STACK_SIZE} to `maxStackSize`
    /// - Sets {@link Item#craftingRemainingItem} to {@link Items#BUCKET}
    ///
    /// @return This {@link SLItemProperties} instance.
    ///
    public SLItemProperties bucket() {
        return this.stacksTo(1).craftRemainder(Items.BUCKET);
    }
}
