package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.menu.BakeryOvenMenu;
import com.sugarplumstudios.sweettreats.menu.DecorationStationMenu;
import com.sugarplumstudios.sweettreats.menu.MixingBowlMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SweetTreats.MOD_ID);

    public static final RegistryObject<MenuType<BakeryOvenMenu>> BAKERY_OVEN = MENUS.register("bakery_oven",
            () -> IForgeMenuType.create(BakeryOvenMenu::new));
    public static final RegistryObject<MenuType<MixingBowlMenu>> MIXING_BOWL = MENUS.register("mixing_bowl",
            () -> IForgeMenuType.create(MixingBowlMenu::new));
    public static final RegistryObject<MenuType<DecorationStationMenu>> DECORATION_STATION = MENUS.register("decoration_station",
            () -> new MenuType<>(DecorationStationMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenuTypes() {
    }
}
