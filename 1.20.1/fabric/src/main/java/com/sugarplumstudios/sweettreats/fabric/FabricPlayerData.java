package com.sugarplumstudios.sweettreats.fabric;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

/**
 * Sweet Treats' per-player data (discovered recipes, whether the book was handed out), saved with the world.
 * Forge keeps it in the player's persisted NBT; Fabric API 1.20.1 has no persistent player attachment.
 * Keyed by player UUID, so it survives death and dimension changes by itself.
 */
final class FabricPlayerData extends SavedData {
    private static final String NAME = "sweettreats_players";
    private final CompoundTag players;

    private FabricPlayerData(CompoundTag players) {
        this.players = players;
    }

    static FabricPlayerData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                tag -> new FabricPlayerData(tag.getCompound("Players")),
                () -> new FabricPlayerData(new CompoundTag()), NAME);
    }

    CompoundTag of(UUID player) {
        String key = player.toString();
        if (!players.contains(key)) {
            players.put(key, new CompoundTag());
        }
        return players.getCompound(key);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("Players", players);
        return tag;
    }
}
