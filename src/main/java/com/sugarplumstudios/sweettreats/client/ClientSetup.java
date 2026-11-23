package com.sugarplumstudios.sweettreats.client;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.client.render.DisplayBlockRenderer;
import com.sugarplumstudios.sweettreats.client.render.MixingBowlRenderer;
import com.sugarplumstudios.sweettreats.client.screen.BakeryOvenScreen;
import com.sugarplumstudios.sweettreats.client.screen.DecorationStationScreen;
import com.sugarplumstudios.sweettreats.client.screen.MixingBowlScreen;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SweetTreats.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.BAKERY_OVEN.get(), BakeryOvenScreen::new);
            MenuScreens.register(ModMenuTypes.MIXING_BOWL.get(), MixingBowlScreen::new);
            MenuScreens.register(ModMenuTypes.DECORATION_STATION.get(), DecorationStationScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DISPLAY.get(), DisplayBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIXING_BOWL.get(), MixingBowlRenderer::new);
    }
}
