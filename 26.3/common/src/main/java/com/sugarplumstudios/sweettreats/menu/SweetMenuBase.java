package com.sugarplumstudios.sweettreats.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Shared helpers for Sweet Treats menus: player inventory layout and shift-click handling.
 * Subclasses add their machine slots first, then call {@link #addPlayerInventory}.
 */
public abstract class SweetMenuBase extends AbstractContainerMenu {
    protected int machineSlotCount;

    protected SweetMenuBase(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    protected void addPlayerInventory(Inventory inventory, int x, int y) {
        machineSlotCount = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, x + col * 18, y + 58));
        }
    }

    /** Index of the output slot (or -1 if none), used to give it shift-click priority. */
    protected abstract int outputSlotIndex();

    /** Try to move a stack from the player inventory into the machine; return true if anything moved. */
    protected abstract boolean moveIntoMachine(ItemStack stack);

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int invStart = machineSlotCount;
        int hotbarStart = invStart + 27;
        int end = hotbarStart + 9;

        if (index < machineSlotCount) {
            if (!moveItemStackTo(stack, invStart, end, true)) {
                return ItemStack.EMPTY;
            }
            if (index == outputSlotIndex()) {
                slot.onQuickCraft(stack, original);
            }
        } else {
            if (!moveIntoMachine(stack)) {
                if (index < hotbarStart) {
                    if (!moveItemStackTo(stack, hotbarStart, end, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, invStart, hotbarStart, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}
