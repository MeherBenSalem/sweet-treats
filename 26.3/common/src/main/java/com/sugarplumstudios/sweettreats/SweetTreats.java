package com.sugarplumstudios.sweettreats;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import com.sugarplumstudios.sweettreats.registry.ModBlocks;
import com.sugarplumstudios.sweettreats.registry.ModCreativeTabs;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import com.sugarplumstudios.sweettreats.registry.ModMenuTypes;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Sweet Treats — a cute bakery mod by Sugarplum Studios. Loader-independent entry point: each loader calls
 * {@link #init()} from its own mod constructor or initializer.
 */
public final class SweetTreats {
    public static final String MOD_ID = "sweettreats";
    public static final Logger LOGGER = LogUtils.getLogger();

    private SweetTreats() {
    }

    /** Reads the common config and registers every block, item, block entity, menu, recipe type and creative tab. */
    public static void init() {
        SweetTreatsConfig.loadCommon();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModMenuTypes.init();
        ModRecipes.init();
        ModCreativeTabs.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
