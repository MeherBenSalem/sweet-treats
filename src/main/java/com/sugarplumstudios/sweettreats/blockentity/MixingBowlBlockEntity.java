package com.sugarplumstudios.sweettreats.blockentity;

import com.sugarplumstudios.sweettreats.menu.MixingBowlMenu;
import com.sugarplumstudios.sweettreats.recipe.BakingRecipe;
import com.sugarplumstudios.sweettreats.recipe.MixingRecipe;
import com.sugarplumstudios.sweettreats.recipe.RecipeUtil;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Mixing Bowl: 6 ingredient slots, a container slot (bottles for drinks) and an output slot.
 * Stirs {@link MixingRecipe}s without fuel. Contents are synced to clients so the bowl can show them swirling.
 * Hoppers: top inserts ingredients, sides insert containers, bottom extracts results.
 */
public class MixingBowlBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_COUNT = 6;
    public static final int CONTAINER_SLOT = 6;
    public static final int OUTPUT_SLOT = 7;
    public static final int SLOT_COUNT = 8;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            recipeDirty = true;
            setChanged();
            syncToClients();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot != OUTPUT_SLOT;
        }
    };
    private final LazyOptional<IItemHandler> allItems = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> inputItems = LazyOptional.of(() -> new RangedWrapper(items, 0, INPUT_COUNT));
    private final LazyOptional<IItemHandler> containerItems = LazyOptional.of(() -> new RangedWrapper(items, CONTAINER_SLOT, CONTAINER_SLOT + 1));
    private final LazyOptional<IItemHandler> outputItems = LazyOptional.of(() -> new RangedWrapper(items, OUTPUT_SLOT, OUTPUT_SLOT + 1));

    private int progress;
    private int totalTime;
    private boolean mixing;
    private boolean recipeDirty = true;
    private int recipeRecheck;
    @Nullable
    private MixingRecipe cachedRecipe;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : totalTime;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            } else {
                totalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public MixingBowlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXING_BOWL.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    /** True while ingredients are being stirred (synced to the client for the swirl animation). */
    public boolean isMixing() {
        return mixing;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixingBowlBlockEntity bowl) {
        MixingRecipe recipe = bowl.currentRecipe(level);
        boolean canMix = recipe != null && bowl.canOutput(recipe.getResult());
        if (canMix) {
            bowl.totalTime = recipe.getScaledMixingTime();
            bowl.progress++;
            if (bowl.progress % 20 == 1) {
                level.playSound(null, pos, SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 0.35F, 1.4F + level.random.nextFloat() * 0.3F);
            }
            if (bowl.progress >= bowl.totalTime) {
                bowl.progress = 0;
                bowl.mix(level, pos, recipe);
            }
            setChanged(level, pos, state);
        } else if (bowl.progress > 0) {
            bowl.progress = 0;
            setChanged(level, pos, state);
        }
        boolean nowMixing = canMix;
        if (nowMixing != bowl.mixing) {
            bowl.mixing = nowMixing;
            bowl.syncToClients();
        }
    }

    @Nullable
    private MixingRecipe currentRecipe(Level level) {
        if (recipeDirty || --recipeRecheck <= 0) {
            recipeDirty = false;
            recipeRecheck = 20;
            cachedRecipe = RecipeUtil.findBest(level, ModRecipes.MIXING.get(), recipeContainer());
        }
        return cachedRecipe;
    }

    private SimpleContainer recipeContainer() {
        SimpleContainer container = new SimpleContainer(INPUT_COUNT + 1);
        for (int i = 0; i <= CONTAINER_SLOT; i++) {
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

    private void mix(Level level, BlockPos pos, MixingRecipe recipe) {
        SimpleContainer inputs = recipeContainer();
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
        if (recipe.needsContainer()) {
            RecipeUtil.consume(items, CONTAINER_SLOT, new int[]{1}, level, pos);
        }
        recipeDirty = true;

        level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.8F, 1.2F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    4, 0.2D, 0.1D, 0.2D, 0.0D);
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
        Containers.dropContents(level, worldPosition, all);
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    // ------------------------------------------------------------------ menu

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.sweettreats.mixing_bowl");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MixingBowlMenu(id, inventory, items, data, ContainerLevelAccess.create(level, worldPosition));
    }

    // ------------------------------------------------------------------ saving & syncing

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        totalTime = tag.getInt("TotalTime");
        mixing = tag.getBoolean("Mixing");
        recipeDirty = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());
        tag.putInt("Progress", progress);
        tag.putInt("TotalTime", totalTime);
        tag.putBoolean("Mixing", mixing);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("Inventory", items.serializeNBT());
        tag.putBoolean("Mixing", mixing);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        items.deserializeNBT(tag.getCompound("Inventory"));
        mixing = tag.getBoolean("Mixing");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
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
                default -> containerItems.cast();
            };
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        allItems.invalidate();
        inputItems.invalidate();
        containerItems.invalidate();
        outputItems.invalidate();
    }
}
