package com.sugarplumstudios.sweettreats.menu;

import net.minecraft.util.Prediction;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.platform.Services;
import com.sugarplumstudios.sweettreats.recipe.DecoratingRecipe;
import com.sugarplumstudios.sweettreats.recipe.RecipeUtil;
import com.sugarplumstudios.sweettreats.registry.ModBlocks;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Decoration Station menu: base slot, two topping slots and an instant result slot (like a smithing table).
 */
public class DecorationStationMenu extends SweetMenuBase {
    public static final int BASE_X = 26;
    public static final int BASE_Y = 35;
    public static final int TOPPING_X = 62;
    public static final int TOPPING_Y1 = 26;
    public static final int TOPPING_Y2 = 44;
    public static final int RESULT_X = 124;
    public static final int RESULT_Y = 35;
    public static final int RESULT_SLOT = 3;

    private final ContainerLevelAccess access;
    private final Player player;
    private final Level level;
    private final ResultContainer result = new ResultContainer();
    private final SimpleContainer inputs = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    };
    @Nullable
    private DecoratingRecipe currentRecipe;

    /** Client constructor. */
    public DecorationStationMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public DecorationStationMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.DECORATION_STATION.get(), id);
        this.access = access;
        this.player = inventory.player;
        this.level = inventory.player.level();

        addSlot(new Slot(inputs, 0, BASE_X, BASE_Y));
        addSlot(new Slot(inputs, 1, TOPPING_X, TOPPING_Y1));
        addSlot(new Slot(inputs, 2, TOPPING_X, TOPPING_Y2));
        addSlot(new Slot(result, 0, RESULT_X, RESULT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                // Recipes are only known to the server; the client trusts the result the server sent.
                return hasItem() && (currentRecipe != null || level.isClientSide());
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                decorate(player, stack);
                super.onTake(player, stack);
            }
        });
        addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == inputs) {
            updateResult();
        }
    }

    private void updateResult() {
        if (level.isClientSide()) {
            // The server works out the result and syncs the result slot.
            return;
        }
        currentRecipe = RecipeUtil.findBest(level, ModRecipes.DECORATING.get(), inputs);
        result.setItem(0, currentRecipe != null ? currentRecipe.getResult().copy() : ItemStack.EMPTY);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index != RESULT_SLOT) {
            return super.quickMoveStack(player, index);
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (!moveItemStackTo(stack, machineSlotCount, machineSlotCount + 36, true)) {
            return ItemStack.EMPTY;
        }
        slot.onQuickCraft(stack, original);
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        if (!stack.isEmpty()) {
            // Inventory was too full for the whole result: the rest is dropped rather than lost.
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
        return original;
    }

    /** True if something is in the inputs but no recipe matches (the screen shows a little cross). */
    public boolean hasInvalidInputs() {
        if (inputs.isEmpty()) {
            return false;
        }
        return level.isClientSide() ? result.getItem(0).isEmpty() : currentRecipe == null;
    }

    private void decorate(Player player, ItemStack crafted) {
        DecoratingRecipe recipe = currentRecipe;
        if (recipe == null) {
            return;
        }
        crafted.onCraftedBy(player, crafted.getCount());
        int[] toppingUse = recipe.toppingAllocation(inputs);
        consume(player, 0, 1);
        if (toppingUse != null) {
            consume(player, 1, toppingUse[0]);
            consume(player, 2, toppingUse[1]);
        }
        access.execute((lvl, pos) -> lvl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F,
                1.0F + lvl.getRandom().nextFloat() * 0.4F));
        if (player instanceof ServerPlayer serverPlayer) {
            RecipeDiscovery.discoverByResult(serverPlayer, crafted);
        }
    }

    private void consume(Player player, int slot, int amount) {
        if (amount <= 0) {
            return;
        }
        ItemStack stack = inputs.getItem(slot);
        ItemStack remainder = Services.PLATFORM.craftingRemainder(stack);
        inputs.removeItem(slot, amount);
        if (!remainder.isEmpty()) {
            remainder.setCount(Math.min(remainder.getMaxStackSize(), remainder.getCount() * amount));
            if (inputs.getItem(slot).isEmpty()) {
                inputs.setItem(slot, remainder);
            } else if (!player.getInventory().add(remainder)) {
                player.drop(remainder, false, Prediction.SERVER_ONLY);
            }
        }
    }

    @Override
    protected int outputSlotIndex() {
        return RESULT_SLOT;
    }

    @Override
    protected boolean moveIntoMachine(ItemStack stack) {
        // Try toppings first if a base is already in place, otherwise fill the base slot.
        if (!slots.get(0).hasItem() && moveItemStackTo(stack, 0, 1, false)) {
            return true;
        }
        return moveItemStackTo(stack, 1, 3, false) || moveItemStackTo(stack, 0, 1, false);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != result && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((lvl, pos) -> clearContainer(player, inputs));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.DECORATION_STATION.get());
    }
}
