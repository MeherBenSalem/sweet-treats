package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.platform.RegistryEntry;
import com.sugarplumstudios.sweettreats.platform.Services;
import com.sugarplumstudios.sweettreats.block.BakeryOvenBlock;
import com.sugarplumstudios.sweettreats.block.CakeTreatBlock;
import com.sugarplumstudios.sweettreats.block.DecorBlock;
import com.sugarplumstudios.sweettreats.block.DecorationStationBlock;
import com.sugarplumstudios.sweettreats.block.DisplayBlock;
import com.sugarplumstudios.sweettreats.block.DisplayType;
import com.sugarplumstudios.sweettreats.block.FruitTreeBlock;
import com.sugarplumstudios.sweettreats.block.MixingBowlBlock;
import com.sugarplumstudios.sweettreats.block.PieTreatBlock;
import com.sugarplumstudios.sweettreats.block.SweetCropBlock;
import com.sugarplumstudios.sweettreats.block.WildPlantBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public final class ModBlocks {

    // ---------------------------------------------------------------- Crops
    public static final RegistryEntry<Block> STRAWBERRY_CROP = register("strawberry_crop",
            () -> new SweetCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).mapColor(MapColor.COLOR_RED), () -> ModItems.STRAWBERRY_SEEDS.get()));
    public static final RegistryEntry<Block> VANILLA_CROP = register("vanilla_crop",
            () -> new SweetCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).mapColor(MapColor.PLANT), () -> ModItems.VANILLA_SEEDS.get()));
    public static final RegistryEntry<Block> LAVENDER_CROP = register("lavender_crop",
            () -> new SweetCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).mapColor(MapColor.COLOR_PURPLE), () -> ModItems.LAVENDER_SEEDS.get()));

    public static final RegistryEntry<Block> PEACH_TREE = register("peach_tree",
            () -> new FruitTreeBlock(fruitTreeProps(), () -> ModItems.PEACH.get(), () -> ModItems.PEACH_PIT.get()));
    public static final RegistryEntry<Block> CHERRY_TREE = register("cherry_tree",
            () -> new FruitTreeBlock(fruitTreeProps(), () -> ModItems.CHERRY.get(), () -> ModItems.CHERRY_PIT.get()));

    // ---------------------------------------------------------------- Wild plants (world generation)
    public static final RegistryEntry<Block> WILD_STRAWBERRIES = register("wild_strawberries",
            () -> new WildPlantBlock(wildProps(MapColor.COLOR_RED)));
    public static final RegistryEntry<Block> WILD_LAVENDER = register("wild_lavender",
            () -> new WildPlantBlock(wildProps(MapColor.COLOR_PURPLE)));
    public static final RegistryEntry<Block> WILD_VANILLA = register("wild_vanilla",
            () -> new WildPlantBlock(wildProps(MapColor.PLANT)));

    // ---------------------------------------------------------------- Machines
    public static final RegistryEntry<Block> BAKERY_OVEN = register("bakery_oven",
            () -> new BakeryOvenBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0)));
    public static final RegistryEntry<Block> MIXING_BOWL = register("mixing_bowl",
            () -> new MixingBowlBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(1.0F)
                    .sound(SoundType.DECORATED_POT)
                    .noOcclusion()));
    public static final RegistryEntry<Block> DECORATION_STATION = register("decoration_station",
            () -> new DecorationStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)));

    // ---------------------------------------------------------------- Placeable treats
    public static final RegistryEntry<Block> STRAWBERRY_CAKE = register("strawberry_cake",
            () -> new CakeTreatBlock(BlockBehaviour.Properties.copy(Blocks.CAKE).mapColor(MapColor.COLOR_PINK), 3, 0.2F));
    public static final RegistryEntry<Block> CHERRY_PIE = register("cherry_pie",
            () -> new PieTreatBlock(BlockBehaviour.Properties.copy(Blocks.CAKE).mapColor(MapColor.COLOR_RED), 4, 0.3F));

    // ---------------------------------------------------------------- Bakery decorations
    public static final RegistryEntry<Block> CAKE_DISPLAY = register("cake_display",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(1.0F)
                    .sound(SoundType.GLASS).noOcclusion(), DisplayType.CAKE_DISPLAY));
    public static final RegistryEntry<Block> BAKERY_COUNTER = register("bakery_counter",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(2.0F)
                    .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS), DisplayType.BAKERY_COUNTER));
    public static final RegistryEntry<Block> CUPCAKE_STAND = register("cupcake_stand",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.8F)
                    .sound(SoundType.DECORATED_POT).noOcclusion(), DisplayType.CUPCAKE_STAND));
    public static final RegistryEntry<Block> DESSERT_PLATE = register("dessert_plate",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.5F)
                    .sound(SoundType.DECORATED_POT).noOcclusion().pushReaction(PushReaction.DESTROY), DisplayType.DESSERT_PLATE));
    public static final RegistryEntry<Block> GLASS_JAR = register("glass_jar",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.6F)
                    .sound(SoundType.GLASS).noOcclusion(), DisplayType.GLASS_JAR));
    public static final RegistryEntry<Block> TEA_SET = register("tea_set",
            () -> new DecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.8F)
                    .sound(SoundType.DECORATED_POT).noOcclusion(), DecorBlock.Kind.TEA_SET));
    public static final RegistryEntry<Block> BAKERY_SIGN = register("bakery_sign",
            () -> new DecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(1.0F)
                    .sound(SoundType.WOOD).noOcclusion().noCollission(), DecorBlock.Kind.BAKERY_SIGN));
    public static final RegistryEntry<Block> DECORATIVE_MENU = register("decorative_menu",
            () -> new DecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.0F)
                    .sound(SoundType.WOOD).noOcclusion(), DecorBlock.Kind.MENU_BOARD));

    private ModBlocks() {
    }

    /** Loads the class, which registers every block (in this order). */
    public static void init() {
    }

    private static RegistryEntry<Block> register(String name, Supplier<Block> factory) {
        return Services.PLATFORM.register(Registries.BLOCK, name, factory);
    }

    private static BlockBehaviour.Properties fruitTreeProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .randomTicks()
                .noCollission()
                .instabreak()
                .sound(SoundType.SWEET_BERRY_BUSH)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties wildProps(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY);
    }
}
