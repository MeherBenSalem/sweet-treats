package com.sugarplumstudios.sweettreats.fabric;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.client.ClientHooks;
import com.sugarplumstudios.sweettreats.client.render.DisplayBlockRenderer;
import com.sugarplumstudios.sweettreats.client.render.MixingBowlRenderer;
import com.sugarplumstudios.sweettreats.client.screen.BakeryOvenScreen;
import com.sugarplumstudios.sweettreats.client.screen.DecorationStationScreen;
import com.sugarplumstudios.sweettreats.client.screen.MixingBowlScreen;
import com.sugarplumstudios.sweettreats.network.OpenRecipeBookPacket;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

/**
 * Fabric client set-up. Since 26.1 vanilla picks each block quad's render layer from its texture, so no
 * BlockRenderLayerMap entries are needed.
 */
public final class SweetTreatsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SweetTreatsConfig.loadClient();
        // Handlers run on the client's main thread.
        ClientPlayNetworking.registerGlobalReceiver(OpenRecipeBookPacket.TYPE, (packet, context) -> ClientHooks.openRecipeBook(packet));
        MenuScreens.register(ModMenuTypes.BAKERY_OVEN.get(), BakeryOvenScreen::new);
        MenuScreens.register(ModMenuTypes.MIXING_BOWL.get(), MixingBowlScreen::new);
        MenuScreens.register(ModMenuTypes.DECORATION_STATION.get(), DecorationStationScreen::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.DISPLAY.get(), DisplayBlockRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.MIXING_BOWL.get(), MixingBowlRenderer::new);

    }
}
