package com.sugarplumstudios.sweettreats.dev;

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
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.server.IntegratedServer;
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
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.sugarplumstudios.sweettreats.platform.RegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Development-only visual self test (not part of the mod jar). Enabled with -Dsweettreats.visualtest=true:
 * creates a flat creative world, builds a small bakery scene, opens every GUI and saves screenshots, then quits.
 */
@Mod.EventBusSubscriber(modid = SweetTreats.MOD_ID, value = Dist.CLIENT)
public final class VisualSelfTest {
    private static final boolean ENABLED = Boolean.getBoolean("sweettreats.visualtest");
    private static final TreeMap<Integer, Runnable> STEPS = new TreeMap<>();
    private static int menuTicks;
    private static boolean worldRequested;
    private static int worldTicks;
    private static BlockPos origin = BlockPos.ZERO;

    private VisualSelfTest() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            if (!worldRequested && ++menuTicks > 60) {
                worldRequested = true;
                createWorld(mc);
            }
            return;
        }
        if (mc.player == null || !mc.hasSingleplayerServer()) {
            return;
        }
        if (worldTicks == 0) {
            schedule();
        }
        worldTicks++;
        Runnable step = STEPS.remove(worldTicks);
        if (step != null) {
            try {
                step.run();
            } catch (Throwable t) {
                SweetTreats.LOGGER.error("[visualtest] step at tick {} failed", worldTicks, t);
            }
        }
    }

    private static void createWorld(Minecraft mc) {
        SweetTreats.LOGGER.info("[visualtest] creating world");
        LevelSettings settings = new LevelSettings("SweetTreatsVisualTest", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("SweetTreatsVisualTest" + System.currentTimeMillis(), settings,
                new WorldOptions(20261104L, false, false),
                access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
    }

    private static IntegratedServer server() {
        return Minecraft.getInstance().getSingleplayerServer();
    }

    private static void onServer(java.util.function.Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = server();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (player != null) {
                action.accept(player);
            }
        });
    }

    private static void shot(String name) {
        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, "sweettreats_" + name + ".png", mc.getMainRenderTarget(),
                message -> SweetTreats.LOGGER.info("[visualtest] screenshot {}: {}", name, message.getString()));
    }

    private static void schedule() {
        Minecraft mc = Minecraft.getInstance();
        STEPS.put(40, () -> onServer(VisualSelfTest::buildScene));
        STEPS.put(60, () -> mc.options.hideGui = true);
        STEPS.put(160, () -> shot("scene"));
        STEPS.put(165, () -> onServer(player -> player.teleportTo(player.serverLevel(),
                origin.getX() + 0.5D, origin.getY() + 0.2D, origin.getZ() + 1.4D, 0.0F, 38.0F)));
        STEPS.put(220, () -> shot("scene_closeup"));
        STEPS.put(225, () -> onServer(player -> player.teleportTo(player.serverLevel(),
                origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D, 0.0F, 22.0F)));
        STEPS.put(240, () -> mc.options.hideGui = false);

        STEPS.put(250, () -> onServer(player -> {
            if (player.level().getBlockEntity(origin.offset(-2, 0, 6)) instanceof BakeryOvenBlockEntity oven) {
                player.openMenu(oven);
            }
        }));
        STEPS.put(275, () -> shot("gui_oven"));
        STEPS.put(280, () -> onServer(ServerPlayer::closeContainer));

        STEPS.put(290, () -> onServer(player -> {
            if (player.level().getBlockEntity(origin.offset(4, 0, 3)) instanceof MixingBowlBlockEntity bowl) {
                player.openMenu(bowl);
            }
        }));
        STEPS.put(315, () -> shot("gui_mixing_bowl"));
        STEPS.put(320, () -> onServer(ServerPlayer::closeContainer));

        STEPS.put(330, () -> onServer(player -> {
            BlockPos station = origin.offset(0, 0, 6);
            player.openMenu(player.level().getBlockState(station).getMenuProvider(player.level(), station));
            if (player.containerMenu instanceof DecorationStationMenu menu) {
                menu.slots.get(0).set(new ItemStack(ModItems.PLAIN_CUPCAKE.get(), 3));
                menu.slots.get(1).set(new ItemStack(ModItems.PINK_ICING.get(), 2));
                menu.slots.get(2).set(new ItemStack(ModItems.SUGAR_FLOWERS.get(), 2));
                menu.broadcastChanges();
            }
        }));
        STEPS.put(355, () -> shot("gui_decoration_station"));
        STEPS.put(360, () -> onServer(ServerPlayer::closeContainer));

        STEPS.put(370, () -> onServer(player -> {
            for (Item item : List.of(ModItems.SUGAR_COOKIE.get(), ModItems.SPONGE_CAKE.get(), ModItems.CINNAMON_ROLL.get(),
                    ModItems.CHERRY_PIE.get(), ModItems.HEART_CANDY.get(), ModItems.PLAIN_CUPCAKE.get(), ModItems.LAVENDER_TEA.get())) {
                RecipeDiscovery.discoverByResult(player, new ItemStack(item));
            }
            ModNetwork.openRecipeBook(player);
        }));
        STEPS.put(395, () -> shot("gui_recipe_book_oven"));
        STEPS.put(400, () -> clickRecipeBook(2, -15)); // Decorating tab
        STEPS.put(420, () -> shot("gui_recipe_book_decorating"));
        STEPS.put(425, () -> clickRecipeBook(3, -15)); // Crafting tab
        STEPS.put(430, () -> clickRecipeBookCell(11));  // a shaped recipe
        STEPS.put(450, () -> shot("gui_recipe_book_crafting"));
        STEPS.put(455, () -> mc.setScreen(null));

        STEPS.put(462, () -> onServer(player -> player.setGameMode(GameType.SURVIVAL)));
        STEPS.put(465, () -> onServer(player -> fillInventory(player, 0)));
        STEPS.put(480, () -> mc.setScreen(new InventoryScreen(mc.player)));
        STEPS.put(500, () -> shot("items_1"));
        STEPS.put(505, () -> mc.setScreen(null));
        STEPS.put(510, () -> onServer(player -> fillInventory(player, 36)));
        STEPS.put(525, () -> mc.setScreen(new InventoryScreen(mc.player)));
        STEPS.put(545, () -> shot("items_2"));
        STEPS.put(550, () -> mc.setScreen(null));

        STEPS.put(555, () -> onServer(player -> player.setGameMode(GameType.CREATIVE)));
        STEPS.put(560, () -> onServer(player -> {
            player.teleportTo(player.serverLevel(), origin.getX() + 6.5D, origin.getY() + 1.0D, origin.getZ() + 5.5D, 90.0F, 30.0F);
        }));
        STEPS.put(565, () -> mc.options.hideGui = true);
        STEPS.put(640, () -> shot("scene_side"));
        STEPS.put(660, () -> {
            SweetTreats.LOGGER.info("[visualtest] done, stopping client");
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

    private static void fillInventory(ServerPlayer player, int start) {
        player.getInventory().clearContent();
        List<RegistryEntry<Item>> all = new ArrayList<>(ModItems.ALL);
        for (int i = start; i < Math.min(all.size(), start + 36); i++) {
            player.getInventory().add(new ItemStack(all.get(i).get()));
        }
    }

    // ------------------------------------------------------------------ scene

    private static void buildScene(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        origin = player.blockPosition();
        level.setDayTime(6000);
        level.setWeatherParameters(6000, 0, false, false);
        int y = origin.getY();

        // clear a work area
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-7, 0, -1), origin.offset(7, 4, 10))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        // floor of pink & white "tiles"
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-7, -1, 1), origin.offset(7, -1, 7))) {
            boolean even = ((pos.getX() + pos.getZ()) & 1) == 0;
            level.setBlock(pos, even ? Blocks.PINK_CONCRETE.defaultBlockState() : Blocks.WHITE_CONCRETE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        // Row 1 (z+3): presentation
        place(level, origin.offset(-5, 0, 3), Blocks.STONE.defaultBlockState());
        place(level, origin.offset(-4, 0, 3), ModBlocks.STRAWBERRY_CAKE.get().defaultBlockState());
        place(level, origin.offset(-3, 0, 3), ModBlocks.CHERRY_PIE.get().defaultBlockState().setValue(PieTreatBlock.BITES, 1));
        place(level, origin.offset(-2, 0, 3), ModBlocks.CAKE_DISPLAY.get().defaultBlockState());
        display(level, origin.offset(-2, 0, 3), new ItemStack(ModItems.STRAWBERRY_CAKE.get()));
        place(level, origin.offset(-1, 0, 3), ModBlocks.BAKERY_COUNTER.get().defaultBlockState());
        display(level, origin.offset(-1, 0, 3), new ItemStack(ModItems.PINK_VANILLA_CUPCAKE.get()));
        place(level, origin.offset(0, 0, 3), ModBlocks.CUPCAKE_STAND.get().defaultBlockState());
        display(level, origin.offset(0, 0, 3), new ItemStack(ModItems.CHOCOLATE_CUPCAKE.get()));
        display(level, origin.offset(0, 0, 3), new ItemStack(ModItems.HONEY_CUPCAKE.get()));
        display(level, origin.offset(0, 0, 3), new ItemStack(ModItems.PINK_VANILLA_CUPCAKE.get()));
        place(level, origin.offset(1, 0, 3), ModBlocks.DESSERT_PLATE.get().defaultBlockState());
        display(level, origin.offset(1, 0, 3), new ItemStack(ModItems.LAVENDER_MACARON.get()));
        place(level, origin.offset(2, 0, 3), ModBlocks.GLASS_JAR.get().defaultBlockState());
        display(level, origin.offset(2, 0, 3), new ItemStack(ModItems.HEART_CANDY.get(), 40));
        place(level, origin.offset(3, 0, 3), ModBlocks.TEA_SET.get().defaultBlockState());
        place(level, origin.offset(4, 0, 3), ModBlocks.MIXING_BOWL.get().defaultBlockState());
        if (level.getBlockEntity(origin.offset(4, 0, 3)) instanceof MixingBowlBlockEntity bowl) {
            bowl.setItem(0, new ItemStack(Items.SUGAR, 64));
            bowl.setItem(1, new ItemStack(ModItems.CREAM.get(), 32));
            bowl.setItem(2, new ItemStack(ModItems.STRAWBERRY.get(), 32));
        }
        place(level, origin.offset(5, 0, 3), ModBlocks.CAKE_DISPLAY.get().defaultBlockState());
        display(level, origin.offset(5, 0, 3), new ItemStack(ModItems.CHERRY_PIE.get()));

        // Row 2 (z+6): machines, a wall with the sign
        place(level, origin.offset(-2, 0, 6), ModBlocks.BAKERY_OVEN.get().defaultBlockState());
        if (level.getBlockEntity(origin.offset(-2, 0, 6)) instanceof BakeryOvenBlockEntity oven) {
            oven.setItem(0, new ItemStack(ModItems.COOKIE_DOUGH.get(), 16));
            oven.setItem(1, new ItemStack(Items.COCOA_BEANS, 16));
            oven.setItem(BakeryOvenBlockEntity.FUEL_SLOT, new ItemStack(Items.COAL, 8));
        }
        place(level, origin.offset(-1, 0, 6), ModBlocks.BAKERY_COUNTER.get().defaultBlockState());
        display(level, origin.offset(-1, 0, 6), new ItemStack(ModItems.CINNAMON_ROLL.get()));
        place(level, origin.offset(0, 0, 6), ModBlocks.DECORATION_STATION.get().defaultBlockState());
        place(level, origin.offset(1, 0, 6), ModBlocks.DECORATIVE_MENU.get().defaultBlockState());
        for (int dx = -3; dx <= 3; dx++) {
            place(level, origin.offset(dx, 1, 7), Blocks.WHITE_CONCRETE.defaultBlockState());
            place(level, origin.offset(dx, 2, 7), Blocks.PINK_TERRACOTTA.defaultBlockState());
            place(level, origin.offset(dx, 0, 7), Blocks.WHITE_CONCRETE.defaultBlockState());
        }
        place(level, origin.offset(-1, 2, 6), ModBlocks.BAKERY_SIGN.get().defaultBlockState().setValue(DecorBlock.FACING, Direction.NORTH));
        place(level, origin.offset(0, 2, 6), ModBlocks.BAKERY_SIGN.get().defaultBlockState().setValue(DecorBlock.FACING, Direction.NORTH));
        place(level, origin.offset(2, 0, 6), ModBlocks.BAKERY_OVEN.get().defaultBlockState());

        // Garden to the side (x -7..-5 / +5..+7, z 5..7)
        for (int dz = 5; dz <= 7; dz++) {
            for (int dx = 5; dx <= 7; dx++) {
                BlockPos ground = origin.offset(dx, -1, dz);
                place(level, ground, Blocks.FARMLAND.defaultBlockState());
            }
        }
        place(level, origin.offset(6, -1, 6), Blocks.WATER.defaultBlockState());
        cropRow(level, 5, (CropBlock) ModBlocks.STRAWBERRY_CROP.get());
        cropRow(level, 6, (CropBlock) ModBlocks.VANILLA_CROP.get());
        cropRow(level, 7, (CropBlock) ModBlocks.LAVENDER_CROP.get());
        for (int i = 0; i < 4; i++) {
            BlockPos ground = origin.offset(-7 + (i % 2) * 2, -1, 5 + (i / 2) * 2);
            place(level, ground, Blocks.GRASS_BLOCK.defaultBlockState());
            Block tree = i < 2 ? ModBlocks.PEACH_TREE.get() : ModBlocks.CHERRY_TREE.get();
            place(level, ground.above(), tree.defaultBlockState().setValue(FruitTreeBlock.AGE, i % 2 == 0 ? 2 : 3));
        }
        BlockPos wildGround = origin.offset(-6, -1, 2);
        Block[] wild = {ModBlocks.WILD_STRAWBERRIES.get(), ModBlocks.WILD_LAVENDER.get(), ModBlocks.WILD_VANILLA.get()};
        for (int i = 0; i < 3; i++) {
            place(level, wildGround.offset(i - 1, 0, 0), Blocks.GRASS_BLOCK.defaultBlockState());
            place(level, wildGround.offset(i - 1, 1, 0), wild[i].defaultBlockState());
        }

        player.teleportTo(level, origin.getX() + 0.5D, y, origin.getZ() + 0.5D, 0.0F, 22.0F);
        SweetTreats.LOGGER.info("[visualtest] scene built at {}", origin);
    }

    private static void cropRow(ServerLevel level, int dz, CropBlock crop) {
        int[] ages = {3, 5, 7};
        for (int i = 0; i < 3; i++) {
            BlockPos pos = origin.offset(5 + i, 0, dz);
            if (level.getBlockState(pos.below()).is(Blocks.FARMLAND)) {
                place(level, pos, crop.getStateForAge(ages[i]));
            }
        }
    }

    private static void place(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private static void display(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof DisplayBlockEntity display) {
            display.insert(stack);
        }
    }
}
