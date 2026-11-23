package com.sugarplumstudios.sweettreats.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Server -> client: open the recipe book with the player's discovered recipes.
 */
public record OpenRecipeBookPacket(Set<ResourceLocation> discovered, boolean hideUndiscoveredIngredients) {

    public static void encode(OpenRecipeBookPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.discovered.size());
        packet.discovered.forEach(buffer::writeResourceLocation);
        buffer.writeBoolean(packet.hideUndiscoveredIngredients);
    }

    public static OpenRecipeBookPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Set<ResourceLocation> discovered = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            discovered.add(buffer.readResourceLocation());
        }
        return new OpenRecipeBookPacket(discovered, buffer.readBoolean());
    }

    public static void handle(OpenRecipeBookPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.sugarplumstudios.sweettreats.client.ClientHooks.openRecipeBook(packet.discovered(), packet.hideUndiscoveredIngredients()));
    }
}
