package com.sugarplumstudios.sweettreats.neoforge;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.network.OpenRecipeBookPacket;
import com.sugarplumstudios.sweettreats.platform.IPlatform;
import com.sugarplumstudios.sweettreats.platform.RegistryEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class NeoForgePlatform implements IPlatform {
    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();
    /** The part of the player's NeoForge data that survives death (the same key as on earlier versions). */
    private static final String PERSISTED = "PlayerPersisted";

    /** Hands every deferred register to the mod event bus. Call after {@link SweetTreats#init()}. */
    public static void registerAll(IEventBus modBus) {
        REGISTERS.values().forEach(register -> register.register(modBus));
    }

    @Override
    public String name() {
        return "NeoForge";
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        DeferredRegister<R> register = (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry,
                key -> DeferredRegister.create(registry, SweetTreats.MOD_ID));
        DeferredHolder<R, T> holder = register.register(name, factory);
        return new RegistryEntry<>(holder.getId(), holder);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        return CreativeModeTab.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return new BlockEntityType<>(factory::apply, Set.of(blocks));
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.DEFAULT_FLAGS);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, OpenRecipeBookPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public int burnTime(Level level, ItemStack stack) {
        return stack.isEmpty() ? 0 : stack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
    }

    @Override
    public ItemStack craftingRemainder(ItemStack stack) {
        ItemStackTemplate remainder = stack.isEmpty() ? null : stack.getItem().getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }

    @Override
    public boolean cropGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean grow) {
        return CommonHooks.canCropGrow(level, pos, state, grow);
    }

    @Override
    public void cropGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
        CommonHooks.fireCropGrowPost(level, pos, state);
    }

    @Override
    public CompoundTag playerData(Player player) {
        CompoundTag root = player.getPersistentData();
        Optional<CompoundTag> persisted = root.getCompound(PERSISTED);
        if (persisted.isPresent()) {
            return persisted.get();
        }
        CompoundTag created = new CompoundTag();
        root.put(PERSISTED, created);
        return created;
    }

    @Override
    public void playerDataChanged(Player player) {
        // The persisted NBT is saved with the player.
    }
}
