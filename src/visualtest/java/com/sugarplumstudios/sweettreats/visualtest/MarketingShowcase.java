package com.sugarplumstudios.sweettreats.visualtest;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.block.DecorBlock;
import com.sugarplumstudios.sweettreats.block.FruitTreeBlock;
import com.sugarplumstudios.sweettreats.block.PieTreatBlock;
import com.sugarplumstudios.sweettreats.blockentity.BakeryOvenBlockEntity;
import com.sugarplumstudios.sweettreats.blockentity.DisplayBlockEntity;
import com.sugarplumstudios.sweettreats.blockentity.MixingBowlBlockEntity;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.menu.DecorationStationMenu;
import com.sugarplumstudios.sweettreats.network.ModNetwork;
import com.sugarplumstudios.sweettreats.registry.ModBlocks;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Development-only marketing showcase (not part of the mod jar). Enabled with -Dsweettreats.showcase=true
 * ({@code ./gradlew runShowcaseClient}): creates a flat creative world, builds a little bakery shop with a kitchen, a
 * display counter, café tables and a garden out front, takes 1920x1080 screenshots of it and of every screen into
 * run-showcase/screenshots, then quits. Every Sweet Treats block in the pictures is placed and filled by the mod's own code.
 */
@Mod.EventBusSubscriber(modid = SweetTreats.MOD_ID, value = Dist.CLIENT)
public final class MarketingShowcase {
    private static final boolean ENABLED = Boolean.getBoolean("sweettreats.showcase");
    private static final double EYE = 1.62D;
    private static final TreeMap<Integer, Runnable> STEPS = new TreeMap<>();
    private static int menuTicks;
    private static boolean worldRequested;
    private static int worldTicks;
    private static BlockPos origin = BlockPos.ZERO;

    private MarketingShowcase() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        mc.getToasts().clear();
        if (mc.level == null) {
            if (!worldRequested && ++menuTicks > 60) {
                worldRequested = true;
                mc.options.tutorialStep = TutorialSteps.NONE;
                createWorld(mc);
            }
            return;
        }
        if (mc.player == null || !mc.hasSingleplayerServer()) {
            return;
        }
        if (worldTicks == 0) {
            mc.getTutorial().setStep(TutorialSteps.NONE);
            schedule();
        }
        worldTicks++;
        Runnable step = STEPS.remove(worldTicks);
        if (step != null) {
            try {
                step.run();
            } catch (Throwable t) {
                SweetTreats.LOGGER.error("[showcase] step at tick {} failed", worldTicks, t);
            }
        }
    }

    private static void createWorld(Minecraft mc) {
        SweetTreats.LOGGER.info("[showcase] creating world");
        LevelSettings settings = new LevelSettings("SweetTreatsShowcase", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("SweetTreatsShowcase" + System.currentTimeMillis(), settings,
                new WorldOptions(20261123L, false, false),
                access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
    }

    private static void onServer(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (player != null) {
                action.accept(player);
            }
        });
    }

    private static void shot(String name) {
        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(),
                message -> SweetTreats.LOGGER.info("[showcase] screenshot {}: {}", name, message.getString()));
    }

    /** Moves the camera so the eye sits at (x, y, z) relative to the origin and looks at (tx, ty, tz). */
    private static void camera(double x, double y, double z, double tx, double ty, double tz) {
        onServer(player -> {
            double dx = tx - x;
            double dy = ty - y;
            double dz = tz - z;
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(player.serverLevel(), origin.getX() + x, origin.getY() + y - EYE, origin.getZ() + z, yaw, pitch);
        });
    }

    private static void schedule() {
        Minecraft mc = Minecraft.getInstance();
        STEPS.put(40, () -> onServer(MarketingShowcase::build));
        STEPS.put(45, () -> {
            mc.options.hideGui = true;
            mc.options.fov().set(75);
            mc.options.bobView().set(false);
        });

        // Coordinates below are relative to the origin, in blocks: the shop spans x -6..6, z 0..10, its door is at x 0, z 0.
        STEPS.put(200, () -> camera(5.3, 3.7, 0.9, -1.5, 0.5, 7.4));
        STEPS.put(260, () -> shot("bakery"));
        STEPS.put(265, () -> camera(0.5, 2.1, 2.4, 0.5, 1.2, 5.5));
        STEPS.put(320, () -> shot("counter"));
        STEPS.put(325, () -> camera(0.6, 2.5, 6.9, -3.4, 0.8, 9.4));
        STEPS.put(380, () -> shot("kitchen"));
        STEPS.put(385, () -> camera(-1.6, 2.1, 3.9, -4.3, 1.0, 2.0));
        STEPS.put(440, () -> shot("tea-table"));
        STEPS.put(445, () -> camera(0.5, 4.2, -14.5, 0.5, 2.0, 0.0));
        STEPS.put(520, () -> shot("storefront"));
        STEPS.put(525, () -> camera(-0.6, 2.4, -2.2, -4.5, 0.4, -7.0));
        STEPS.put(580, () -> shot("garden"));
        STEPS.put(585, () -> camera(0.6, 2.2, -1.8, 4.6, 0.6, -6.0));
        STEPS.put(640, () -> shot("orchard"));

        // The screens, opened from inside the kitchen so every menu stays in reach.
        STEPS.put(645, () -> camera(-2.5, 1.7, 7.4, -2.5, 1.0, 9.5));
        STEPS.put(660, () -> mc.options.hideGui = false);
        STEPS.put(665, () -> onServer(player -> {
            if (player.level().getBlockEntity(origin.offset(-5, 0, 9)) instanceof BakeryOvenBlockEntity oven) {
                NetworkHooks.openScreen(player, oven, oven.getBlockPos());
            }
        }));
        STEPS.put(700, () -> shot("gui-oven"));
        STEPS.put(705, () -> onServer(ServerPlayer::closeContainer));
        STEPS.put(715, () -> onServer(player -> {
            if (player.level().getBlockEntity(origin.offset(-3, 1, 9)) instanceof MixingBowlBlockEntity bowl) {
                NetworkHooks.openScreen(player, bowl, bowl.getBlockPos());
            }
        }));
        STEPS.put(750, () -> shot("gui-mixing-bowl"));
        STEPS.put(755, () -> onServer(ServerPlayer::closeContainer));
        STEPS.put(765, () -> onServer(player -> {
            BlockPos station = origin.offset(-2, 0, 9);
            player.openMenu(player.level().getBlockState(station).getMenuProvider(player.level(), station));
            if (player.containerMenu instanceof DecorationStationMenu menu) {
                menu.slots.get(0).set(new ItemStack(ModItems.PLAIN_CUPCAKE.get(), 6));
                menu.slots.get(1).set(new ItemStack(ModItems.PINK_ICING.get(), 4));
                menu.slots.get(2).set(new ItemStack(ModItems.SUGAR_FLOWERS.get(), 4));
                menu.broadcastChanges();
            }
        }));
        STEPS.put(800, () -> shot("gui-decoration-station"));
        STEPS.put(805, () -> onServer(ServerPlayer::closeContainer));
        STEPS.put(815, () -> onServer(player -> {
            for (Item item : List.of(ModItems.SUGAR_COOKIE.get(), ModItems.SPONGE_CAKE.get(), ModItems.CINNAMON_ROLL.get(),
                    ModItems.CHERRY_PIE.get(), ModItems.HEART_CANDY.get(), ModItems.PLAIN_CUPCAKE.get(), ModItems.LAVENDER_TEA.get(),
                    ModItems.PEACH_PASTRY.get(), ModItems.MINI_DONUTS.get(), ModItems.HOT_CHOCOLATE.get(), Items.COOKIE,
                    ModItems.PINK_VANILLA_CUPCAKE.get(), ModItems.STRAWBERRY_CAKE.get(), ModItems.RAINBOW_COOKIE.get(),
                    ModItems.LAVENDER_MACARON.get(), ModItems.MACARON_SHELLS.get(), ModItems.TEACUP.get(), Items.BREAD,
                    ModItems.CHOCOLATE.get(), ModItems.SUGAR_FLOWERS.get(), ModItems.ICING.get(), ModItems.PINK_ICING.get(),
                    ModItems.CHOCOLATE_FROSTING.get(), ModItems.TAPIOCA_PEARLS.get(), ModItems.CAKE_BATTER.get(),
                    ModItems.COOKIE_DOUGH.get(), ModItems.PASTRY_DOUGH.get(), ModItems.MACARON_BATTER.get(),
                    ModItems.STRAWBERRY_MILK.get(), ModItems.BUBBLE_TEA.get(), ModItems.MARSHMALLOW_CLOUD.get())) {
                RecipeDiscovery.discoverByResult(player, new ItemStack(item));
            }
            ModNetwork.openRecipeBook(player);
        }));
        STEPS.put(835, () -> clickRecipeBookCell(7)); // Cinnamon Roll
        STEPS.put(850, () -> shot("book-oven"));
        STEPS.put(855, () -> clickRecipeBook(2, -15)); // Decorating tab
        STEPS.put(860, () -> clickRecipeBookCell(1));
        STEPS.put(885, () -> shot("book-decorating"));
        STEPS.put(890, () -> clickRecipeBook(1, -15)); // Mixing Bowl tab
        STEPS.put(895, () -> clickRecipeBookCell(14)); // Bubble Tea
        STEPS.put(915, () -> shot("book-mixing"));
        STEPS.put(920, () -> mc.setScreen(null));
        STEPS.put(930, () -> {
            SweetTreats.LOGGER.info("[showcase] done, stopping client");
            mc.stop();
        });
    }

    private static void clickRecipeBook(int tab, int dy) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            int left = (mc.screen.width - 256) / 2;
            int top = (mc.screen.height - 180) / 2 + 10;
            mc.screen.mouseClicked(left + 14 + tab * 30 + 14, top + dy, 0);
        }
    }

    private static void clickRecipeBookCell(int index) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            int left = (mc.screen.width - 256) / 2;
            int top = (mc.screen.height - 180) / 2 + 10;
            mc.screen.mouseClicked(left + 17 + (index % 5) * 20 + 10, top + 28 + (index / 5) * 20 + 10, 0);
        }
    }

    // ------------------------------------------------------------------ the bakery

    private static void build(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        origin = player.blockPosition();
        level.setDayTime(5000);
        level.setWeatherParameters(12000, 0, false, false);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, level.getServer());

        // clear the lot and lay fresh grass
        fill(level, -14, 0, -20, 14, 14, 14, Blocks.AIR.defaultBlockState());
        fill(level, -14, -1, -20, 14, -1, 14, Blocks.GRASS_BLOCK.defaultBlockState());

        // floor: pink and white tiles
        for (int x = -6; x <= 6; x++) {
            for (int z = 0; z <= 10; z++) {
                set(level, x, -1, z, ((x + z) & 1) == 0 ? Blocks.PINK_CONCRETE.defaultBlockState() : Blocks.WHITE_CONCRETE.defaultBlockState());
            }
        }
        // walls: a pink base course, white walls, cherry log corners and pink glass windows
        for (int y = 0; y <= 4; y++) {
            for (int x = -6; x <= 6; x++) {
                wall(level, x, y, 0);
                wall(level, x, y, 10);
            }
            for (int z = 1; z <= 9; z++) {
                wall(level, -6, y, z);
                wall(level, 6, y, z);
            }
        }
        for (int x : new int[]{-4, -3, -2, 2, 3, 4}) {
            set(level, x, 1, 0, Blocks.PINK_STAINED_GLASS.defaultBlockState());
            set(level, x, 2, 0, Blocks.PINK_STAINED_GLASS.defaultBlockState());
        }
        for (int z : new int[]{3, 4, 6, 7}) {
            set(level, -6, 2, z, Blocks.PINK_STAINED_GLASS.defaultBlockState());
            set(level, 6, 2, z, Blocks.PINK_STAINED_GLASS.defaultBlockState());
        }
        set(level, 0, 0, 0, Blocks.AIR.defaultBlockState());
        set(level, 0, 1, 0, Blocks.AIR.defaultBlockState());
        // gable roof of cherry stairs, closed with white gable ends
        for (int i = 0; i <= 6; i++) {
            int y = 5 + i;
            for (int z = -1; z <= 11; z++) {
                set(level, -7 + i, y, z, Blocks.CHERRY_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
                set(level, 7 - i, y, z, Blocks.CHERRY_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
            }
            for (int x = -6 + i; x <= 6 - i; x++) {
                set(level, x, y, 0, Blocks.WHITE_CONCRETE.defaultBlockState());
                set(level, x, y, 10, Blocks.WHITE_CONCRETE.defaultBlockState());
            }
        }
        for (int z = -1; z <= 11; z++) {
            set(level, 0, 12, z, Blocks.CHERRY_SLAB.defaultBlockState());
        }
        set(level, 0, 7, 0, Blocks.PINK_STAINED_GLASS.defaultBlockState());
        // invisible light blocks so the inside is evenly lit
        for (int x = -4; x <= 4; x += 4) {
            for (int z = 2; z <= 8; z += 3) {
                set(level, x, 4, z, Blocks.LIGHT.defaultBlockState());
            }
        }

        // the kitchen along the back wall
        oven(level, -5, 9);
        oven(level, -4, 9);
        set(level, -3, 0, 9, ModBlocks.BAKERY_COUNTER.get().defaultBlockState());
        set(level, -3, 1, 9, ModBlocks.MIXING_BOWL.get().defaultBlockState());
        if (level.getBlockEntity(at(-3, 1, 9)) instanceof MixingBowlBlockEntity bowl) {
            bowl.getItems().setStackInSlot(0, new ItemStack(ModItems.CREAM.get(), 24));
            bowl.getItems().setStackInSlot(1, new ItemStack(ModItems.TAPIOCA_PEARLS.get(), 24));
            bowl.getItems().setStackInSlot(2, new ItemStack(Items.SUGAR, 24));
            bowl.getItems().setStackInSlot(MixingBowlBlockEntity.CONTAINER_SLOT, new ItemStack(Items.GLASS_BOTTLE, 16));
        }
        set(level, -2, 0, 9, ModBlocks.DECORATION_STATION.get().defaultBlockState());
        counter(level, -1, 9, new ItemStack(ModItems.CINNAMON_ROLL.get()));
        set(level, 0, 0, 9, ModBlocks.BAKERY_COUNTER.get().defaultBlockState());
        set(level, 0, 1, 9, ModBlocks.TEA_SET.get().defaultBlockState().setValue(DecorBlock.FACING, Direction.NORTH));
        counter(level, 1, 9, new ItemStack(ModItems.PEACH_PASTRY.get()));
        set(level, 2, 0, 9, ModBlocks.DECORATIVE_MENU.get().defaultBlockState().setValue(DecorBlock.FACING, Direction.NORTH));
        set(level, 3, 0, 9, ModBlocks.BAKERY_COUNTER.get().defaultBlockState());
        display(level, 3, 1, 9, ModBlocks.GLASS_JAR.get(), new ItemStack(ModItems.STRAWBERRY_LOLLIPOP.get(), 48));
        counter(level, 4, 9, new ItemStack(ModItems.MINI_DONUTS.get()));
        oven(level, 5, 9);
        for (int x = -5; x <= 5; x += 2) {
            set(level, x, 2, 9, ModBlocks.BAKERY_SIGN.get().defaultBlockState().setValue(DecorBlock.FACING, Direction.NORTH));
        }

        // the display counter across the middle
        for (int x = -4; x <= 4; x++) {
            set(level, x, 0, 5, ModBlocks.BAKERY_COUNTER.get().defaultBlockState());
        }
        display(level, -4, 1, 5, ModBlocks.CAKE_DISPLAY.get(), new ItemStack(ModItems.STRAWBERRY_CAKE.get()));
        counterTop(level, -3, 5, new ItemStack(ModItems.PINK_VANILLA_CUPCAKE.get()));
        display(level, -2, 1, 5, ModBlocks.CUPCAKE_STAND.get(), new ItemStack(ModItems.CHOCOLATE_CUPCAKE.get()),
                new ItemStack(ModItems.HONEY_CUPCAKE.get()), new ItemStack(ModItems.PINK_VANILLA_CUPCAKE.get()));
        display(level, -1, 1, 5, ModBlocks.GLASS_JAR.get(), new ItemStack(ModItems.HEART_CANDY.get(), 48));
        counterTop(level, 0, 5, new ItemStack(ModItems.HEART_COOKIE.get()));
        display(level, 1, 1, 5, ModBlocks.DESSERT_PLATE.get(), new ItemStack(ModItems.LAVENDER_MACARON.get()));
        display(level, 2, 1, 5, ModBlocks.GLASS_JAR.get(), new ItemStack(ModItems.RAINBOW_COOKIE.get(), 48));
        display(level, 3, 1, 5, ModBlocks.CUPCAKE_STAND.get(), new ItemStack(ModItems.STRAWBERRY_MACARON.get()),
                new ItemStack(ModItems.LAVENDER_MACARON.get()), new ItemStack(ModItems.MOON_COOKIE.get()));
        display(level, 4, 1, 5, ModBlocks.CAKE_DISPLAY.get(), new ItemStack(ModItems.CHERRY_PIE.get()));

        // café tables by the windows: a tea set, a strawberry cake and a cherry pie with a slice gone
        table(level, -4, 2, ModBlocks.TEA_SET.get().defaultBlockState().setValue(DecorBlock.FACING, Direction.SOUTH));
        chair(level, -5, 2, Direction.WEST);
        chair(level, -3, 2, Direction.EAST);
        table(level, 4, 2, ModBlocks.STRAWBERRY_CAKE.get().defaultBlockState());
        chair(level, 3, 2, Direction.WEST);
        chair(level, 5, 2, Direction.EAST);
        table(level, -4, 7, ModBlocks.CHERRY_PIE.get().defaultBlockState().setValue(PieTreatBlock.BITES, 1));
        table(level, 4, 7, ModBlocks.DESSERT_PLATE.get().defaultBlockState());
        if (level.getBlockEntity(at(4, 1, 7)) instanceof DisplayBlockEntity plate) {
            plate.insert(new ItemStack(ModItems.CINNAMON_ROLL.get()));
        }
        set(level, -5, 0, 1, Blocks.POTTED_PINK_TULIP.defaultBlockState());
        set(level, 5, 0, 1, Blocks.POTTED_ALLIUM.defaultBlockState());

        // out front: a sign over the door, hedges under the windows, a path, crops and fruit trees
        set(level, 0, 2, -1, ModBlocks.BAKERY_SIGN.get().defaultBlockState().setValue(DecorBlock.FACING, Direction.NORTH));
        for (int x : new int[]{-5, -4, -3, -2, 2, 3, 4, 5}) {
            set(level, x, 0, -1, Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
        }
        for (int z = -16; z <= -1; z++) {
            set(level, 0, -1, z, Blocks.DIRT_PATH.defaultBlockState());
        }
        crops(level, -3, (CropBlock) ModBlocks.STRAWBERRY_CROP.get());
        crops(level, -4, (CropBlock) ModBlocks.STRAWBERRY_CROP.get());
        crops(level, -6, (CropBlock) ModBlocks.VANILLA_CROP.get());
        crops(level, -7, (CropBlock) ModBlocks.VANILLA_CROP.get());
        crops(level, -9, (CropBlock) ModBlocks.LAVENDER_CROP.get());
        crops(level, -10, (CropBlock) ModBlocks.LAVENDER_CROP.get());
        for (int x = -6; x <= -2; x++) {
            set(level, x, -1, -5, Blocks.WATER.defaultBlockState());
            set(level, x, -1, -8, Blocks.WATER.defaultBlockState());
        }
        int[][] trees = {{2, -4}, {4, -4}, {6, -4}, {2, -7}, {4, -7}, {6, -7}, {3, -10}, {5, -10}};
        for (int i = 0; i < trees.length; i++) {
            Block tree = i % 2 == 0 ? ModBlocks.PEACH_TREE.get() : ModBlocks.CHERRY_TREE.get();
            set(level, trees[i][0], 0, trees[i][1], tree.defaultBlockState().setValue(FruitTreeBlock.AGE, i == 6 ? 2 : 3));
        }
        Block[] wild = {ModBlocks.WILD_STRAWBERRIES.get(), ModBlocks.WILD_LAVENDER.get(), ModBlocks.WILD_VANILLA.get()};
        int[][] wildSpots = {{-1, -3}, {1, -6}, {-1, -9}, {1, -12}, {-1, -14}, {1, -2}, {-8, -4}, {-8, -9}, {8, -5}, {8, -9}};
        for (int i = 0; i < wildSpots.length; i++) {
            set(level, wildSpots[i][0], 0, wildSpots[i][1], wild[i % 3].defaultBlockState());
        }
        BlockState[] flowers = {Blocks.PINK_TULIP.defaultBlockState(), Blocks.ALLIUM.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(),
                Blocks.AZURE_BLUET.defaultBlockState(), Blocks.LILY_OF_THE_VALLEY.defaultBlockState()};
        int[][] flowerSpots = {{-1, -5}, {1, -4}, {-1, -7}, {1, -9}, {-1, -11}, {1, -14}, {-8, -2}, {8, -2}, {-8, -7}, {8, -7},
                {-8, -12}, {8, -12}, {-3, -13}, {3, -13}, {-5, -15}, {5, -15}};
        for (int i = 0; i < flowerSpots.length; i++) {
            set(level, flowerSpots[i][0], 0, flowerSpots[i][1], flowers[i % flowers.length]);
        }
        SweetTreats.LOGGER.info("[showcase] bakery built at {}", origin);
    }

    private static BlockPos at(int x, int y, int z) {
        return origin.offset(x, y, z);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(at(x, y, z), state, Block.UPDATE_ALL);
    }

    private static void fill(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(at(x0, y0, z0), at(x1, y1, z1))) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void wall(ServerLevel level, int x, int y, int z) {
        boolean corner = Math.abs(x) == 6 && (z == 0 || z == 10);
        BlockState state = corner ? Blocks.CHERRY_LOG.defaultBlockState()
                : y == 0 ? Blocks.PINK_CONCRETE.defaultBlockState()
                : y == 4 ? Blocks.CHERRY_PLANKS.defaultBlockState()
                : Blocks.WHITE_CONCRETE.defaultBlockState();
        set(level, x, y, z, state);
    }

    private static void oven(ServerLevel level, int x, int z) {
        set(level, x, 0, z, ModBlocks.BAKERY_OVEN.get().defaultBlockState());
        if (level.getBlockEntity(at(x, 0, z)) instanceof BakeryOvenBlockEntity oven) {
            oven.getItems().setStackInSlot(0, new ItemStack(ModItems.COOKIE_DOUGH.get(), 32));
            oven.getItems().setStackInSlot(1, new ItemStack(Items.COCOA_BEANS, 32));
            oven.getItems().setStackInSlot(BakeryOvenBlockEntity.FUEL_SLOT, new ItemStack(Items.COAL, 16));
        }
    }

    private static void counter(ServerLevel level, int x, int z, ItemStack treat) {
        set(level, x, 0, z, ModBlocks.BAKERY_COUNTER.get().defaultBlockState());
        counterTop(level, x, z, treat);
    }

    private static void counterTop(ServerLevel level, int x, int z, ItemStack treat) {
        if (level.getBlockEntity(at(x, 0, z)) instanceof DisplayBlockEntity display) {
            display.insert(treat);
        }
    }

    private static void display(ServerLevel level, int x, int y, int z, Block block, ItemStack... treats) {
        set(level, x, y, z, block.defaultBlockState());
        if (level.getBlockEntity(at(x, y, z)) instanceof DisplayBlockEntity display) {
            for (ItemStack treat : treats) {
                display.insert(treat);
            }
        }
    }

    private static void table(ServerLevel level, int x, int z, BlockState top) {
        set(level, x, 0, z, Blocks.CHERRY_FENCE.defaultBlockState());
        set(level, x, 1, z, top);
    }

    private static void chair(ServerLevel level, int x, int z, Direction back) {
        set(level, x, 0, z, Blocks.CHERRY_STAIRS.defaultBlockState().setValue(StairBlock.FACING, back));
    }

    private static void crops(ServerLevel level, int z, CropBlock crop) {
        for (int x = -6; x <= -2; x++) {
            set(level, x, -1, z, Blocks.FARMLAND.defaultBlockState());
            set(level, x, 0, z, crop.getStateForAge(crop.getMaxAge()));
        }
    }
}
