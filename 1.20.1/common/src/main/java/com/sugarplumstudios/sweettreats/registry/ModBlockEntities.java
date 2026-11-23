package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.blockentity.BakeryOvenBlockEntity;
import com.sugarplumstudios.sweettreats.blockentity.DisplayBlockEntity;
import com.sugarplumstudios.sweettreats.blockentity.MixingBowlBlockEntity;
import com.sugarplumstudios.sweettreats.platform.RegistryEntry;
import com.sugarplumstudios.sweettreats.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final RegistryEntry<BlockEntityType<BakeryOvenBlockEntity>> BAKERY_OVEN = Services.PLATFORM.register(
            Registries.BLOCK_ENTITY_TYPE, "bakery_oven",
            () -> Services.PLATFORM.blockEntityType(BakeryOvenBlockEntity::new, ModBlocks.BAKERY_OVEN.get()));

    public static final RegistryEntry<BlockEntityType<MixingBowlBlockEntity>> MIXING_BOWL = Services.PLATFORM.register(
            Registries.BLOCK_ENTITY_TYPE, "mixing_bowl",
            () -> Services.PLATFORM.blockEntityType(MixingBowlBlockEntity::new, ModBlocks.MIXING_BOWL.get()));

    public static final RegistryEntry<BlockEntityType<DisplayBlockEntity>> DISPLAY = Services.PLATFORM.register(
            Registries.BLOCK_ENTITY_TYPE, "display",
            () -> Services.PLATFORM.blockEntityType(DisplayBlockEntity::new,
                    ModBlocks.CAKE_DISPLAY.get(),
                    ModBlocks.BAKERY_COUNTER.get(),
                    ModBlocks.CUPCAKE_STAND.get(),
                    ModBlocks.DESSERT_PLATE.get(),
                    ModBlocks.GLASS_JAR.get()));

    private ModBlockEntities() {
    }

    /** Loads the class, which registers the block entity types. */
    public static void init() {
    }
}
