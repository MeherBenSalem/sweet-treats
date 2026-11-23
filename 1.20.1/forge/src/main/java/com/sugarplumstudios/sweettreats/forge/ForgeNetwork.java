package com.sugarplumstudios.sweettreats.forge;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.client.ClientHooks;
import com.sugarplumstudios.sweettreats.network.OpenRecipeBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** The mod's Forge channel (sweettreats:main). */
final class ForgeNetwork {
    private static final String PROTOCOL = "2";
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            SweetTreats.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;

    private ForgeNetwork() {
    }

    static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(OpenRecipeBookPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenRecipeBookPacket::encode)
                .decoder(OpenRecipeBookPacket::decode)
                .consumerMainThread((packet, context) -> {
                    context.get().setPacketHandled(true);
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openRecipeBook(packet));
                })
                .add();
    }

    static void sendTo(ServerPlayer player, OpenRecipeBookPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
