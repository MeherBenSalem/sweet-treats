package com.sugarplumstudios.sweettreats.gametest;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.block.BakeryOvenBlock;
import com.sugarplumstudios.sweettreats.block.CakeTreatBlock;
import com.sugarplumstudios.sweettreats.block.FruitTreeBlock;
import com.sugarplumstudios.sweettreats.blockentity.BakeryOvenBlockEntity;
import com.sugarplumstudios.sweettreats.blockentity.DisplayBlockEntity;
import com.sugarplumstudios.sweettreats.blockentity.MixingBowlBlockEntity;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.item.TreatItem;
import com.sugarplumstudios.sweettreats.menu.DecorationStationMenu;
import com.sugarplumstudios.sweettreats.recipe.DecoratingRecipe;
import com.sugarplumstudios.sweettreats.recipe.RecipeUtil;
import com.sugarplumstudios.sweettreats.registry.ModBlocks;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import com.sugarplumstudios.sweettreats.platform.RegistryEntry;

import java.util.List;

/**
 * In-game tests for Sweet Treats. Run them headless with {@code gradlew runGameTestServer}.
 */
@GameTestHolder(SweetTreats.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SweetTreatsGameTests {
    private static final String EMPTY = "empty";

    private SweetTreatsGameTests() {
    }

    // ---------------------------------------------------------------- registration & data

    @GameTest(template = EMPTY)
    public static void recipesLoad(GameTestHelper helper) {
        RecipeManager manager = helper.getLevel().getRecipeManager();
        int baking = manager.getAllRecipesFor(ModRecipes.BAKING.get()).size();
        int mixing = manager.getAllRecipesFor(ModRecipes.MIXING.get()).size();
        int decorating = manager.getAllRecipesFor(ModRecipes.DECORATING.get()).size();
        long crafting = manager.getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(r -> r.getId().getNamespace().equals(SweetTreats.MOD_ID)).count();
        helper.assertTrue(baking == 18, "expected 18 baking recipes, found " + baking);
        helper.assertTrue(mixing == 17, "expected 17 mixing recipes, found " + mixing);
        helper.assertTrue(decorating == 9, "expected 9 decorating recipes, found " + decorating);
        helper.assertTrue(crafting == 20, "expected 20 crafting recipes, found " + crafting);
        for (Recipe<?> recipe : RecipeDiscovery.trackedRecipes(manager)) {
            helper.assertFalse(recipe.getResultItem(helper.getLevel().registryAccess()).isEmpty(),
                    "recipe " + recipe.getId() + " has an empty result");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void everyTreatIsFood(GameTestHelper helper) {
        List<RegistryEntry<Item>> treats = List.of(
                ModItems.PINK_VANILLA_CUPCAKE, ModItems.CHOCOLATE_CUPCAKE, ModItems.LAVENDER_MACARON, ModItems.STRAWBERRY_MACARON,
                ModItems.HEART_COOKIE, ModItems.CINNAMON_ROLL, ModItems.MINI_DONUTS, ModItems.PEACH_PASTRY,
                ModItems.STRAWBERRY_MILK, ModItems.VANILLA_MILKSHAKE, ModItems.LAVENDER_TEA, ModItems.PEACH_TEA,
                ModItems.BUBBLE_TEA, ModItems.PINK_LEMONADE, ModItems.HOT_CHOCOLATE, ModItems.ROSE_TEA,
                ModItems.HEART_CANDY, ModItems.STRAWBERRY_LOLLIPOP, ModItems.MARSHMALLOW_CLOUD, ModItems.RAINBOW_COOKIE,
                ModItems.HONEY_CUPCAKE, ModItems.MOON_COOKIE, ModItems.STRAWBERRY, ModItems.PEACH, ModItems.CHERRY);
        for (RegistryEntry<Item> treat : treats) {
            Item item = treat.get();
            helper.assertTrue(item.isEdible() && item.getFoodProperties() != null && item.getFoodProperties().getNutrition() > 0,
                    treat.id() + " should be edible");
        }
        helper.assertTrue(((TreatItem) ModItems.LAVENDER_TEA.get()).isDrink(), "lavender tea should be a drink");
        helper.succeed();
    }

    // ---------------------------------------------------------------- machines

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void ovenBakesCookiesWithFuel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.BAKERY_OVEN.get());
        BakeryOvenBlockEntity oven = (BakeryOvenBlockEntity) helper.getBlockEntity(pos);
        oven.setItem(0, new ItemStack(ModItems.COOKIE_DOUGH.get(), 2));
        oven.setItem(BakeryOvenBlockEntity.FUEL_SLOT, new ItemStack(Items.COAL));
        helper.runAfterDelay(5, () -> helper.assertBlockProperty(pos, BakeryOvenBlock.LIT, true));
        helper.succeedWhen(() -> {
            ItemStack out = oven.getItem(BakeryOvenBlockEntity.OUTPUT_SLOT);
            helper.assertTrue(out.is(ModItems.SUGAR_COOKIE.get()) && out.getCount() == 8, "waiting for 8 sugar cookies, have " + out);
            helper.assertTrue(oven.getItem(0).isEmpty(), "dough should be used up");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void ovenNeedsFuel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.BAKERY_OVEN.get());
        BakeryOvenBlockEntity oven = (BakeryOvenBlockEntity) helper.getBlockEntity(pos);
        oven.setItem(0, new ItemStack(ModItems.COOKIE_DOUGH.get()));
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(oven.getItem(BakeryOvenBlockEntity.OUTPUT_SLOT).isEmpty(), "baked without fuel");
            helper.assertTrue(oven.getProgress() == 0, "progress without fuel");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void mixingBowlUsesStacks(GameTestHelper helper) {
        // Icing = 2 sugar + cream; both sugars come from a single stack.
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.MIXING_BOWL.get());
        MixingBowlBlockEntity bowl = (MixingBowlBlockEntity) helper.getBlockEntity(pos);
        bowl.setItem(0, new ItemStack(Items.SUGAR, 2));
        bowl.setItem(3, new ItemStack(ModItems.CREAM.get(), 1));
        helper.succeedWhen(() -> {
            ItemStack out = bowl.getItem(MixingBowlBlockEntity.OUTPUT_SLOT);
            helper.assertTrue(out.is(ModItems.ICING.get()) && out.getCount() == 2, "waiting for icing, have " + out);
            helper.assertTrue(bowl.getItem(0).isEmpty() && bowl.getItem(3).isEmpty(),
                    "ingredients should be used up");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void mixingBowlDrinkNeedsBottle(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.MIXING_BOWL.get());
        MixingBowlBlockEntity bowl = (MixingBowlBlockEntity) helper.getBlockEntity(pos);
        bowl.setItem(0, new ItemStack(ModItems.CREAM.get()));
        bowl.setItem(1, new ItemStack(ModItems.STRAWBERRY.get(), 2));
        helper.runAfterDelay(80, () -> {
            helper.assertTrue(bowl.getItem(MixingBowlBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "strawberry milk must not be made without a bottle");
            bowl.setItem(MixingBowlBlockEntity.CONTAINER_SLOT, new ItemStack(Items.GLASS_BOTTLE, 3));
        });
        helper.succeedWhen(() -> {
            ItemStack out = bowl.getItem(MixingBowlBlockEntity.OUTPUT_SLOT);
            helper.assertTrue(out.is(ModItems.STRAWBERRY_MILK.get()), "waiting for strawberry milk, have " + out);
            helper.assertTrue(bowl.getItem(MixingBowlBlockEntity.CONTAINER_SLOT).getCount() == 2,
                    "one bottle should be used");
        });
    }

    @GameTest(template = EMPTY)
    public static void decoratingMatchesAndReturnsBottles(GameTestHelper helper) {
        // Recipe lookup
        SimpleContainer inputs = new SimpleContainer(new ItemStack(ModItems.PLAIN_CUPCAKE.get()),
                new ItemStack(ModItems.SUGAR_FLOWERS.get()), new ItemStack(ModItems.PINK_ICING.get()));
        DecoratingRecipe recipe = RecipeUtil.findBest(helper.getLevel(), ModRecipes.DECORATING.get(), inputs);
        helper.assertTrue(recipe != null && recipe.getResult().is(ModItems.PINK_VANILLA_CUPCAKE.get()),
                "plain cupcake + sugar flowers + pink icing should make a pink vanilla cupcake");

        // Full menu flow: honey cupcake gives the glass bottle back.
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.DECORATION_STATION.get());
        Player player = helper.makeMockPlayer();
        DecorationStationMenu menu = new DecorationStationMenu(1, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(pos)));
        menu.slots.get(0).set(new ItemStack(ModItems.PLAIN_CUPCAKE.get(), 2));
        menu.slots.get(1).set(new ItemStack(Items.HONEY_BOTTLE, 1));
        helper.assertTrue(menu.slots.get(DecorationStationMenu.RESULT_SLOT).getItem().is(ModItems.HONEY_CUPCAKE.get()),
                "result slot should show a honey cupcake");
        menu.clicked(DecorationStationMenu.RESULT_SLOT, 0, ClickType.QUICK_MOVE, player);
        helper.assertTrue(player.getInventory().countItem(ModItems.HONEY_CUPCAKE.get()) == 1, "player should get one honey cupcake");
        helper.assertTrue(menu.slots.get(0).getItem().getCount() == 1, "one cupcake base should remain");
        helper.assertTrue(menu.slots.get(1).getItem().is(Items.GLASS_BOTTLE), "honey bottle should leave a glass bottle");
        helper.succeed();
    }

    // ---------------------------------------------------------------- blocks

    @GameTest(template = EMPTY)
    public static void cakeIsEatenSliceBySlice(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, ModBlocks.STRAWBERRY_CAKE.get());
        Player player = helper.makeMockPlayer();
        player.getFoodData().setFoodLevel(10);
        BlockPos abs = helper.absolutePos(pos);
        BlockState state = helper.getBlockState(pos);
        state.use(helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        helper.assertBlockProperty(pos, CakeTreatBlock.BITES, 1);
        helper.assertTrue(player.getFoodData().getFoodLevel() == 13, "a slice should restore 3 food, now " + player.getFoodData().getFoodLevel());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void matureCropsDropProduce(GameTestHelper helper) {
        BlockState mature = ((net.minecraft.world.level.block.CropBlock) ModBlocks.STRAWBERRY_CROP.get()).getStateForAge(7);
        List<ItemStack> drops = Block.getDrops(mature, helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), null);
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(ModItems.STRAWBERRY.get()) && s.getCount() >= 2), "mature strawberries should drop berries: " + drops);
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(ModItems.STRAWBERRY_SEEDS.get())), "mature strawberries should drop seeds: " + drops);
        List<ItemStack> young = Block.getDrops(ModBlocks.LAVENDER_CROP.get().defaultBlockState(), helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 1)), null);
        helper.assertTrue(young.size() == 1 && young.get(0).is(ModItems.LAVENDER_SEEDS.get()), "young lavender should only drop seeds: " + young);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void fruitTreeHarvest(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.below(), Blocks.GRASS_BLOCK);
        helper.setBlock(pos, ModBlocks.PEACH_TREE.get().defaultBlockState().setValue(FruitTreeBlock.AGE, 3));
        Player player = helper.makeMockPlayer();
        BlockPos abs = helper.absolutePos(pos);
        helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        helper.assertBlockProperty(pos, FruitTreeBlock.AGE, 1);
        helper.assertItemEntityPresent(ModItems.PEACH.get(), pos, 2.0D);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void displaysHoldTreats(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.CUPCAKE_STAND.get());
        DisplayBlockEntity stand = (DisplayBlockEntity) helper.getBlockEntity(pos);
        ItemStack cupcakes = new ItemStack(ModItems.HONEY_CUPCAKE.get(), 5);
        int inserted = 0;
        for (int i = 0; i < 4; i++) {
            inserted += stand.insert(cupcakes.copyWithCount(1));
        }
        helper.assertTrue(inserted == 3, "cupcake stand holds 3 treats, inserted " + inserted);
        helper.assertTrue(stand.takeLast(false).is(ModItems.HONEY_CUPCAKE.get()), "should take a cupcake back");

        BlockPos jarPos = new BlockPos(1, 1, 1);
        helper.setBlock(jarPos, ModBlocks.GLASS_JAR.get());
        DisplayBlockEntity jar = (DisplayBlockEntity) helper.getBlockEntity(jarPos);
        helper.assertTrue(jar.insert(new ItemStack(ModItems.HEART_CANDY.get(), 40)) == 40, "jar takes a whole stack");
        helper.assertTrue(jar.takeLast(true).getCount() == 40, "sneak-taking empties the jar");
        helper.assertTrue(jar.canInsert(new ItemStack(ModItems.FLOUR.get())), "jars store ingredients too");

        BlockPos counterPos = new BlockPos(3, 1, 3);
        helper.setBlock(counterPos, ModBlocks.BAKERY_COUNTER.get());
        DisplayBlockEntity counter = (DisplayBlockEntity) helper.getBlockEntity(counterPos);
        helper.assertFalse(counter.canInsert(new ItemStack(Items.STONE)), "counters must not swallow building blocks");
        helper.assertTrue(counter.canInsert(new ItemStack(ModItems.STRAWBERRY_CAKE.get())), "counters show placeable cakes");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void discoveryIsTracked(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        ResourceLocation id = SweetTreats.id("baking/sugar_cookie");
        helper.assertFalse(RecipeDiscovery.isDiscovered(player, id), "nothing discovered yet");
        helper.assertTrue(RecipeDiscovery.discover(player, id), "first discovery is new");
        helper.assertFalse(RecipeDiscovery.discover(player, id), "second discovery is not new");
        helper.assertTrue(RecipeDiscovery.getDiscovered(player).contains(id), "discovery stored");
        helper.succeed();
    }
}
