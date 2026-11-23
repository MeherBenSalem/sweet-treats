package com.sugarplumstudios.sweettreats;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * All configurable features of Sweet Treats.
 * Common config lives in config/sweettreats-common.toml, client config in config/sweettreats-client.toml.
 */
public final class SweetTreatsConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Common, ForgeConfigSpec> common = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = common.getLeft();
        COMMON_SPEC = common.getRight();
        Pair<Client, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();
    }

    private SweetTreatsConfig() {
    }

    public static final class Common {
        // Food
        public final ForgeConfigSpec.BooleanValue foodEffects;
        public final ForgeConfigSpec.DoubleValue effectDurationMultiplier;
        // Machines
        public final ForgeConfigSpec.DoubleValue ovenSpeedMultiplier;
        public final ForgeConfigSpec.BooleanValue ovenRequiresFuel;
        public final ForgeConfigSpec.DoubleValue mixingSpeedMultiplier;
        // Recipe book
        public final ForgeConfigSpec.BooleanValue giveRecipeBookOnFirstJoin;
        public final ForgeConfigSpec.BooleanValue discoveryMessages;
        public final ForgeConfigSpec.BooleanValue hideUndiscoveredIngredients;
        // World
        public final ForgeConfigSpec.BooleanValue grassSeedDrops;
        public final ForgeConfigSpec.BooleanValue cherryLeafDrops;
        public final ForgeConfigSpec.DoubleValue dropChanceMultiplier;
        public final ForgeConfigSpec.IntValue fruitTreeGrowthChance;
        public final ForgeConfigSpec.BooleanValue wanderingTraderTrades;

        Common(ForgeConfigSpec.Builder b) {
            b.comment("Food and drink settings").push("food");
            foodEffects = b.comment("If true, treats and drinks grant their small bonus effects (e.g. lavender tea -> Regeneration).")
                    .define("foodEffects", true);
            effectDurationMultiplier = b.comment("Multiplier applied to the duration of every treat effect.")
                    .defineInRange("effectDurationMultiplier", 1.0D, 0.1D, 10.0D);
            b.pop();

            b.comment("Bakery machines").push("machines");
            ovenSpeedMultiplier = b.comment("Bakery Oven speed. 2.0 bakes twice as fast, 0.5 takes twice as long.")
                    .defineInRange("ovenSpeedMultiplier", 1.0D, 0.1D, 10.0D);
            ovenRequiresFuel = b.comment("If false, the Bakery Oven bakes without consuming fuel.")
                    .define("ovenRequiresFuel", true);
            mixingSpeedMultiplier = b.comment("Mixing Bowl speed. 2.0 mixes twice as fast.")
                    .defineInRange("mixingSpeedMultiplier", 1.0D, 0.1D, 10.0D);
            b.pop();

            b.comment("Recipe book and recipe discovery").push("recipe_book");
            giveRecipeBookOnFirstJoin = b.comment("Give every player a Sweet Treats Recipe Book the first time they join a world.")
                    .define("giveRecipeBookOnFirstJoin", true);
            discoveryMessages = b.comment("Show a message and play a chime when a player discovers a new recipe.")
                    .define("discoveryMessages", true);
            hideUndiscoveredIngredients = b.comment("If true, the recipe book hides the ingredients of recipes the player has not discovered yet.")
                    .define("hideUndiscoveredIngredients", false);
            b.pop();

            b.comment("World and loot").push("world");
            grassSeedDrops = b.comment("Grass and tall grass can drop strawberry seeds, vanilla seeds, lavender seeds, peach pits and cherry pits.")
                    .define("grassSeedDrops", true);
            cherryLeafDrops = b.comment("Cherry leaves can drop cherries and cherry pits.")
                    .define("cherryLeafDrops", true);
            dropChanceMultiplier = b.comment("Multiplier for the chance of the extra seed/fruit drops above.")
                    .defineInRange("dropChanceMultiplier", 1.0D, 0.0D, 10.0D);
            fruitTreeGrowthChance = b.comment("Peach and cherry trees grow one stage with a 1-in-N chance per random tick. Lower = faster.")
                    .defineInRange("fruitTreeGrowthChance", 5, 1, 100);
            wanderingTraderTrades = b.comment("Wandering traders sell Sweet Treats seeds and pits.")
                    .define("wanderingTraderTrades", true);
            b.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue cookingParticles;
        public final ForgeConfigSpec.BooleanValue cookingAnimations;

        Client(ForgeConfigSpec.Builder b) {
            b.comment("Visual settings").push("visuals");
            cookingParticles = b.comment("Show smoke/flour particles while the oven and mixing bowl are working.")
                    .define("cookingParticles", true);
            cookingAnimations = b.comment("Animate ingredients swirling in the mixing bowl and treats spinning in cake displays.")
                    .define("cookingAnimations", true);
            b.pop();
        }
    }

    // ---- Safe accessors (fall back to defaults if read before the config is loaded) ----

    public static boolean foodEffects() {
        return get(COMMON.foodEffects, true);
    }

    public static double effectDurationMultiplier() {
        return get(COMMON.effectDurationMultiplier, 1.0D);
    }

    public static double ovenSpeedMultiplier() {
        return get(COMMON.ovenSpeedMultiplier, 1.0D);
    }

    public static boolean ovenRequiresFuel() {
        return get(COMMON.ovenRequiresFuel, true);
    }

    public static double mixingSpeedMultiplier() {
        return get(COMMON.mixingSpeedMultiplier, 1.0D);
    }

    public static boolean giveRecipeBookOnFirstJoin() {
        return get(COMMON.giveRecipeBookOnFirstJoin, true);
    }

    public static boolean discoveryMessages() {
        return get(COMMON.discoveryMessages, true);
    }

    public static boolean hideUndiscoveredIngredients() {
        return get(COMMON.hideUndiscoveredIngredients, false);
    }

    public static boolean grassSeedDrops() {
        return get(COMMON.grassSeedDrops, true);
    }

    public static boolean cherryLeafDrops() {
        return get(COMMON.cherryLeafDrops, true);
    }

    public static double dropChanceMultiplier() {
        return get(COMMON.dropChanceMultiplier, 1.0D);
    }

    public static int fruitTreeGrowthChance() {
        return get(COMMON.fruitTreeGrowthChance, 5);
    }

    public static boolean wanderingTraderTrades() {
        return get(COMMON.wanderingTraderTrades, true);
    }

    public static boolean cookingParticles() {
        return get(CLIENT.cookingParticles, true);
    }

    public static boolean cookingAnimations() {
        return get(CLIENT.cookingAnimations, true);
    }

    private static <T> T get(ForgeConfigSpec.ConfigValue<T> value, T fallback) {
        try {
            return value.get();
        } catch (IllegalStateException notLoadedYet) {
            return fallback;
        }
    }
}
