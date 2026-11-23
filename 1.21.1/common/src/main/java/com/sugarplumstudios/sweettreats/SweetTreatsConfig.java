package com.sugarplumstudios.sweettreats;

import com.sugarplumstudios.sweettreats.config.TomlConfig;
import com.sugarplumstudios.sweettreats.platform.Services;

/**
 * The mod's two config files, {@code config/sweettreats-common.toml} (gameplay rules) and
 * {@code config/sweettreats-client.toml} (visuals). Same layout and keys as the Forge 1.0 files, on every loader.
 */
public final class SweetTreatsConfig {
    private static final TomlConfig.Builder COMMON_BUILDER = new TomlConfig.Builder();
    public static final Common COMMON = new Common(COMMON_BUILDER);
    private static final TomlConfig COMMON_FILE = COMMON_BUILDER.build("sweettreats-common.toml");
    private static final TomlConfig.Builder CLIENT_BUILDER = new TomlConfig.Builder();
    public static final Client CLIENT = new Client(CLIENT_BUILDER);
    private static final TomlConfig CLIENT_FILE = CLIENT_BUILDER.build("sweettreats-client.toml");
    private static boolean clientLoaded;

    private SweetTreatsConfig() {
    }

    /** Gameplay rules; read at mod start. */
    public static void loadCommon() {
        COMMON_FILE.load(Services.PLATFORM.configDir());
    }

    /** Visual options; read when the client starts. */
    public static void loadClient() {
        CLIENT_FILE.load(Services.PLATFORM.configDir());
        clientLoaded = true;
    }

    public static final class Common {
        // Food
        public final TomlConfig.BooleanValue foodEffects;
        public final TomlConfig.DoubleValue effectDurationMultiplier;
        // Machines
        public final TomlConfig.DoubleValue ovenSpeedMultiplier;
        public final TomlConfig.BooleanValue ovenRequiresFuel;
        public final TomlConfig.DoubleValue mixingSpeedMultiplier;
        // Recipe book
        public final TomlConfig.BooleanValue giveRecipeBookOnFirstJoin;
        public final TomlConfig.BooleanValue discoveryMessages;
        public final TomlConfig.BooleanValue hideUndiscoveredIngredients;
        // World
        public final TomlConfig.BooleanValue grassSeedDrops;
        public final TomlConfig.BooleanValue cherryLeafDrops;
        public final TomlConfig.DoubleValue dropChanceMultiplier;
        public final TomlConfig.IntValue fruitTreeGrowthChance;
        public final TomlConfig.BooleanValue wanderingTraderTrades;

        Common(TomlConfig.Builder b) {
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
        public final TomlConfig.BooleanValue cookingParticles;
        public final TomlConfig.BooleanValue cookingAnimations;

        Client(TomlConfig.Builder b) {
            b.comment("Visual settings").push("visuals");
            cookingParticles = b.comment("Show smoke/flour particles while the oven and mixing bowl are working.")
                    .define("cookingParticles", true);
            cookingAnimations = b.comment("Animate ingredients swirling in the mixing bowl and treats spinning in cake displays.")
                    .define("cookingAnimations", true);
            b.pop();
        }
    }

    // ---- Accessors (defaults until the files are read) ----

    public static boolean foodEffects() {
        return COMMON.foodEffects.get();
    }

    public static double effectDurationMultiplier() {
        return COMMON.effectDurationMultiplier.get();
    }

    public static double ovenSpeedMultiplier() {
        return COMMON.ovenSpeedMultiplier.get();
    }

    public static boolean ovenRequiresFuel() {
        return COMMON.ovenRequiresFuel.get();
    }

    public static double mixingSpeedMultiplier() {
        return COMMON.mixingSpeedMultiplier.get();
    }

    public static boolean giveRecipeBookOnFirstJoin() {
        return COMMON.giveRecipeBookOnFirstJoin.get();
    }

    public static boolean discoveryMessages() {
        return COMMON.discoveryMessages.get();
    }

    public static boolean hideUndiscoveredIngredients() {
        return COMMON.hideUndiscoveredIngredients.get();
    }

    public static boolean grassSeedDrops() {
        return COMMON.grassSeedDrops.get();
    }

    public static boolean cherryLeafDrops() {
        return COMMON.cherryLeafDrops.get();
    }

    public static double dropChanceMultiplier() {
        return COMMON.dropChanceMultiplier.get();
    }

    public static int fruitTreeGrowthChance() {
        return COMMON.fruitTreeGrowthChance.get();
    }

    public static boolean wanderingTraderTrades() {
        return COMMON.wanderingTraderTrades.get();
    }

    public static boolean cookingParticles() {
        return !clientLoaded || CLIENT.cookingParticles.get();
    }

    public static boolean cookingAnimations() {
        return !clientLoaded || CLIENT.cookingAnimations.get();
    }
}
