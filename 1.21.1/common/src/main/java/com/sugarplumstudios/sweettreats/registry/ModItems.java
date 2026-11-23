package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.item.RecipeBookItem;
import com.sugarplumstudios.sweettreats.item.TreatItem;
import com.sugarplumstudios.sweettreats.platform.RegistryEntry;
import com.sugarplumstudios.sweettreats.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Every item in Sweet Treats. Registration order is also the creative tab and recipe book order.
 */
public final class ModItems {
    /** Every item, in registration order (also the creative tab order). */
    public static final List<RegistryEntry<Item>> ALL = new ArrayList<>();
    /** Items that can go in a composter, with their chance. Filled during class init, applied in common setup. */
    public static final Map<Supplier<? extends Item>, Double> COMPOSTABLES = new LinkedHashMap<>();

    // =============================================================== Tools
    public static final RegistryEntry<Item> RECIPE_BOOK = register("recipe_book",
            () -> new RecipeBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    // =============================================================== Machines & blocks
    public static final RegistryEntry<Item> BAKERY_OVEN = blockItem("bakery_oven", ModBlocks.BAKERY_OVEN);
    public static final RegistryEntry<Item> MIXING_BOWL = blockItem("mixing_bowl", ModBlocks.MIXING_BOWL);
    public static final RegistryEntry<Item> DECORATION_STATION = blockItem("decoration_station", ModBlocks.DECORATION_STATION);

    // =============================================================== Crops, fruit & seeds
    public static final RegistryEntry<Item> STRAWBERRY = compost(register("strawberry",
            () -> new Item(new Item.Properties().food(food(2, 0.3F).fast().build()))), 0.3);
    public static final RegistryEntry<Item> STRAWBERRY_SEEDS = compost(register("strawberry_seeds",
            () -> new ItemNameBlockItem(ModBlocks.STRAWBERRY_CROP.get(), new Item.Properties())), 0.3);
    public static final RegistryEntry<Item> VANILLA_POD = compost(simple("vanilla_pod"), 0.3);
    public static final RegistryEntry<Item> VANILLA_SEEDS = compost(register("vanilla_seeds",
            () -> new ItemNameBlockItem(ModBlocks.VANILLA_CROP.get(), new Item.Properties())), 0.3);
    public static final RegistryEntry<Item> LAVENDER = compost(simple("lavender"), 0.3);
    public static final RegistryEntry<Item> LAVENDER_SEEDS = compost(register("lavender_seeds",
            () -> new ItemNameBlockItem(ModBlocks.LAVENDER_CROP.get(), new Item.Properties())), 0.3);
    public static final RegistryEntry<Item> PEACH = compost(register("peach",
            () -> new Item(new Item.Properties().food(food(4, 0.3F).build()))), 0.65);
    public static final RegistryEntry<Item> PEACH_PIT = compost(register("peach_pit",
            () -> new ItemNameBlockItem(ModBlocks.PEACH_TREE.get(), new Item.Properties())), 0.3);
    public static final RegistryEntry<Item> CHERRY = compost(register("cherry",
            () -> new Item(new Item.Properties().food(food(2, 0.2F).fast().build()))), 0.3);
    public static final RegistryEntry<Item> CHERRY_PIT = compost(register("cherry_pit",
            () -> new ItemNameBlockItem(ModBlocks.CHERRY_TREE.get(), new Item.Properties())), 0.3);
    public static final RegistryEntry<Item> WILD_STRAWBERRIES = compost(blockItem("wild_strawberries", ModBlocks.WILD_STRAWBERRIES), 0.65);
    public static final RegistryEntry<Item> WILD_LAVENDER = compost(blockItem("wild_lavender", ModBlocks.WILD_LAVENDER), 0.65);
    public static final RegistryEntry<Item> WILD_VANILLA = compost(blockItem("wild_vanilla", ModBlocks.WILD_VANILLA), 0.65);

    // =============================================================== Baking ingredients
    public static final RegistryEntry<Item> FLOUR = simple("flour");
    public static final RegistryEntry<Item> CREAM = simple("cream");
    public static final RegistryEntry<Item> BUTTER = simple("butter");
    public static final RegistryEntry<Item> COCOA_POWDER = simple("cocoa_powder");
    public static final RegistryEntry<Item> CHOCOLATE = register("chocolate",
            () -> new Item(new Item.Properties().food(food(3, 0.3F).fast().build())));
    public static final RegistryEntry<Item> CINNAMON = simple("cinnamon");
    public static final RegistryEntry<Item> SUGAR_FLOWERS = simple("sugar_flowers");
    public static final RegistryEntry<Item> ICING = simple("icing");
    public static final RegistryEntry<Item> PINK_ICING = simple("pink_icing");
    public static final RegistryEntry<Item> CHOCOLATE_FROSTING = simple("chocolate_frosting");
    public static final RegistryEntry<Item> SPRINKLES = simple("sprinkles");
    public static final RegistryEntry<Item> TAPIOCA_PEARLS = simple("tapioca_pearls");
    public static final RegistryEntry<Item> CAKE_BATTER = simple("cake_batter");
    public static final RegistryEntry<Item> COOKIE_DOUGH = simple("cookie_dough");
    public static final RegistryEntry<Item> PASTRY_DOUGH = simple("pastry_dough");
    public static final RegistryEntry<Item> MACARON_BATTER = simple("macaron_batter");
    public static final RegistryEntry<Item> SPONGE_CAKE = register("sponge_cake",
            () -> new Item(new Item.Properties().food(food(4, 0.3F).build())));
    public static final RegistryEntry<Item> PLAIN_CUPCAKE = register("plain_cupcake",
            () -> new Item(new Item.Properties().food(food(3, 0.3F).build())));
    public static final RegistryEntry<Item> SUGAR_COOKIE = register("sugar_cookie",
            () -> new Item(new Item.Properties().food(food(2, 0.1F).fast().build())));
    public static final RegistryEntry<Item> MACARON_SHELLS = simple("macaron_shells");
    public static final RegistryEntry<Item> TEACUP = simple("teacup");

    // =============================================================== Cakes & pastries
    public static final RegistryEntry<Item> STRAWBERRY_CAKE = register("strawberry_cake",
            () -> new BlockItem(ModBlocks.STRAWBERRY_CAKE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryEntry<Item> PINK_VANILLA_CUPCAKE = register("pink_vanilla_cupcake",
            () -> new TreatItem(TreatItem.builder(5, 0.4F)));
    public static final RegistryEntry<Item> CHOCOLATE_CUPCAKE = register("chocolate_cupcake",
            () -> new TreatItem(TreatItem.builder(5, 0.4F)));
    public static final RegistryEntry<Item> LAVENDER_MACARON = register("lavender_macaron",
            () -> new TreatItem(TreatItem.builder(3, 0.3F).fast().effect(() -> MobEffects.REGENERATION, 5, 0)));
    public static final RegistryEntry<Item> STRAWBERRY_MACARON = register("strawberry_macaron",
            () -> new TreatItem(TreatItem.builder(3, 0.3F).fast()));
    public static final RegistryEntry<Item> HEART_COOKIE = register("heart_cookie",
            () -> new TreatItem(TreatItem.builder(3, 0.2F).fast()));
    public static final RegistryEntry<Item> CINNAMON_ROLL = register("cinnamon_roll",
            () -> new TreatItem(TreatItem.builder(6, 0.5F)));
    public static final RegistryEntry<Item> MINI_DONUTS = register("mini_donuts",
            () -> new TreatItem(TreatItem.builder(4, 0.3F).fast()));
    public static final RegistryEntry<Item> PEACH_PASTRY = register("peach_pastry",
            () -> new TreatItem(TreatItem.builder(6, 0.5F)));
    public static final RegistryEntry<Item> CHERRY_PIE = register("cherry_pie",
            () -> new BlockItem(ModBlocks.CHERRY_PIE.get(), new Item.Properties().stacksTo(1)));

    // =============================================================== Drinks
    public static final RegistryEntry<Item> STRAWBERRY_MILK = register("strawberry_milk",
            () -> new TreatItem(TreatItem.builder(4, 0.3F).drink(() -> Items.GLASS_BOTTLE)));
    public static final RegistryEntry<Item> VANILLA_MILKSHAKE = register("vanilla_milkshake",
            () -> new TreatItem(TreatItem.builder(5, 0.4F).drink(() -> Items.GLASS_BOTTLE)));
    public static final RegistryEntry<Item> LAVENDER_TEA = register("lavender_tea",
            () -> new TreatItem(TreatItem.builder(3, 0.4F).drink(ModItems.TEACUP).effect(() -> MobEffects.REGENERATION, 10, 0)));
    public static final RegistryEntry<Item> PEACH_TEA = register("peach_tea",
            () -> new TreatItem(TreatItem.builder(4, 0.4F).drink(ModItems.TEACUP).effect(() -> MobEffects.DIG_SPEED, 45, 0)));
    public static final RegistryEntry<Item> BUBBLE_TEA = register("bubble_tea",
            () -> new TreatItem(TreatItem.builder(6, 0.5F).drink(() -> Items.GLASS_BOTTLE).effect(() -> MobEffects.JUMP, 45, 0)));
    public static final RegistryEntry<Item> PINK_LEMONADE = register("pink_lemonade",
            () -> new TreatItem(TreatItem.builder(4, 0.3F).drink(() -> Items.GLASS_BOTTLE).effect(() -> MobEffects.MOVEMENT_SPEED, 30, 0)));
    public static final RegistryEntry<Item> HOT_CHOCOLATE = register("hot_chocolate",
            () -> new TreatItem(TreatItem.builder(5, 0.6F).drink(ModItems.TEACUP).effect(() -> MobEffects.DAMAGE_RESISTANCE, 30, 0)));
    public static final RegistryEntry<Item> ROSE_TEA = register("rose_tea",
            () -> new TreatItem(TreatItem.builder(3, 0.4F).drink(ModItems.TEACUP).effect(() -> MobEffects.ABSORPTION, 60, 0)));

    // =============================================================== Special treats
    public static final RegistryEntry<Item> HEART_CANDY = register("heart_candy",
            () -> new TreatItem(TreatItem.builder(1, 0.1F).fast().effect(() -> MobEffects.REGENERATION, 4, 0)));
    public static final RegistryEntry<Item> STRAWBERRY_LOLLIPOP = register("strawberry_lollipop",
            () -> new TreatItem(TreatItem.builder(2, 0.2F)));
    public static final RegistryEntry<Item> MARSHMALLOW_CLOUD = register("marshmallow_cloud",
            () -> new TreatItem(TreatItem.builder(2, 0.2F).fast().effect(() -> MobEffects.SLOW_FALLING, 10, 0)));
    public static final RegistryEntry<Item> RAINBOW_COOKIE = register("rainbow_cookie",
            () -> new TreatItem(TreatItem.builder(3, 0.3F).fast().effect(() -> MobEffects.LUCK, 60, 0)));
    public static final RegistryEntry<Item> HONEY_CUPCAKE = register("honey_cupcake",
            () -> new TreatItem(TreatItem.builder(6, 0.5F)));
    public static final RegistryEntry<Item> MOON_COOKIE = register("moon_cookie",
            () -> new TreatItem(TreatItem.builder(3, 0.3F).fast().effect(() -> MobEffects.NIGHT_VISION, 30, 0)));

    // =============================================================== Bakery decorations
    public static final RegistryEntry<Item> CAKE_DISPLAY = blockItem("cake_display", ModBlocks.CAKE_DISPLAY);
    public static final RegistryEntry<Item> BAKERY_COUNTER = blockItem("bakery_counter", ModBlocks.BAKERY_COUNTER);
    public static final RegistryEntry<Item> CUPCAKE_STAND = blockItem("cupcake_stand", ModBlocks.CUPCAKE_STAND);
    public static final RegistryEntry<Item> DESSERT_PLATE = blockItem("dessert_plate", ModBlocks.DESSERT_PLATE);
    public static final RegistryEntry<Item> GLASS_JAR = blockItem("glass_jar", ModBlocks.GLASS_JAR);
    public static final RegistryEntry<Item> TEA_SET = blockItem("tea_set", ModBlocks.TEA_SET);
    public static final RegistryEntry<Item> BAKERY_SIGN = blockItem("bakery_sign", ModBlocks.BAKERY_SIGN);
    public static final RegistryEntry<Item> DECORATIVE_MENU = blockItem("decorative_menu", ModBlocks.DECORATIVE_MENU);

    private ModItems() {
    }

    /** Loads the class, which registers every item (in this order). */
    public static void init() {
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> RegistryEntry<T> register(String name, Supplier<T> factory) {
        RegistryEntry<T> entry = Services.PLATFORM.register(Registries.ITEM, name, factory);
        ALL.add((RegistryEntry<Item>) entry);
        return entry;
    }

    private static FoodProperties.Builder food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
    }

    private static RegistryEntry<Item> simple(String name) {
        return register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryEntry<Item> blockItem(String name, RegistryEntry<? extends Block> block) {
        return register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Item> RegistryEntry<T> compost(RegistryEntry<T> item, double chance) {
        COMPOSTABLES.put(item, chance);
        return item;
    }
}
