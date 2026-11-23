package com.sugarplumstudios.sweettreats.fabric;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.network.OpenRecipeBookPacket;
import com.sugarplumstudios.sweettreats.platform.IPlatform;
import com.sugarplumstudios.sweettreats.platform.RegistryEntry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class FabricPlatform implements IPlatform {
    /** Sweet Treats' per-player data (discovered recipes, whether the book was handed out), saved with the player. */
    private static final AttachmentType<CompoundTag> PLAYER_DATA =
            AttachmentRegistry.createPersistent(SweetTreats.id("player_data"), CompoundTag.CODEC);

    /** Loads the class, which registers the player data attachment. */
    static void registerPlayerData() {
    }

    @Override
    public String name() {
        return "Fabric";
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> RegistryEntry<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        Registry<R> target = (Registry<R>) BuiltInRegistries.REGISTRY.get(registry.location());
        if (target == null) {
            throw new IllegalArgumentException("Unknown registry " + registry);
        }
        T value = Registry.register(target, SweetTreats.id(name), factory.get());
        return new RegistryEntry<>(SweetTreats.id(name), () -> value);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        return FabricItemGroup.builder();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> menuType(MenuFactory<T> factory) {
        return new MenuType<>(factory::create, FeatureFlags.VANILLA_SET);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, OpenRecipeBookPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public int burnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Integer time = FuelRegistry.INSTANCE.get(stack.getItem());
        return time == null ? 0 : time;
    }

    @Override
    public ItemStack craftingRemainder(ItemStack stack) {
        Item remainder = stack.getItem().getCraftingRemainingItem();
        return remainder == null ? ItemStack.EMPTY : new ItemStack(remainder);
    }

    @Override
    public boolean cropGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean grow) {
        // Fabric API has no crop growth events.
        return grow;
    }

    @Override
    public void cropGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
    }

    @Override
    public CompoundTag playerData(Player player) {
        return player.getAttachedOrCreate(PLAYER_DATA, CompoundTag::new);
    }

    @Override
    public void playerDataChanged(Player player) {
        player.setAttached(PLAYER_DATA, playerData(player));
    }
}
