package com.sugarplumstudios.sweettreats.platform;

import com.sugarplumstudios.sweettreats.network.OpenRecipeBookPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** What Sweet Treats needs from the mod loader. One implementation per loader. */
public interface IPlatform {
    String name();

    Path configDir();

    /** Registers an object under sweettreats:name in the given registry. */
    <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);

    CreativeModeTab.Builder creativeTab();

    <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory);

    /** Server only: opens the recipe book on a player's client. */
    void sendToPlayer(ServerPlayer player, OpenRecipeBookPacket packet);

    /** Furnace burn time of a fuel item, in ticks (0 if it is no fuel). */
    int burnTime(ItemStack stack);

    /** What is left behind when the stack is used up in a recipe (a bucket, a bottle...), or an empty stack. */
    ItemStack craftingRemainder(ItemStack stack);

    /** Lets other mods allow or deny a crop growth step (Forge's crop growth events); returns {@code grow} otherwise. */
    boolean cropGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean grow);

    void cropGrowPost(ServerLevel level, BlockPos pos, BlockState state);

    /** Data kept with the player across deaths and relogs (Forge: the player's persisted NBT). */
    CompoundTag playerData(Player player);

    /** Call after changing {@link #playerData(Player)}. */
    void playerDataChanged(Player player);

    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory);
    }
}
