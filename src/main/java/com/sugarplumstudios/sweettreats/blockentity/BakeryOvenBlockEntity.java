package com.sugarplumstudios.sweettreats.blockentity;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.block.BakeryOvenBlock;
import com.sugarplumstudios.sweettreats.menu.BakeryOvenMenu;
import com.sugarplumstudios.sweettreats.recipe.BakingRecipe;
import com.sugarplumstudios.sweettreats.recipe.RecipeUtil;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Bakery Oven: 4 ingredient slots, a fuel slot and an output slot. Burns furnace fuel to bake {@link BakingRecipe}s.
 * Hoppers: top inserts ingredients, sides insert fuel, bottom extracts baked goods.
 */
public class BakeryOvenBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_COUNT = 4;
    public static final int FUEL_SLOT = 4;
    public static final int OUTPUT_SLOT = 5;
    public static final int SLOT_COUNT = 6;
    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_TOTAL = 3;
    private static final int NO_FUEL_BURN = 40;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot < FUEL_SLOT) {
                recipeDirty = true;
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == OUTPUT_SLOT) {
                return false;
            }
            if (slot == FUEL_SLOT) {
                return isFuel(stack);
            }
            return true;
        }
    };
    private final LazyOptional<IItemHandler> allItems = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> inputItems = LazyOptional.of(() -> new RangedWrapper(items, 0, INPUT_COUNT));
    private final LazyOptional<IItemHandler> fuelItems = LazyOptional.of(() -> new RangedWrapper(items, FUEL_SLOT, FUEL_SLOT + 1));
    private final LazyOptional<IItemHandler> outputItems = LazyOptional.of(() -> new RangedWrapper(items, OUTPUT_SLOT, OUTPUT_SLOT + 1));

    private int litTime;
    private int litDuration;
    private int progress;
    private int totalTime;
    private float storedExperience;
    private boolean recipeDirty = true;
    private int recipeRecheck;
    @Nullable
    private BakingRecipe cachedRecipe;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_LIT_TIME -> litTime;
                case DATA_LIT_DURATION -> litDuration;
                case DATA_PROGRESS -> progress;
                case DATA_TOTAL -> totalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_LIT_TIME -> litTime = value;
                case DATA_LIT_DURATION -> litDuration = value;
                case DATA_PROGRESS -> progress = value;
                case DATA_TOTAL -> totalTime = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public BakeryOvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BAKERY_OVEN.get(), pos, state);
    }

    public static boolean isFuel(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isLit() {
        return litTime > 0;
    }

    public int getProgress() {
        return progress;
    }

    // ------------------------------------------------------------------ ticking

    public static void serverTick(Level level, BlockPos pos, BlockState state, BakeryOvenBlockEntity oven) {
        boolean wasLit = oven.isLit();
        boolean changed = false;
        if (oven.isLit()) {
            oven.litTime--;
        }

        BakingRecipe recipe = oven.currentRecipe(level);
        boolean canBake = recipe != null && oven.canOutput(recipe.getResult());
        if (canBake) {
            if (!oven.isLit()) {
                changed |= oven.ignite();
            }
            if (oven.isLit()) {
                oven.totalTime = recipe.getScaledCookingTime();
                oven.progress++;
                if (oven.progress >= oven.totalTime) {
                    oven.progress = 0;
                    oven.bake(level, pos, recipe);
                }
                changed = true;
            } else if (oven.progress > 0) {
                oven.progress = Mth.clamp(oven.progress - 2, 0, oven.totalTime);
                changed = true;
            }
        } else if (oven.progress > 0) {
            oven.progress = 0;
            changed = true;
        }

        if (wasLit != oven.isLit()) {
            state = state.setValue(BakeryOvenBlock.LIT, oven.isLit());
            level.setBlock(pos, state, 3);
            changed = true;
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    /** Starts burning a piece of fuel (or runs for free if fuel is disabled in the config). */
    private boolean ignite() {
        if (!SweetTreatsConfig.ovenRequiresFuel()) {
            litTime = litDuration = NO_FUEL_BURN;
            return true;
        }
        ItemStack fuel = items.getStackInSlot(FUEL_SLOT);
        int burnTime = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
        if (burnTime <= 0) {
            return false;
        }
        litTime = litDuration = burnTime;
        if (fuel.hasCraftingRemainingItem() && fuel.getCount() == 1) {
            items.setStackInSlot(FUEL_SLOT, fuel.getCraftingRemainingItem());
        } else {
            ItemStack left = fuel.copy();
            left.shrink(1);
            items.setStackInSlot(FUEL_SLOT, left);
        }
        return true;
    }

    @Nullable
    private BakingRecipe currentRecipe(Level level) {
        if (recipeDirty || --recipeRecheck <= 0) {
            recipeDirty = false;
            recipeRecheck = 20;
            cachedRecipe = RecipeUtil.findBest(level, ModRecipes.BAKING.get(), inputContainer());
        }
        return cachedRecipe;
    }

    private SimpleContainer inputContainer() {
        SimpleContainer container = new SimpleContainer(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) {
            container.setItem(i, items.getStackInSlot(i).copy());
        }
        return container;
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void bake(Level level, BlockPos pos, BakingRecipe recipe) {
        SimpleContainer inputs = inputContainer();
        int[] used = RecipeUtil.allocate(recipe.getIngredients(), BakingRecipe.contents(inputs, INPUT_COUNT));
        if (used == null) {
            recipeDirty = true;
            return;
        }
        ItemStack result = recipe.assemble(inputs, level.registryAccess());
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            ItemStack grown = output.copy();
            grown.grow(result.getCount());
            items.setStackInSlot(OUTPUT_SLOT, grown);
        }
        RecipeUtil.consume(items, 0, used, level, pos);
        storedExperience += recipe.getExperience();
        recipeDirty = true;

        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 0.5F, 1.8F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART, pos.getX() + 0.5D, pos.getY() + 1.15D, pos.getZ() + 0.5D,
                    2, 0.25D, 0.05D, 0.25D, 0.0D);
        }
    }

    /** Hands out the experience earned from baking, like a furnace. */
    public void awardExperience(Player player) {
        if (level instanceof ServerLevel serverLevel && storedExperience > 0) {
            int whole = Mth.floor(storedExperience);
            float fraction = Mth.frac(storedExperience);
            if (fraction != 0.0F && Math.random() < fraction) {
                whole++;
            }
            storedExperience = 0;
            ExperienceOrb.award(serverLevel, player.position(), whole);
            setChanged();
        }
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        SimpleContainer all = new SimpleContainer(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            all.setItem(i, items.getStackInSlot(i));
        }
        net.minecraft.world.Containers.dropContents(level, worldPosition, all);
    }

    // ------------------------------------------------------------------ menu

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.sweettreats.bakery_oven");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BakeryOvenMenu(id, inventory, items, data, net.minecraft.world.inventory.ContainerLevelAccess.create(level, worldPosition), this::awardExperience);
    }

    // ------------------------------------------------------------------ saving

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
        progress = tag.getInt("Progress");
        totalTime = tag.getInt("TotalTime");
        storedExperience = tag.getFloat("StoredXp");
        recipeDirty = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
        tag.putInt("Progress", progress);
        tag.putInt("TotalTime", totalTime);
        tag.putFloat("StoredXp", storedExperience);
    }

    // ------------------------------------------------------------------ capabilities

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return allItems.cast();
            }
            return switch (side) {
                case UP -> inputItems.cast();
                case DOWN -> outputItems.cast();
                default -> fuelItems.cast();
            };
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        allItems.invalidate();
        inputItems.invalidate();
        fuelItems.invalidate();
        outputItems.invalidate();
    }
}
