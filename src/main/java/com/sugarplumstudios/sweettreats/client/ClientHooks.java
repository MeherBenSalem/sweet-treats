package com.sugarplumstudios.sweettreats.client;

import com.sugarplumstudios.sweettreats.client.screen.SweetRecipeBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Client-only entry points called from packets (kept separate so dedicated servers never load client classes).
 */
public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openRecipeBook(Set<ResourceLocation> discovered, boolean hideUndiscoveredIngredients) {
        Minecraft.getInstance().setScreen(new SweetRecipeBookScreen(discovered, hideUndiscoveredIngredients));
    }
}
