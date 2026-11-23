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
import com.sugarplumstudios.sweettreats.SweetTreats;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
            () -> new SweetCropBlock(copy(Blocks.WHEAT, "strawberry_crop").mapColor(MapColor.COLOR_RED), () -> ModItems.STRAWBERRY_SEEDS.get()));
    public static final RegistryEntry<Block> VANILLA_CROP = register("vanilla_crop",
            () -> new SweetCropBlock(copy(Blocks.WHEAT, "vanilla_crop").mapColor(MapColor.PLANT), () -> ModItems.VANILLA_SEEDS.get()));
    public static final RegistryEntry<Block> LAVENDER_CROP = register("lavender_crop",
            () -> new SweetCropBlock(copy(Blocks.WHEAT, "lavender_crop").mapColor(MapColor.COLOR_PURPLE), () -> ModItems.LAVENDER_SEEDS.get()));

    public static final RegistryEntry<Block> PEACH_TREE = register("peach_tree",
            () -> new FruitTreeBlock(fruitTreeProps("peach_tree"), () -> ModItems.PEACH.get(), () -> ModItems.PEACH_PIT.get()));
    public static final RegistryEntry<Block> CHERRY_TREE = register("cherry_tree",
            () -> new FruitTreeBlock(fruitTreeProps("cherry_tree"), () -> ModItems.CHERRY.get(), () -> ModItems.CHERRY_PIT.get()));

    // ---------------------------------------------------------------- Wild plants (world generation)
    public static final RegistryEntry<Block> WILD_STRAWBERRIES = register("wild_strawberries",
            () -> new WildPlantBlock(wildProps(MapColor.COLOR_RED, "wild_strawberries")));
    public static final RegistryEntry<Block> WILD_LAVENDER = register("wild_lavender",
            () -> new WildPlantBlock(wildProps(MapColor.COLOR_PURPLE, "wild_lavender")));
    public static final RegistryEntry<Block> WILD_VANILLA = register("wild_vanilla",
            () -> new WildPlantBlock(wildProps(MapColor.PLANT, "wild_vanilla")));

    // ---------------------------------------------------------------- Machines
    public static final RegistryEntry<Block> BAKERY_OVEN = register("bakery_oven",
            () -> new BakeryOvenBlock(block("bakery_oven")
                    .mapColor(MapColor.COLOR_PINK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0)));
    public static final RegistryEntry<Block> MIXING_BOWL = register("mixing_bowl",
            () -> new MixingBowlBlock(block("mixing_bowl")
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(1.0F)
                    .sound(SoundType.DECORATED_POT)
                    .noOcclusion()));
    public static final RegistryEntry<Block> DECORATION_STATION = register("decoration_station",
            () -> new DecorationStationBlock(block("decoration_station")
                    .mapColor(MapColor.COLOR_PINK)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)));

    // ---------------------------------------------------------------- Placeable treats
    public static final RegistryEntry<Block> STRAWBERRY_CAKE = register("strawberry_cake",
            () -> new CakeTreatBlock(copy(Blocks.CAKE, "strawberry_cake").mapColor(MapColor.COLOR_PINK), 3, 0.2F));
    public static final RegistryEntry<Block> CHERRY_PIE = register("cherry_pie",
            () -> new PieTreatBlock(copy(Blocks.CAKE, "cherry_pie").mapColor(MapColor.COLOR_RED), 4, 0.3F));

    // ---------------------------------------------------------------- Bakery decorations
    public static final RegistryEntry<Block> CAKE_DISPLAY = register("cake_display",
            () -> new DisplayBlock(block("cake_display").mapColor(MapColor.COLOR_PINK).strength(1.0F)
                    .sound(SoundType.GLASS).noOcclusion(), DisplayType.CAKE_DISPLAY));
    public static final RegistryEntry<Block> BAKERY_COUNTER = register("bakery_counter",
            () -> new DisplayBlock(block("bakery_counter").mapColor(MapColor.COLOR_PINK).strength(2.0F)
                    .sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS), DisplayType.BAKERY_COUNTER));
    public static final RegistryEntry<Block> CUPCAKE_STAND = register("cupcake_stand",
            () -> new DisplayBlock(block("cupcake_stand").mapColor(MapColor.SNOW).strength(0.8F)
                    .sound(SoundType.DECORATED_POT).noOcclusion(), DisplayType.CUPCAKE_STAND));
    public static final RegistryEntry<Block> DESSERT_PLATE = register("dessert_plate",
            () -> new DisplayBlock(block("dessert_plate").mapColor(MapColor.SNOW).strength(0.5F)
                    .sound(SoundType.DECORATED_POT).noOcclusion().pushReaction(PushReaction.DESTROY), DisplayType.DESSERT_PLATE));
    public static final RegistryEntry<Block> GLASS_JAR = register("glass_jar",
            () -> new DisplayBlock(block("glass_jar").mapColor(MapColor.NONE).strength(0.6F)
                    .sound(SoundType.GLASS).noOcclusion(), DisplayType.GLASS_JAR));
    public static final RegistryEntry<Block> TEA_SET = register("tea_set",
            () -> new DecorBlock(block("tea_set").mapColor(MapColor.SNOW).strength(0.8F)
                    .sound(SoundType.DECORATED_POT).noOcclusion(), DecorBlock.Kind.TEA_SET));
    public static final RegistryEntry<Block> BAKERY_SIGN = register("bakery_sign",
            () -> new DecorBlock(block("bakery_sign").mapColor(MapColor.COLOR_PINK).strength(1.0F)
                    .sound(SoundType.WOOD).noOcclusion().noCollision(), DecorBlock.Kind.BAKERY_SIGN));
    public static final RegistryEntry<Block> DECORATIVE_MENU = register("decorative_menu",
            () -> new DecorBlock(block("decorative_menu").mapColor(MapColor.COLOR_BLACK).strength(1.0F)
                    .sound(SoundType.WOOD).noOcclusion(), DecorBlock.Kind.MENU_BOARD));

    private ModBlocks() {
    }

    /** Loads the class, which registers every block (in this order). */
    public static void init() {
    }

    private static RegistryEntry<Block> register(String name, Supplier<Block> factory) {
        return Services.PLATFORM.register(Registries.BLOCK, name, factory);
    }

    /** Since 1.21.2 a block carries its registry key in its properties; this starts a property set for a name. */
    private static BlockBehaviour.Properties block(String name) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, SweetTreats.id(name)));
    }

    /** All the properties of {@code template}, under this block's own key. */
    private static BlockBehaviour.Properties copy(Block template, String name) {
        return BlockBehaviour.Properties.ofFullCopy(template).setId(ResourceKey.create(Registries.BLOCK, SweetTreats.id(name)));
    }

    private static BlockBehaviour.Properties fruitTreeProps(String name) {
        return block(name)
                .mapColor(MapColor.PLANT)
                .randomTicks()
                .noCollision()
                .instabreak()
                .sound(SoundType.SWEET_BERRY_BUSH)
                .pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties wildProps(MapColor color, String name) {
        return block(name)
                .mapColor(color)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY);
    }
}
