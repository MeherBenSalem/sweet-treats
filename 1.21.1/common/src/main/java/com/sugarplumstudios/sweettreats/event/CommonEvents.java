package com.sugarplumstudios.sweettreats.event;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.platform.Services;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

/** Game events, called by each loader's own event hooks. */
public final class CommonEvents {
    private CommonEvents() {
    }

    /** First join: hand out a recipe book (configurable). */
    public static void onLogin(ServerPlayer player) {
        if (!SweetTreatsConfig.giveRecipeBookOnFirstJoin()) {
            return;
        }
        CompoundTag data = RecipeDiscovery.persistedData(player);
        if (!data.getBoolean(RecipeDiscovery.RECEIVED_BOOK_KEY)) {
            data.putBoolean(RecipeDiscovery.RECEIVED_BOOK_KEY, true);
            Services.PLATFORM.playerDataChanged(player);
            ItemStack book = new ItemStack(ModItems.RECIPE_BOOK.get());
            if (!player.getInventory().add(book)) {
                player.drop(book, false);
            }
        }
    }

    /** Keep discoveries across death and End-portal respawns. */
    public static void onClone(Player original, Player clone) {
        CompoundTag oldData = RecipeDiscovery.persistedData(original);
        CompoundTag newData = RecipeDiscovery.persistedData(clone);
        for (String key : new String[]{RecipeDiscovery.DISCOVERED_KEY, RecipeDiscovery.RECEIVED_BOOK_KEY}) {
            if (oldData.contains(key) && !newData.contains(key)) {
                newData.put(key, oldData.get(key).copy());
            }
        }
        Services.PLATFORM.playerDataChanged(clone);
    }

    /** Where the loader has no crafted or pickup events: once a second, notice new treats in each inventory. */
    public static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            RecipeDiscovery.discoverFromInventory(player);
        }
    }

    /** The seeds and pits wandering traders sell (when enabled in the config). */
    public static List<VillagerTrades.ItemListing> wanderingTrades() {
        return List.of(
                (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 1), new ItemStack(ModItems.STRAWBERRY_SEEDS.get(), 3), 12, 1, 0.05F),
                (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 1), new ItemStack(ModItems.LAVENDER_SEEDS.get(), 3), 12, 1, 0.05F),
                (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 2), new ItemStack(ModItems.VANILLA_SEEDS.get(), 2), 8, 1, 0.05F),
                (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 2), new ItemStack(ModItems.PEACH_PIT.get(), 1), 6, 1, 0.05F),
                (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, 2), new ItemStack(ModItems.CHERRY_PIT.get(), 1), 6, 1, 0.05F));
    }
}
