package com.sugarplumstudios.sweettreats.menu;

import com.sugarplumstudios.sweettreats.blockentity.BakeryOvenBlockEntity;
import com.sugarplumstudios.sweettreats.registry.ModBlocks;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BakeryOvenMenu extends SweetMenuBase {
    public static final int[][] INPUT_POSITIONS = {{44, 17}, {62, 17}, {44, 35}, {62, 35}};
    public static final int FUEL_X = 12;
    public static final int FUEL_Y = 53;
    public static final int OUTPUT_X = 124;
    public static final int OUTPUT_Y = 35;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final Level level;

    /** Client constructor. */
    public BakeryOvenMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(BakeryOvenBlockEntity.SLOT_COUNT), new SimpleContainerData(4),
                ContainerLevelAccess.NULL, null);
    }

    public BakeryOvenMenu(int id, Inventory inventory, Container items, ContainerData data, ContainerLevelAccess access,
                          @Nullable Consumer<Player> onTakeOutput) {
        super(ModMenuTypes.BAKERY_OVEN.get(), id);
        checkContainerDataCount(data, 4);
        this.data = data;
        this.access = access;
        this.level = inventory.player.level();

        for (int i = 0; i < BakeryOvenBlockEntity.INPUT_COUNT; i++) {
            addSlot(new MachineSlot(items, i, INPUT_POSITIONS[i][0], INPUT_POSITIONS[i][1]));
        }
        addSlot(new MachineSlot(items, BakeryOvenBlockEntity.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BakeryOvenBlockEntity.isFuel(level, stack);
            }
        });
        addSlot(new TreatResultSlot(inventory.player, items, BakeryOvenBlockEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y, onTakeOutput));
        addPlayerInventory(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    protected int outputSlotIndex() {
        return BakeryOvenBlockEntity.OUTPUT_SLOT;
    }

    @Override
    protected boolean moveIntoMachine(ItemStack stack) {
        if (BakeryOvenBlockEntity.isFuel(level, stack) && moveItemStackTo(stack, BakeryOvenBlockEntity.FUEL_SLOT, BakeryOvenBlockEntity.FUEL_SLOT + 1, false)) {
            return true;
        }
        return moveItemStackTo(stack, 0, BakeryOvenBlockEntity.INPUT_COUNT, false);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.BAKERY_OVEN.get());
    }

    public boolean isLit() {
        return data.get(BakeryOvenBlockEntity.DATA_LIT_TIME) > 0;
    }

    /** Remaining fuel, scaled to {@code pixels}. */
    public int getLitProgress(int pixels) {
        int duration = data.get(BakeryOvenBlockEntity.DATA_LIT_DURATION);
        if (duration == 0) {
            duration = 200;
        }
        return data.get(BakeryOvenBlockEntity.DATA_LIT_TIME) * pixels / duration;
    }

    /** Baking progress, scaled to {@code pixels}. */
    public int getBakeProgress(int pixels) {
        int progress = data.get(BakeryOvenBlockEntity.DATA_PROGRESS);
        int total = data.get(BakeryOvenBlockEntity.DATA_TOTAL);
        return total != 0 && progress != 0 ? progress * pixels / total : 0;
    }
}
