package com.sugarplumstudios.sweettreats.client;

import com.sugarplumstudios.sweettreats.client.screen.SweetRecipeBookScreen;
import com.sugarplumstudios.sweettreats.network.OpenRecipeBookPacket;
import net.minecraft.client.Minecraft;

/**
 * Client-only entry points called from packets (kept separate so dedicated servers never load client classes).
 */
public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openRecipeBook(OpenRecipeBookPacket packet) {
        Minecraft.getInstance().gui.setScreen(new SweetRecipeBookScreen(packet.discovered(), packet.hideUndiscoveredIngredients(), packet.entries()));
    }
}
