package com.sugarplumstudios.sweettreats.network;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.discovery.RecipeBookEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server -> client: open the recipe book with every recipe it shows and the player's discovered recipes.
 */
public record OpenRecipeBookPacket(Set<ResourceLocation> discovered, boolean hideUndiscoveredIngredients, List<RecipeBookEntry> entries) {
    public static final ResourceLocation ID = SweetTreats.id("open_recipe_book");

    public static void encode(OpenRecipeBookPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.discovered.size());
        packet.discovered.forEach(buffer::writeResourceLocation);
        buffer.writeBoolean(packet.hideUndiscoveredIngredients);
        buffer.writeVarInt(packet.entries.size());
        packet.entries.forEach(entry -> entry.write(buffer));
    }

    public static OpenRecipeBookPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Set<ResourceLocation> discovered = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            discovered.add(buffer.readResourceLocation());
        }
        boolean hide = buffer.readBoolean();
        int count = buffer.readVarInt();
        List<RecipeBookEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(RecipeBookEntry.read(buffer));
        }
        return new OpenRecipeBookPacket(discovered, hide, entries);
    }
}
