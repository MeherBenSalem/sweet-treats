package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.SweetTreats;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SweetTreats.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SWEET_TREATS = TABS.register("sweet_treats", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sweettreats"))
            .icon(() -> new ItemStack(ModItems.PINK_VANILLA_CUPCAKE.get()))
            .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
            .build());

    private ModCreativeTabs() {
    }
}
