package com.sugarplumstudios.sweettreats.menu;

import com.sugarplumstudios.sweettreats.blockentity.MixingBowlBlockEntity;
import com.sugarplumstudios.sweettreats.registry.ModBlocks;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MixingBowlMenu extends SweetMenuBase {
    public static final int[][] INPUT_POSITIONS = {{26, 17}, {44, 17}, {62, 17}, {26, 35}, {44, 35}, {62, 35}};
    public static final int CONTAINER_X = 92;
    public static final int CONTAINER_Y = 56;
    public static final int OUTPUT_X = 124;
    public static final int OUTPUT_Y = 35;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Client constructor. */
    public MixingBowlMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, new ItemStackHandler(MixingBowlBlockEntity.SLOT_COUNT), new SimpleContainerData(2),
                ContainerLevelAccess.create(inventory.player.level(), buffer.readBlockPos()));
    }

    public MixingBowlMenu(int id, Inventory inventory, IItemHandler items, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.MIXING_BOWL.get(), id);
        checkContainerDataCount(data, 2);
        this.data = data;
        this.access = access;

        for (int i = 0; i < MixingBowlBlockEntity.INPUT_COUNT; i++) {
            addSlot(new MachineSlot(items, i, INPUT_POSITIONS[i][0], INPUT_POSITIONS[i][1]));
        }
        addSlot(new MachineSlot(items, MixingBowlBlockEntity.CONTAINER_SLOT, CONTAINER_X, CONTAINER_Y));
        addSlot(new TreatResultSlot(inventory.player, items, MixingBowlBlockEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y, null));
        addPlayerInventory(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    protected int outputSlotIndex() {
        return MixingBowlBlockEntity.OUTPUT_SLOT;
    }

    @Override
    protected boolean moveIntoMachine(ItemStack stack) {
        if (stack.is(Items.GLASS_BOTTLE)
                && moveItemStackTo(stack, MixingBowlBlockEntity.CONTAINER_SLOT, MixingBowlBlockEntity.CONTAINER_SLOT + 1, false)) {
            return true;
        }
        return moveItemStackTo(stack, 0, MixingBowlBlockEntity.INPUT_COUNT, false);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MIXING_BOWL.get());
    }

    public int getMixProgress(int pixels) {
        int progress = data.get(0);
        int total = data.get(1);
        return total != 0 && progress != 0 ? progress * pixels / total : 0;
    }
}
