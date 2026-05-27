package dev.satherov.sathlib.testmod.common.menu;

import dev.satherov.sathlib.common.menu.SLMenu;
import dev.satherov.sathlib.common.menu.logic.SLMenus;
import dev.satherov.sathlib.common.menu.logic.SLQuickMovePlan;
import dev.satherov.sathlib.common.menu.logic.SLSlotKeys;
import dev.satherov.sathlib.common.menu.slot.SLContainerSlot;
import dev.satherov.sathlib.testmod.common.TestMenus;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class UITestBenchMenu extends SLMenu {
    
    private final SimpleContainer machineInventory = new SimpleContainer(6);
    
    public UITestBenchMenu(int containerId, Inventory playerInventory) {
        super(TestMenus.UI_TEST_BENCH.get(), containerId, playerInventory);
        this.seedMachineInventory();
        this.defineMenu(SLMenus.root(builder -> builder
                .group(SLSlotKeys.MACHINE_INPUT, group -> group
                        .slot(this.machineInventory, 0)
                        .slot(this.machineInventory, 1)
                )
                .group(SLSlotKeys.MACHINE_OUTPUT, group -> group
                        .slot(() -> new SLContainerSlot(this.machineInventory, 2) {
                            @Override
                            public boolean mayPlace(ItemStack stack) {
                                return false;
                            }
                        })
                )
                .group(SLSlotKeys.MACHINE_STORAGE, group -> group
                        .slot(this.machineInventory, 3)
                        .slot(this.machineInventory, 4)
                        .slot(this.machineInventory, 5)
                )
                .playerInventory(playerInventory)
                .hotbar(playerInventory)
        ));
        this.setQuickMovePlan(SLQuickMovePlan.builder()
                .from(SLSlotKeys.MACHINE_INPUT)
                .to(SLSlotKeys.PLAYER_INVENTORY)
                .to(SLSlotKeys.PLAYER_HOTBAR)
                .end()
                .from(SLSlotKeys.MACHINE_OUTPUT)
                .to(SLSlotKeys.PLAYER_INVENTORY)
                .to(SLSlotKeys.PLAYER_HOTBAR)
                .end()
                .from(SLSlotKeys.MACHINE_STORAGE)
                .to(SLSlotKeys.PLAYER_INVENTORY)
                .to(SLSlotKeys.PLAYER_HOTBAR)
                .end()
                .from(SLSlotKeys.PLAYER_INVENTORY)
                .to(SLSlotKeys.MACHINE_INPUT)
                .to(SLSlotKeys.MACHINE_STORAGE)
                .to(SLSlotKeys.PLAYER_HOTBAR)
                .end()
                .from(SLSlotKeys.PLAYER_HOTBAR)
                .to(SLSlotKeys.MACHINE_INPUT)
                .to(SLSlotKeys.MACHINE_STORAGE)
                .to(SLSlotKeys.PLAYER_INVENTORY)
                .end()
                .build());
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    private void seedMachineInventory() {
        this.machineInventory.setItem(0, new ItemStack(Items.IRON_INGOT, 12));
        this.machineInventory.setItem(1, new ItemStack(Items.REDSTONE, 16));
        this.machineInventory.setItem(2, new ItemStack(Items.COPPER_INGOT, 5));
        this.machineInventory.setItem(3, new ItemStack(Items.BUCKET, 1));
        this.machineInventory.setItem(4, new ItemStack(Items.DIAMOND, 3));
        this.machineInventory.setItem(5, ItemStack.EMPTY);
    }
}
