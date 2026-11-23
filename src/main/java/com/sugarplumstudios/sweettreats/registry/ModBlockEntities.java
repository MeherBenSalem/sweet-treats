package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.blockentity.BakeryOvenBlockEntity;
import com.sugarplumstudios.sweettreats.blockentity.DisplayBlockEntity;
import com.sugarplumstudios.sweettreats.blockentity.MixingBowlBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SweetTreats.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<BakeryOvenBlockEntity>> BAKERY_OVEN = BLOCK_ENTITIES.register("bakery_oven",
            () -> BlockEntityType.Builder.of(BakeryOvenBlockEntity::new, ModBlocks.BAKERY_OVEN.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<MixingBowlBlockEntity>> MIXING_BOWL = BLOCK_ENTITIES.register("mixing_bowl",
            () -> BlockEntityType.Builder.of(MixingBowlBlockEntity::new, ModBlocks.MIXING_BOWL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<DisplayBlockEntity>> DISPLAY = BLOCK_ENTITIES.register("display",
            () -> BlockEntityType.Builder.of(DisplayBlockEntity::new,
                    ModBlocks.CAKE_DISPLAY.get(),
                    ModBlocks.BAKERY_COUNTER.get(),
                    ModBlocks.CUPCAKE_STAND.get(),
                    ModBlocks.DESSERT_PLATE.get(),
                    ModBlocks.GLASS_JAR.get()).build(null));

    private ModBlockEntities() {
    }
}
