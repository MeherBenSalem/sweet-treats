package com.sugarplumstudios.sweettreats.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Bakery Oven recipe: 1-4 shapeless ingredients baked into a result using fuel.
 * <pre>{"type": "sweettreats:baking", "ingredients": [...], "result": {...}, "cookingtime": 200, "experience": 0.35}</pre>
 * The container passed to {@link #matches} holds the 4 ingredient slots.
 */
public class BakingRecipe implements Recipe<SweetInput> {
    public static final int MAX_INGREDIENTS = 4;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final int cookingTime;
    private final float experience;

    public BakingRecipe(NonNullList<Ingredient> ingredients, ItemStack result, int cookingTime, float experience) {
        this.ingredients = ingredients;
        this.result = result;
        this.cookingTime = cookingTime;
        this.experience = experience;
    }

    public static List<ItemStack> contents(Container container, int count) {
        List<ItemStack> list = new ArrayList<>(count);
        for (int i = 0; i < Math.min(count, container.getContainerSize()); i++) {
            list.add(container.getItem(i));
        }
        return list;
    }

    public static List<ItemStack> contents(SweetInput input, int count) {
        return new ArrayList<>(input.items().subList(0, Math.min(count, input.size())));
    }

    @Override
    public boolean matches(SweetInput input, Level level) {
        return RecipeUtil.allocate(ingredients, contents(input, MAX_INGREDIENTS)) != null;
    }

    @Override
    public ItemStack assemble(SweetInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    /** Cooking time after the configured oven speed multiplier. */
    public int getScaledCookingTime() {
        return Math.max(1, (int) Math.round(cookingTime / SweetTreatsConfig.ovenSpeedMultiplier()));
    }

    public float getExperience() {
        return experience;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModItems.BAKERY_OVEN.get());
    }

    @Override
    public boolean isSpecial() {
        // Hidden from the vanilla recipe book; Sweet Treats has its own.
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BAKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BAKING.get();
    }

    public static class Serializer implements RecipeSerializer<BakingRecipe> {
        private static final MapCodec<BakingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RecipeUtil.ingredientList(1, MAX_INGREDIENTS).fieldOf("ingredients").forGetter(r -> r.ingredients),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(r -> r.cookingTime),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(r -> r.experience)
        ).apply(i, BakingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, BakingRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<BakingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BakingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static BakingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            NonNullList<Ingredient> ingredients = RecipeUtil.readIngredients(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            int time = buffer.readVarInt();
            float xp = buffer.readFloat();
            return new BakingRecipe(ingredients, result, time, xp);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, BakingRecipe recipe) {
            RecipeUtil.writeIngredients(buffer, recipe.ingredients);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.cookingTime);
            buffer.writeFloat(recipe.experience);
        }
    }
}
