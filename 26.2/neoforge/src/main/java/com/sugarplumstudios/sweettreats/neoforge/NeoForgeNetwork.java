package com.sugarplumstudios.sweettreats.neoforge;

import com.sugarplumstudios.sweettreats.client.ClientHooks;
import com.sugarplumstudios.sweettreats.network.OpenRecipeBookPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

final class NeoForgeNetwork {
    private static final String PROTOCOL = "1";

    private NeoForgeNetwork() {
    }

    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        // Handlers run on the main thread. The client class is only touched when the packet arrives on a client.
        registrar.playToClient(OpenRecipeBookPacket.TYPE, OpenRecipeBookPacket.STREAM_CODEC,
                (packet, context) -> ClientHooks.openRecipeBook(packet));
    }
}
