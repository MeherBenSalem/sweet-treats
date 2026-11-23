package com.sugarplumstudios.sweettreats.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The inventory shared by the Bakery Oven and the Mixing Bowl. Hoppers and pipes see the sides of the block:
 * the top takes ingredients, the bottom gives the result, the other sides take fuel or containers.
 * Saved under {@code Inventory} in the same layout as earlier versions (an item list plus its size).
 */
public abstract class MachineBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    protected final NonNullList<ItemStack> items;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    /** Called whenever a slot's stack is replaced or changed. */
    protected abstract void onContentsChanged(int slot);

    /** The slots reachable from one side of the block. */
    protected abstract int[] slotsFor(@Nullable Direction side);

    @Override
    public abstract boolean canPlaceItem(int slot, ItemStack stack);

    // ------------------------------------------------------------------ container

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            onContentsChanged(slot);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) {
            onContentsChanged(slot);
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        onContentsChanged(0);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return slotsFor(side);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (!canPlaceItem(slot, stack)) {
            return false;
        }
        for (int reachable : slotsFor(side)) {
            if (reachable == slot) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        SimpleContainer all = new SimpleContainer(items.size());
        for (int i = 0; i < items.size(); i++) {
            all.setItem(i, items.get(i));
        }
        Containers.dropContents(level, worldPosition, all);
    }

    // ------------------------------------------------------------------ saving

    protected void loadInventory(CompoundTag tag) {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag.getCompound("Inventory"), items);
    }

    protected void saveInventory(CompoundTag tag) {
        CompoundTag inventory = new CompoundTag();
        ContainerHelper.saveAllItems(inventory, items, true);
        inventory.putInt("Size", items.size());
        tag.put("Inventory", inventory);
    }
}
