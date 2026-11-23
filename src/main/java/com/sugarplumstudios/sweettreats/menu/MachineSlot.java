package com.sugarplumstudios.sweettreats.menu;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

/**
 * A slot backed by a machine's item handler that also notifies the handler when its stack is changed in place
 * (e.g. by shift-clicking), so the machine re-checks its recipe and saves.
 */
public class MachineSlot extends SlotItemHandler {
    public MachineSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (getItemHandler() instanceof IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(getSlotIndex(), getItem());
        }
    }
}
