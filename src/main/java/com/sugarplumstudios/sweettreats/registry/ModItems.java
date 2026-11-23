package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.item.RecipeBookItem;
import com.sugarplumstudios.sweettreats.item.TreatItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Every item in Sweet Treats. Registration order is also the creative tab and recipe book order.
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SweetTreats.MOD_ID);
    /** Items that can go in a composter, with their chance. Filled during class init, applied in common setup. */
    public static final Map<Supplier<? extends Item>, Double> COMPOSTABLES = new LinkedHashMap<>();

    // =============================================================== Tools
    public static final RegistryObject<Item> RECIPE_BOOK = ITEMS.register("recipe_book",
            () -> new RecipeBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    // =============================================================== Machines & blocks
    public static final RegistryObject<Item> BAKERY_OVEN = blockItem("bakery_oven", ModBlocks.BAKERY_OVEN);
    public static final RegistryObject<Item> MIXING_BOWL = blockItem("mixing_bowl", ModBlocks.MIXING_BOWL);
    public static final RegistryObject<Item> DECORATION_STATION = blockItem("decoration_station", ModBlocks.DECORATION_STATION);

    // =============================================================== Crops, fruit & seeds
    public static final RegistryObject<Item> STRAWBERRY = compost(ITEMS.register("strawberry",
            () -> new Item(new Item.Properties().food(food(2, 0.3F).fast().build()))), 0.3);
    public static final RegistryObject<Item> STRAWBERRY_SEEDS = compost(ITEMS.register("strawberry_seeds",
            () -> new ItemNameBlockItem(ModBlocks.STRAWBERRY_CROP.get(), new Item.Properties())), 0.3);
    public static final RegistryObject<Item> VANILLA_POD = compost(simple("vanilla_pod"), 0.3);
    public static final RegistryObject<Item> VANILLA_SEEDS = compost(ITEMS.register("vanilla_seeds",
            () -> new ItemNameBlockItem(ModBlocks.VANILLA_CROP.get(), new Item.Properties())), 0.3);
    public static final RegistryObject<Item> LAVENDER = compost(simple("lavender"), 0.3);
    public static final RegistryObject<Item> LAVENDER_SEEDS = compost(ITEMS.register("lavender_seeds",
            () -> new ItemNameBlockItem(ModBlocks.LAVENDER_CROP.get(), new Item.Properties())), 0.3);
    public static final RegistryObject<Item> PEACH = compost(ITEMS.register("peach",
            () -> new Item(new Item.Properties().food(food(4, 0.3F).build()))), 0.65);
    public static final RegistryObject<Item> PEACH_PIT = compost(ITEMS.register("peach_pit",
            () -> new ItemNameBlockItem(ModBlocks.PEACH_TREE.get(), new Item.Properties())), 0.3);
    public static final RegistryObject<Item> CHERRY = compost(ITEMS.register("cherry",
            () -> new Item(new Item.Properties().food(food(2, 0.2F).fast().build()))), 0.3);
    public static final RegistryObject<Item> CHERRY_PIT = compost(ITEMS.register("cherry_pit",
            () -> new ItemNameBlockItem(ModBlocks.CHERRY_TREE.get(), new Item.Properties())), 0.3);
    public static final RegistryObject<Item> WILD_STRAWBERRIES = compost(blockItem("wild_strawberries", ModBlocks.WILD_STRAWBERRIES), 0.65);
    public static final RegistryObject<Item> WILD_LAVENDER = compost(blockItem("wild_lavender", ModBlocks.WILD_LAVENDER), 0.65);
    public static final RegistryObject<Item> WILD_VANILLA = compost(blockItem("wild_vanilla", ModBlocks.WILD_VANILLA), 0.65);

    // =============================================================== Baking ingredients
    public static final RegistryObject<Item> FLOUR = simple("flour");
    public static final RegistryObject<Item> CREAM = simple("cream");
    public static final RegistryObject<Item> BUTTER = simple("butter");
    public static final RegistryObject<Item> COCOA_POWDER = simple("cocoa_powder");
    public static final RegistryObject<Item> CHOCOLATE = ITEMS.register("chocolate",
            () -> new Item(new Item.Properties().food(food(3, 0.3F).fast().build())));
    public static final RegistryObject<Item> CINNAMON = simple("cinnamon");
    public static final RegistryObject<Item> SUGAR_FLOWERS = simple("sugar_flowers");
    public static final RegistryObject<Item> ICING = simple("icing");
    public static final RegistryObject<Item> PINK_ICING = simple("pink_icing");
    public static final RegistryObject<Item> CHOCOLATE_FROSTING = simple("chocolate_frosting");
    public static final RegistryObject<Item> SPRINKLES = simple("sprinkles");
    public static final RegistryObject<Item> TAPIOCA_PEARLS = simple("tapioca_pearls");
    public static final RegistryObject<Item> CAKE_BATTER = simple("cake_batter");
    public static final RegistryObject<Item> COOKIE_DOUGH = simple("cookie_dough");
    public static final RegistryObject<Item> PASTRY_DOUGH = simple("pastry_dough");
    public static final RegistryObject<Item> MACARON_BATTER = simple("macaron_batter");
    public static final RegistryObject<Item> SPONGE_CAKE = ITEMS.register("sponge_cake",
            () -> new Item(new Item.Properties().food(food(4, 0.3F).build())));
    public static final RegistryObject<Item> PLAIN_CUPCAKE = ITEMS.register("plain_cupcake",
            () -> new Item(new Item.Properties().food(food(3, 0.3F).build())));
    public static final RegistryObject<Item> SUGAR_COOKIE = ITEMS.register("sugar_cookie",
            () -> new Item(new Item.Properties().food(food(2, 0.1F).fast().build())));
    public static final RegistryObject<Item> MACARON_SHELLS = simple("macaron_shells");
    public static final RegistryObject<Item> TEACUP = simple("teacup");

    // =============================================================== Cakes & pastries
    public static final RegistryObject<Item> STRAWBERRY_CAKE = ITEMS.register("strawberry_cake",
            () -> new BlockItem(ModBlocks.STRAWBERRY_CAKE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PINK_VANILLA_CUPCAKE = ITEMS.register("pink_vanilla_cupcake",
            () -> new TreatItem(TreatItem.builder(5, 0.4F)));
    public static final RegistryObject<Item> CHOCOLATE_CUPCAKE = ITEMS.register("chocolate_cupcake",
            () -> new TreatItem(TreatItem.builder(5, 0.4F)));
    public static final RegistryObject<Item> LAVENDER_MACARON = ITEMS.register("lavender_macaron",
            () -> new TreatItem(TreatItem.builder(3, 0.3F).fast().effect(() -> MobEffects.REGENERATION, 5, 0)));
    public static final RegistryObject<Item> STRAWBERRY_MACARON = ITEMS.register("strawberry_macaron",
            () -> new TreatItem(TreatItem.builder(3, 0.3F).fast()));
    public static final RegistryObject<Item> HEART_COOKIE = ITEMS.register("heart_cookie",
            () -> new TreatItem(TreatItem.builder(3, 0.2F).fast()));
    public static final RegistryObject<Item> CINNAMON_ROLL = ITEMS.register("cinnamon_roll",
            () -> new TreatItem(TreatItem.builder(6, 0.5F)));
    public static final RegistryObject<Item> MINI_DONUTS = ITEMS.register("mini_donuts",
            () -> new TreatItem(TreatItem.builder(4, 0.3F).fast()));
    public static final RegistryObject<Item> PEACH_PASTRY = ITEMS.register("peach_pastry",
            () -> new TreatItem(TreatItem.builder(6, 0.5F)));
    public static final RegistryObject<Item> CHERRY_PIE = ITEMS.register("cherry_pie",
            () -> new BlockItem(ModBlocks.CHERRY_PIE.get(), new Item.Properties().stacksTo(1)));

    // =============================================================== Drinks
    public static final RegistryObject<Item> STRAWBERRY_MILK = ITEMS.register("strawberry_milk",
            () -> new TreatItem(TreatItem.builder(4, 0.3F).drink(() -> Items.GLASS_BOTTLE)));
    public static final RegistryObject<Item> VANILLA_MILKSHAKE = ITEMS.register("vanilla_milkshake",
            () -> new TreatItem(TreatItem.builder(5, 0.4F).drink(() -> Items.GLASS_BOTTLE)));
    public static final RegistryObject<Item> LAVENDER_TEA = ITEMS.register("lavender_tea",
            () -> new TreatItem(TreatItem.builder(3, 0.4F).drink(ModItems.TEACUP).effect(() -> MobEffects.REGENERATION, 10, 0)));
    public static final RegistryObject<Item> PEACH_TEA = ITEMS.register("peach_tea",
            () -> new TreatItem(TreatItem.builder(4, 0.4F).drink(ModItems.TEACUP).effect(() -> MobEffects.DIG_SPEED, 45, 0)));
    public static final RegistryObject<Item> BUBBLE_TEA = ITEMS.register("bubble_tea",
            () -> new TreatItem(TreatItem.builder(6, 0.5F).drink(() -> Items.GLASS_BOTTLE).effect(() -> MobEffects.JUMP, 45, 0)));
    public static final RegistryObject<Item> PINK_LEMONADE = ITEMS.register("pink_lemonade",
            () -> new TreatItem(TreatItem.builder(4, 0.3F).drink(() -> Items.GLASS_BOTTLE).effect(() -> MobEffects.MOVEMENT_SPEED, 30, 0)));
    public static final RegistryObject<Item> HOT_CHOCOLATE = ITEMS.register("hot_chocolate",
            () -> new TreatItem(TreatItem.builder(5, 0.6F).drink(ModItems.TEACUP).effect(() -> MobEffects.DAMAGE_RESISTANCE, 30, 0)));
    public static final RegistryObject<Item> ROSE_TEA = ITEMS.register("rose_tea",
            () -> new TreatItem(TreatItem.builder(3, 0.4F).drink(ModItems.TEACUP).effect(() -> MobEffects.ABSORPTION, 60, 0)));

    // =============================================================== Special treats
    public static final RegistryObject<Item> HEART_CANDY = ITEMS.register("heart_candy",
            () -> new TreatItem(TreatItem.builder(1, 0.1F).fast().effect(() -> MobEffects.REGENERATION, 4, 0)));
    public static final RegistryObject<Item> STRAWBERRY_LOLLIPOP = ITEMS.register("strawberry_lollipop",
            () -> new TreatItem(TreatItem.builder(2, 0.2F)));
    public static final RegistryObject<Item> MARSHMALLOW_CLOUD = ITEMS.register("marshmallow_cloud",
            () -> new TreatItem(TreatItem.builder(2, 0.2F).fast().effect(() -> MobEffects.SLOW_FALLING, 10, 0)));
    public static final RegistryObject<Item> RAINBOW_COOKIE = ITEMS.register("rainbow_cookie",
            () -> new TreatItem(TreatItem.builder(3, 0.3F).fast().effect(() -> MobEffects.LUCK, 60, 0)));
    public static final RegistryObject<Item> HONEY_CUPCAKE = ITEMS.register("honey_cupcake",
            () -> new TreatItem(TreatItem.builder(6, 0.5F)));
    public static final RegistryObject<Item> MOON_COOKIE = ITEMS.register("moon_cookie",
            () -> new TreatItem(TreatItem.builder(3, 0.3F).fast().effect(() -> MobEffects.NIGHT_VISION, 30, 0)));

    // =============================================================== Bakery decorations
    public static final RegistryObject<Item> CAKE_DISPLAY = blockItem("cake_display", ModBlocks.CAKE_DISPLAY);
    public static final RegistryObject<Item> BAKERY_COUNTER = blockItem("bakery_counter", ModBlocks.BAKERY_COUNTER);
    public static final RegistryObject<Item> CUPCAKE_STAND = blockItem("cupcake_stand", ModBlocks.CUPCAKE_STAND);
    public static final RegistryObject<Item> DESSERT_PLATE = blockItem("dessert_plate", ModBlocks.DESSERT_PLATE);
    public static final RegistryObject<Item> GLASS_JAR = blockItem("glass_jar", ModBlocks.GLASS_JAR);
    public static final RegistryObject<Item> TEA_SET = blockItem("tea_set", ModBlocks.TEA_SET);
    public static final RegistryObject<Item> BAKERY_SIGN = blockItem("bakery_sign", ModBlocks.BAKERY_SIGN);
    public static final RegistryObject<Item> DECORATIVE_MENU = blockItem("decorative_menu", ModBlocks.DECORATIVE_MENU);

    private ModItems() {
    }

    private static FoodProperties.Builder food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation);
    }

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> blockItem(String name, RegistryObject<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Item> RegistryObject<T> compost(RegistryObject<T> item, double chance) {
        COMPOSTABLES.put(item, chance);
        return item;
    }
}
