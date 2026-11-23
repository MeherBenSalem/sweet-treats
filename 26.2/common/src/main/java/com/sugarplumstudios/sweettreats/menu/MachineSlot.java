package com.sugarplumstudios.sweettreats.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A slot backed by a machine's inventory that also notifies the machine when its stack is changed in place
 * (e.g. by shift-clicking), so the machine re-checks its recipe and saves.
 */
public class MachineSlot extends Slot {
    public MachineSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(getContainerSlot(), stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        container.setItem(getContainerSlot(), getItem());
    }
}
