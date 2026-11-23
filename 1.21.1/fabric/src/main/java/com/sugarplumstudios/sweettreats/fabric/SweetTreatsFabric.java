package com.sugarplumstudios.sweettreats.fabric;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.event.CommonEvents;
import com.sugarplumstudios.sweettreats.network.OpenRecipeBookPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

public final class SweetTreatsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SweetTreats.init();
        SweetTreats.registerCompostables();
        FabricWorld.register();
        FabricPlatform.registerPlayerData();
        PayloadTypeRegistry.playS2C().register(OpenRecipeBookPacket.TYPE, OpenRecipeBookPacket.STREAM_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CommonEvents.onLogin(handler.player));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> CommonEvents.onClone(oldPlayer, newPlayer));
        // Fabric API has no crafted or pickup events: new treats are noticed in the inventory instead.
        ServerTickEvents.END_SERVER_TICK.register(CommonEvents::onServerTick);
        if (SweetTreatsConfig.wanderingTraderTrades()) {
            TradeOfferHelper.registerWanderingTraderOffers(1, offers -> offers.addAll(CommonEvents.wanderingTrades()));
        }
    }
}
