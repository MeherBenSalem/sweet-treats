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
import com.sugarplumstudios.sweettreats.registry.ModBlocks;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class SweetTreatsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SweetTreatsConfig.loadClient();
        // Handlers run on the client's main thread.
        ClientPlayNetworking.registerGlobalReceiver(OpenRecipeBookPacket.TYPE, (packet, context) -> ClientHooks.openRecipeBook(packet));
        MenuScreens.register(ModMenuTypes.BAKERY_OVEN.get(), BakeryOvenScreen::new);
        MenuScreens.register(ModMenuTypes.MIXING_BOWL.get(), MixingBowlScreen::new);
        MenuScreens.register(ModMenuTypes.DECORATION_STATION.get(), DecorationStationScreen::new);
        BlockEntityRenderers.register(ModBlockEntities.DISPLAY.get(), DisplayBlockRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MIXING_BOWL.get(), MixingBowlRenderer::new);

        // Forge reads each model's render_type; on Fabric the layer is set per block.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                ModBlocks.BAKERY_SIGN.get(), ModBlocks.CHERRY_TREE.get(), ModBlocks.CUPCAKE_STAND.get(),
                ModBlocks.DESSERT_PLATE.get(), ModBlocks.LAVENDER_CROP.get(), ModBlocks.PEACH_TREE.get(),
                ModBlocks.STRAWBERRY_CROP.get(), ModBlocks.VANILLA_CROP.get(), ModBlocks.WILD_LAVENDER.get(),
                ModBlocks.WILD_STRAWBERRIES.get(), ModBlocks.WILD_VANILLA.get());
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(),
                ModBlocks.CAKE_DISPLAY.get(), ModBlocks.GLASS_JAR.get());
    }
}
