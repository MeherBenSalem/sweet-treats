package com.sugarplumstudios.sweettreats.network;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            SweetTreats.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;

    private ModNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(OpenRecipeBookPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenRecipeBookPacket::encode)
                .decoder(OpenRecipeBookPacket::decode)
                .consumerMainThread(OpenRecipeBookPacket::handle)
                .add();
    }

    /** Sends the player's discovered recipes and opens the recipe book screen on their client. */
    public static void openRecipeBook(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenRecipeBookPacket(RecipeDiscovery.getDiscovered(player), SweetTreatsConfig.hideUndiscoveredIngredients()));
    }
}
