package com.sugarplumstudios.sweettreats.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Bakery Oven recipe: 1-4 shapeless ingredients baked into a result using fuel.
 * <pre>{"type": "sweettreats:baking", "ingredients": [...], "result": {...}, "cookingtime": 200, "experience": 0.35}</pre>
 * The input passed to {@link #matches} holds the 4 ingredient slots.
 */
public record BakingRecipe(List<Ingredient> ingredients, ItemStackTemplate result, int cookingTime, float experience)
        implements Recipe<SweetInput> {
    public static final int MAX_INGREDIENTS = 4;

    public static final MapCodec<BakingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecipeUtil.ingredientList(1, MAX_INGREDIENTS).fieldOf("ingredients").forGetter(BakingRecipe::ingredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(BakingRecipe::result),
            Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(BakingRecipe::cookingTime),
            Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(BakingRecipe::experience)
    ).apply(i, BakingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BakingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_INGREDIENTS)), BakingRecipe::ingredients,
            ItemStackTemplate.STREAM_CODEC, BakingRecipe::result,
            ByteBufCodecs.VAR_INT, BakingRecipe::cookingTime,
            ByteBufCodecs.FLOAT, BakingRecipe::experience,
            BakingRecipe::new);

    public static RecipeSerializer<BakingRecipe> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
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
    public ItemStack assemble(SweetInput input) {
        return result.create();
    }

    public ItemStack getResult() {
        return result.create();
    }

    public List<Ingredient> getIngredients() {
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
    public boolean isSpecial() {
        // Hidden from the vanilla recipe book; Sweet Treats has its own.
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<BakingRecipe> getSerializer() {
        return ModRecipes.BAKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<BakingRecipe> getType() {
        return ModRecipes.BAKING.get();
    }
}
