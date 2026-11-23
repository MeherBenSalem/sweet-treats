package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.SweetTreats;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SweetTreats.MOD_ID);

    // ---------------------------------------------------------------- Crops
    public static final RegistryObject<Block> STRAWBERRY_CROP = BLOCKS.register("strawberry_crop",
            () -> new SweetCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).mapColor(MapColor.COLOR_RED), ModItems.STRAWBERRY_SEEDS));
    public static final RegistryObject<Block> VANILLA_CROP = BLOCKS.register("vanilla_crop",
            () -> new SweetCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).mapColor(MapColor.PLANT), ModItems.VANILLA_SEEDS));
    public static final RegistryObject<Block> LAVENDER_CROP = BLOCKS.register("lavender_crop",
            () -> new SweetCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).mapColor(MapColor.COLOR_PURPLE), ModItems.LAVENDER_SEEDS));

    public static final RegistryObject<Block> PEACH_TREE = BLOCKS.register("peach_tree",
            () -> new FruitTreeBlock(fruitTreeProps(), ModItems.PEACH, ModItems.PEACH_PIT));
    public static final RegistryObject<Block> CHERRY_TREE = BLOCKS.register("cherry_tree",
            () -> new FruitTreeBlock(fruitTreeProps(), ModItems.CHERRY, ModItems.CHERRY_PIT));

    // ---------------------------------------------------------------- Wild plants (world generation)
    public static final RegistryObject<Block> WILD_STRAWBERRIES = BLOCKS.register("wild_strawberries",
            () -> new WildPlantBlock(wildProps(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> WILD_LAVENDER = BLOCKS.register("wild_lavender",
            () -> new WildPlantBlock(wildProps(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> WILD_VANILLA = BLOCKS.register("wild_vanilla",
            () -> new WildPlantBlock(wildProps(MapColor.PLANT)));

    // ---------------------------------------------------------------- Machines
    public static final RegistryObject<Block> BAKERY_OVEN = BLOCKS.register("bakery_oven",
            () -> new BakeryOvenBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0)));
    public static final RegistryObject<Block> MIXING_BOWL = BLOCKS.register("mixing_bowl",
            () -> new MixingBowlBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(1.0F)
                    .sound(SoundType.DECORATED_POT)
                    .noOcclusion()));
    public static final RegistryObject<Block> DECORATION_STATION = BLOCKS.register("decoration_station",
            () -> new DecorationStationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)));

    // ---------------------------------------------------------------- Placeable treats
    public static final RegistryObject<Block> STRAWBERRY_CAKE = BLOCKS.register("strawberry_cake",
            () -> new CakeTreatBlock(BlockBehaviour.Properties.copy(Blocks.CAKE).mapColor(MapColor.COLOR_PINK), 3, 0.2F));
    public static final RegistryObject<Block> CHERRY_PIE = BLOCKS.register("cherry_pie",
            () -> new PieTreatBlock(BlockBehaviour.Properties.copy(Blocks.CAKE).mapColor(MapColor.COLOR_RED), 4, 0.3F));

    // ---------------------------------------------------------------- Bakery decorations
    public static final RegistryObject<Block> CAKE_DISPLAY = BLOCKS.register("cake_display",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(1.0F)
                    .sound(SoundType.GLASS).noOcclusion(), DisplayType.CAKE_DISPLAY));
    public static final RegistryObject<Block> BAKERY_COUNTER = BLOCKS.register("bakery_counter",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(2.0F)
                    .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS), DisplayType.BAKERY_COUNTER));
    public static final RegistryObject<Block> CUPCAKE_STAND = BLOCKS.register("cupcake_stand",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.8F)
                    .sound(SoundType.DECORATED_POT).noOcclusion(), DisplayType.CUPCAKE_STAND));
    public static final RegistryObject<Block> DESSERT_PLATE = BLOCKS.register("dessert_plate",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.5F)
                    .sound(SoundType.DECORATED_POT).noOcclusion().pushReaction(PushReaction.DESTROY), DisplayType.DESSERT_PLATE));
    public static final RegistryObject<Block> GLASS_JAR = BLOCKS.register("glass_jar",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.6F)
                    .sound(SoundType.GLASS).noOcclusion(), DisplayType.GLASS_JAR));
    public static final RegistryObject<Block> TEA_SET = BLOCKS.register("tea_set",
            () -> new DecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.8F)
                    .sound(SoundType.DECORATED_POT).noOcclusion(), DecorBlock.Kind.TEA_SET));
    public static final RegistryObject<Block> BAKERY_SIGN = BLOCKS.register("bakery_sign",
            () -> new DecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(1.0F)
                    .sound(SoundType.WOOD).noOcclusion().noCollission(), DecorBlock.Kind.BAKERY_SIGN));
    public static final RegistryObject<Block> DECORATIVE_MENU = BLOCKS.register("decorative_menu",
            () -> new DecorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.0F)
                    .sound(SoundType.WOOD).noOcclusion(), DecorBlock.Kind.MENU_BOARD));

    private ModBlocks() {
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
