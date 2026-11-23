package com.sugarplumstudios.sweettreats.registry;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.platform.RegistryEntry;
import com.sugarplumstudios.sweettreats.platform.Services;
import com.sugarplumstudios.sweettreats.recipe.BakingRecipe;
import com.sugarplumstudios.sweettreats.recipe.DecoratingRecipe;
import com.sugarplumstudios.sweettreats.recipe.MixingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipes {
    public static final RegistryEntry<RecipeType<BakingRecipe>> BAKING = Services.PLATFORM.register(Registries.RECIPE_TYPE, "baking",
            () -> type("baking"));
    public static final RegistryEntry<RecipeType<MixingRecipe>> MIXING = Services.PLATFORM.register(Registries.RECIPE_TYPE, "mixing",
            () -> type("mixing"));
    public static final RegistryEntry<RecipeType<DecoratingRecipe>> DECORATING = Services.PLATFORM.register(Registries.RECIPE_TYPE, "decorating",
            () -> type("decorating"));

    public static final RegistryEntry<RecipeSerializer<BakingRecipe>> BAKING_SERIALIZER = Services.PLATFORM.register(
            Registries.RECIPE_SERIALIZER, "baking", BakingRecipe::serializer);
    public static final RegistryEntry<RecipeSerializer<MixingRecipe>> MIXING_SERIALIZER = Services.PLATFORM.register(
            Registries.RECIPE_SERIALIZER, "mixing", MixingRecipe::serializer);
    public static final RegistryEntry<RecipeSerializer<DecoratingRecipe>> DECORATING_SERIALIZER = Services.PLATFORM.register(
            Registries.RECIPE_SERIALIZER, "decorating", DecoratingRecipe::serializer);

    /** Since 1.21.2 a recipe type is just a registered marker; toString keeps logs readable. */
    private static <T extends Recipe<?>> RecipeType<T> type(String name) {
        String id = SweetTreats.id(name).toString();
        return new RecipeType<T>() {
            @Override
            public String toString() {
                return id;
            }
        };
    }

    private ModRecipes() {
    }

    /** Loads the class, which registers the recipe types and serializers. */
    public static void init() {
    }
}
