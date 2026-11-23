package com.sugarplumstudios.sweettreats.event;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.platform.Services;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Game events, called by each loader's own event hooks. Wandering trader trades are not offered on Minecraft 26.x. */
public final class CommonEvents {
    private CommonEvents() {
    }

    /** First join: hand out a recipe book (configurable). */
    public static void onLogin(ServerPlayer player) {
        if (!SweetTreatsConfig.giveRecipeBookOnFirstJoin()) {
            return;
        }
        CompoundTag data = RecipeDiscovery.persistedData(player);
        if (!data.getBooleanOr(RecipeDiscovery.RECEIVED_BOOK_KEY, false)) {
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

}
