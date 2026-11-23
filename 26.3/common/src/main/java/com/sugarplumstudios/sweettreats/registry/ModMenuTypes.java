package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.menu.BakeryOvenMenu;
import com.sugarplumstudios.sweettreats.menu.DecorationStationMenu;
import com.sugarplumstudios.sweettreats.menu.MixingBowlMenu;
import com.sugarplumstudios.sweettreats.platform.RegistryEntry;
import com.sugarplumstudios.sweettreats.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
    public static final RegistryEntry<MenuType<BakeryOvenMenu>> BAKERY_OVEN = Services.PLATFORM.register(Registries.MENU, "bakery_oven",
            () -> Services.PLATFORM.menuType(BakeryOvenMenu::new));
    public static final RegistryEntry<MenuType<MixingBowlMenu>> MIXING_BOWL = Services.PLATFORM.register(Registries.MENU, "mixing_bowl",
            () -> Services.PLATFORM.menuType(MixingBowlMenu::new));
    public static final RegistryEntry<MenuType<DecorationStationMenu>> DECORATION_STATION = Services.PLATFORM.register(Registries.MENU,
            "decoration_station", () -> Services.PLATFORM.menuType(DecorationStationMenu::new));

    private ModMenuTypes() {
    }

    /** Loads the class, which registers the menu types. */
    public static void init() {
    }
}
