package com.sugarplumstudios.sweettreats.forge;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.client.render.DisplayBlockRenderer;
import com.sugarplumstudios.sweettreats.client.render.MixingBowlRenderer;
import com.sugarplumstudios.sweettreats.client.screen.BakeryOvenScreen;
import com.sugarplumstudios.sweettreats.client.screen.DecorationStationScreen;
import com.sugarplumstudios.sweettreats.client.screen.MixingBowlScreen;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client setup: the client config, the machine screens and the block entity renderers. */
final class ForgeClient {
    private ForgeClient() {
    }

    static void init(IEventBus modBus) {
        SweetTreatsConfig.loadClient();
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.BAKERY_OVEN.get(), BakeryOvenScreen::new);
            MenuScreens.register(ModMenuTypes.MIXING_BOWL.get(), MixingBowlScreen::new);
            MenuScreens.register(ModMenuTypes.DECORATION_STATION.get(), DecorationStationScreen::new);
        }));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(ModBlockEntities.DISPLAY.get(), DisplayBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.MIXING_BOWL.get(), MixingBowlRenderer::new);
        });
    }
}
