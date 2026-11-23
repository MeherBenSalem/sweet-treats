package com.sugarplumstudios.sweettreats.event;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SweetTreats.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {
    private CommonEvents() {
    }

    /** First join: hand out a recipe book (configurable). */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !SweetTreatsConfig.giveRecipeBookOnFirstJoin()) {
            return;
        }
        CompoundTag data = RecipeDiscovery.persistedData(player);
        if (!data.getBoolean(RecipeDiscovery.RECEIVED_BOOK_KEY)) {
            data.putBoolean(RecipeDiscovery.RECEIVED_BOOK_KEY, true);
            ItemStack book = new ItemStack(ModItems.RECIPE_BOOK.get());
            if (!player.getInventory().add(book)) {
                player.drop(book, false);
            }
        }
    }

    /** Keep discoveries across death and End-portal respawns. */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        CompoundTag oldData = RecipeDiscovery.persistedData(original);
        CompoundTag newData = RecipeDiscovery.persistedData(event.getEntity());
        for (String key : new String[]{RecipeDiscovery.DISCOVERED_KEY, RecipeDiscovery.RECEIVED_BOOK_KEY}) {
            if (oldData.contains(key) && !newData.contains(key)) {
                newData.put(key, oldData.get(key).copy());
            }
        }
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeDiscovery.discoverByResult(player, event.getCrafting());
        }
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeDiscovery.discoverByResult(player, event.getStack());
        }
    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        if (!SweetTreatsConfig.wanderingTraderTrades()) {
            return;
        }
        event.getGenericTrades().add((trader, random) ->
                new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(ModItems.STRAWBERRY_SEEDS.get(), 3), 12, 1, 0.05F));
        event.getGenericTrades().add((trader, random) ->
                new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(ModItems.LAVENDER_SEEDS.get(), 3), 12, 1, 0.05F));
        event.getGenericTrades().add((trader, random) ->
                new MerchantOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(ModItems.VANILLA_SEEDS.get(), 2), 8, 1, 0.05F));
        event.getGenericTrades().add((trader, random) ->
                new MerchantOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(ModItems.PEACH_PIT.get(), 1), 6, 1, 0.05F));
        event.getGenericTrades().add((trader, random) ->
                new MerchantOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(ModItems.CHERRY_PIT.get(), 1), 6, 1, 0.05F));
    }
}
