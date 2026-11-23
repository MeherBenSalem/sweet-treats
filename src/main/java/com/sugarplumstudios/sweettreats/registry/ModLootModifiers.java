package com.sugarplumstudios.sweettreats.registry;

import com.mojang.serialization.Codec;
import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.loot.AddWeightedItemModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SweetTreats.MOD_ID);

    public static final RegistryObject<Codec<AddWeightedItemModifier>> ADD_WEIGHTED_ITEM =
            SERIALIZERS.register("add_weighted_item", AddWeightedItemModifier.CODEC);

    private ModLootModifiers() {
    }
}
