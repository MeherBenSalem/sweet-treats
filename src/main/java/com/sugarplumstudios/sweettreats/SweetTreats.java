package com.sugarplumstudios.sweettreats;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.sweettreats.network.ModNetwork;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import com.sugarplumstudios.sweettreats.registry.ModBlocks;
import com.sugarplumstudios.sweettreats.registry.ModCreativeTabs;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import com.sugarplumstudios.sweettreats.registry.ModLootModifiers;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Sweet Treats — a cute bakery mod by Sugarplum Studios.
 */
@Mod(SweetTreats.MOD_ID)
@SuppressWarnings("removal") // The context getters and ResourceLocation constructor are correct for every Forge 47.x release.
public class SweetTreats {
    public static final String MOD_ID = "sweettreats";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SweetTreats() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENUS.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModLootModifiers.SERIALIZERS.register(modBus);

        modBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SweetTreatsConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SweetTreatsConfig.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ModNetwork.register();
        event.enqueueWork(SweetTreats::registerCompostables);
    }

    private static void registerCompostables() {
        ModItems.COMPOSTABLES.forEach((item, chance) -> ComposterBlock.COMPOSTABLES.put(item.get(), chance.floatValue()));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
