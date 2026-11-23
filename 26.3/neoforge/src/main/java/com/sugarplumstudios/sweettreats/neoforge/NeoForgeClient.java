package com.sugarplumstudios.sweettreats.neoforge;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.client.render.DisplayBlockRenderer;
import com.sugarplumstudios.sweettreats.client.render.MixingBowlRenderer;
import com.sugarplumstudios.sweettreats.client.screen.BakeryOvenScreen;
import com.sugarplumstudios.sweettreats.client.screen.DecorationStationScreen;
import com.sugarplumstudios.sweettreats.client.screen.MixingBowlScreen;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client setup: the client config, the machine screens and the block entity renderers. */
final class NeoForgeClient {
    private NeoForgeClient() {
    }

    static void init(IEventBus modBus) {
        SweetTreatsConfig.loadClient();
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(ModMenuTypes.BAKERY_OVEN.get(), BakeryOvenScreen::new);
            event.register(ModMenuTypes.MIXING_BOWL.get(), MixingBowlScreen::new);
            event.register(ModMenuTypes.DECORATION_STATION.get(), DecorationStationScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(ModBlockEntities.DISPLAY.get(), DisplayBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.MIXING_BOWL.get(), MixingBowlRenderer::new);
        });
    }
}
