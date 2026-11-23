package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.platform.RegistryEntry;
import com.sugarplumstudios.sweettreats.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    public static final RegistryEntry<CreativeModeTab> SWEET_TREATS = Services.PLATFORM.register(Registries.CREATIVE_MODE_TAB, "sweet_treats",
            () -> Services.PLATFORM.creativeTab()
                    .title(Component.translatable("itemGroup.sweettreats"))
                    .icon(() -> new ItemStack(ModItems.PINK_VANILLA_CUPCAKE.get()))
                    .displayItems((parameters, output) -> ModItems.ALL.forEach(entry -> output.accept(entry.get())))
                    .build());

    private ModCreativeTabs() {
    }

    /** Loads the class, which registers the creative tab. */
    public static void init() {
    }
}
