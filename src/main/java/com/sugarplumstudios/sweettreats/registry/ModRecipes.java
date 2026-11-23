package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.recipe.BakingRecipe;
import com.sugarplumstudios.sweettreats.recipe.DecoratingRecipe;
import com.sugarplumstudios.sweettreats.recipe.MixingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, SweetTreats.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SweetTreats.MOD_ID);

    public static final RegistryObject<RecipeType<BakingRecipe>> BAKING = TYPES.register("baking", () -> RecipeType.simple(SweetTreats.id("baking")));
    public static final RegistryObject<RecipeType<MixingRecipe>> MIXING = TYPES.register("mixing", () -> RecipeType.simple(SweetTreats.id("mixing")));
    public static final RegistryObject<RecipeType<DecoratingRecipe>> DECORATING = TYPES.register("decorating", () -> RecipeType.simple(SweetTreats.id("decorating")));

    public static final RegistryObject<RecipeSerializer<BakingRecipe>> BAKING_SERIALIZER = SERIALIZERS.register("baking", BakingRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<MixingRecipe>> MIXING_SERIALIZER = SERIALIZERS.register("mixing", MixingRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<DecoratingRecipe>> DECORATING_SERIALIZER = SERIALIZERS.register("decorating", DecoratingRecipe.Serializer::new);

    private ModRecipes() {
    }
}
