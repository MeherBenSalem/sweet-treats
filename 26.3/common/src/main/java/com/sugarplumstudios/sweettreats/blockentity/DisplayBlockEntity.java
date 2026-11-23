package com.sugarplumstudios.sweettreats.blockentity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup;
import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.block.DisplayBlock;
import com.sugarplumstudios.sweettreats.block.DisplayType;
import com.sugarplumstudios.sweettreats.block.SliceableTreatBlock;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the treats shown on a {@link DisplayBlock}. Contents are synced to clients for rendering.
 */
public class DisplayBlockEntity extends BlockEntity {
    public static final TagKey<Item> DISPLAYABLE = TagKey.create(Registries.ITEM, SweetTreats.id("displayable"));
    private final NonNullList<ItemStack> items;

    public DisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISPLAY.get(), pos, state);
        this.items = NonNullList.withSize(typeOf(state).slots(), ItemStack.EMPTY);
    }

    private static DisplayType typeOf(BlockState state) {
        return state.getBlock() instanceof DisplayBlock display ? display.getDisplayType() : DisplayType.DESSERT_PLATE;
    }

    public DisplayType getDisplayType() {
        return typeOf(getBlockState());
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    /**
     * What a display may hold: food, placeable cakes/pies and items tagged {@code sweettreats:displayable}.
     * Glass jars also take any non-block ingredient (flour, sprinkles...). Other blocks are never captured,
     * so building next to a counter still places blocks normally.
     */
    public boolean accepts(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        boolean treatBlock = stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SliceableTreatBlock;
        if (stack.has(DataComponents.FOOD) || treatBlock || stack.is(DISPLAYABLE)) {
            return true;
        }
        return getDisplayType() == DisplayType.GLASS_JAR && !(stack.getItem() instanceof BlockItem);
    }

    /** Whether at least one item of this stack can be placed on the display. */
    public boolean canInsert(ItemStack stack) {
        if (!accepts(stack)) {
            return false;
        }
        int max = Math.min(getDisplayType().maxPerSlot(), stack.getMaxStackSize());
        for (ItemStack slot : items) {
            if (slot.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < max) {
                return true;
            }
        }
        return false;
    }

    /**
     * Places items from the given stack (one item, or as many as fit in a glass jar).
     * Does not modify the given stack.
     *
     * @return how many items were inserted
     */
    public int insert(ItemStack stack) {
        if (!accepts(stack)) {
            return 0;
        }
        int max = Math.min(getDisplayType().maxPerSlot(), stack.getMaxStackSize());
        for (int i = 0; i < items.size(); i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < max) {
                int moved = Math.min(stack.getCount(), max - slot.getCount());
                slot.grow(moved);
                markUpdated();
                return moved;
            }
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                int moved = Math.min(stack.getCount(), max);
                items.set(i, stack.copyWithCount(moved));
                markUpdated();
                return moved;
            }
        }
        return 0;
    }

    /**
     * Takes back the most recently filled slot: one item, or the whole stack when {@code all} is true.
     */
    public ItemStack takeLast(boolean all) {
        for (int i = items.size() - 1; i >= 0; i--) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty()) {
                ItemStack taken = all ? slot.copy() : slot.copyWithCount(1);
                slot.shrink(taken.getCount());
                if (slot.isEmpty()) {
                    items.set(i, ItemStack.EMPTY);
                }
                markUpdated();
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public int getComparatorSignal() {
        int filled = 0;
        float fullness = 0;
        int max = getDisplayType().maxPerSlot();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                filled++;
                fullness += stack.getCount() / (float) Math.min(max, stack.getMaxStackSize());
            }
        }
        if (filled == 0) {
            return 0;
        }
        return 1 + Math.round(fullness / items.size() * 14);
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items, true);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            Containers.dropContents(level, pos, items);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
