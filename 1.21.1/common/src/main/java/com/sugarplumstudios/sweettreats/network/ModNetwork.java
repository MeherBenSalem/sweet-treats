package com.sugarplumstudios.sweettreats.network;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.discovery.RecipeBookEntry;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.platform.Services;
import net.minecraft.server.level.ServerPlayer;

public final class ModNetwork {
    private ModNetwork() {
    }

    /** Sends the recipe book's recipes and the player's discoveries, and opens the recipe book screen on their client. */
    public static void openRecipeBook(ServerPlayer player) {
        Services.PLATFORM.sendToPlayer(player, new OpenRecipeBookPacket(RecipeDiscovery.getDiscovered(player),
                SweetTreatsConfig.hideUndiscoveredIngredients(),
                RecipeBookEntry.collect(player.server.getRecipeManager(), player.level().registryAccess())));
    }
}
