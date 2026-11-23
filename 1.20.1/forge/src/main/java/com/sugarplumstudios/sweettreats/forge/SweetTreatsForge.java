package com.sugarplumstudios.sweettreats.forge;

import com.mojang.serialization.Codec;
import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.blockentity.MachineBlockEntity;
import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import com.sugarplumstudios.sweettreats.event.CommonEvents;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod(SweetTreats.MOD_ID)
public final class SweetTreatsForge {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SweetTreats.MOD_ID);

    static {
        LOOT_MODIFIERS.register("add_weighted_item", AddWeightedItemModifier.CODEC);
    }

    @SuppressWarnings("removal") // The context getter is correct for every Forge 47.x release.
    public SweetTreatsForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SweetTreats.init();
        ForgePlatform.registerAll(modBus);
        LOOT_MODIFIERS.register(modBus);
        modBus.addListener(SweetTreatsForge::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(SweetTreatsForge::onLogin);
        MinecraftForge.EVENT_BUS.addListener(SweetTreatsForge::onClone);
        MinecraftForge.EVENT_BUS.addListener(SweetTreatsForge::onCrafted);
        MinecraftForge.EVENT_BUS.addListener(SweetTreatsForge::onPickup);
        MinecraftForge.EVENT_BUS.addListener(SweetTreatsForge::onWandererTrades);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, SweetTreatsForge::attachItemHandlers);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClient.init(modBus);
        }
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        ForgeNetwork.register();
        event.enqueueWork(SweetTreats::registerCompostables);
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

    private static void onPickup(PlayerEvent.ItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeDiscovery.discoverByResult(player, event.getStack());
        }
    }

    private static void onWandererTrades(WandererTradesEvent event) {
        if (SweetTreatsConfig.wanderingTraderTrades()) {
            event.getGenericTrades().addAll(CommonEvents.wanderingTrades());
        }
    }

    /** The oven and mixing bowl offer their inventory to pipes: top ingredients, bottom result, sides fuel or containers. */
    private static void attachItemHandlers(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof MachineBlockEntity machine)) {
            return;
        }
        LazyOptional<IItemHandler> all = LazyOptional.of(() -> new InvWrapper(machine));
        LazyOptional<IItemHandlerModifiable>[] sided = SidedInvWrapper.create(machine, Direction.values());
        event.addCapability(SweetTreats.id("items"), new ICapabilityProvider() {
            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                if (capability != ForgeCapabilities.ITEM_HANDLER) {
                    return LazyOptional.empty();
                }
                return side == null ? all.cast() : sided[side.ordinal()].cast();
            }
        });
        event.addListener(() -> {
            all.invalidate();
            for (LazyOptional<IItemHandlerModifiable> handler : sided) {
                handler.invalidate();
            }
        });
    }
}
