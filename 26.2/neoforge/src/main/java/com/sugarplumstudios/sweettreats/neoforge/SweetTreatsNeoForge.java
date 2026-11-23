package com.sugarplumstudios.sweettreats.neoforge;

import com.mojang.serialization.MapCodec;
import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.event.CommonEvents;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * NeoForge entry point. Seed and fruit drops are global loot modifiers, wild plants are biome modifiers and
 * compostables a data map (all under data/), everything else is wired here. Wandering trader trades are not offered
 * on Minecraft 26.x.
 */
@Mod(SweetTreats.MOD_ID)
public final class SweetTreatsNeoForge {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SweetTreats.MOD_ID);

    static {
        LOOT_MODIFIERS.register("add_weighted_item", () -> AddWeightedItemModifier.CODEC);
    }

    public SweetTreatsNeoForge(IEventBus modBus) {
        SweetTreats.init();
        NeoForgePlatform.registerAll(modBus);
        LOOT_MODIFIERS.register(modBus);
        modBus.addListener(NeoForgeNetwork::register);
        modBus.addListener(SweetTreatsNeoForge::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(SweetTreatsNeoForge::onLogin);
        NeoForge.EVENT_BUS.addListener(SweetTreatsNeoForge::onClone);
        NeoForge.EVENT_BUS.addListener(SweetTreatsNeoForge::onCrafted);
        NeoForge.EVENT_BUS.addListener(SweetTreatsNeoForge::onPickup);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForgeClient.init(modBus);
        }
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CommonEvents.onLogin(player);
        }
    }

    private static void onClone(PlayerEvent.Clone event) {
        CommonEvents.onClone(event.getOriginal(), event.getEntity());
    }

    private static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeDiscovery.discoverByResult(player, event.getCrafting());
        }
    }

    private static void onPickup(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            RecipeDiscovery.discoverByResult(player, event.getOriginalStack());
        }
    }

    /** The oven and mixing bowl offer their inventory to pipes: top ingredients, bottom result, sides fuel or containers. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.BAKERY_OVEN.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.MIXING_BOWL.get(), WorldlyContainerWrapper::new);
    }
}
