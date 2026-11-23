package com.sugarplumstudios.sweettreats.recipe;

import com.google.gson.JsonObject;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Bakery Oven recipe: 1-4 shapeless ingredients baked into a result using fuel.
 * <pre>{"type": "sweettreats:baking", "ingredients": [...], "result": {...}, "cookingtime": 200, "experience": 0.35}</pre>
 * The container passed to {@link #matches} holds the 4 ingredient slots.
 */
public class BakingRecipe implements Recipe<Container> {
    public static final int MAX_INGREDIENTS = 4;
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final int cookingTime;
    private final float experience;

    public BakingRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack result, int cookingTime, float experience) {
        this.id = id;
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

    @Override
    public boolean matches(Container container, Level level) {
        return RecipeUtil.allocate(ingredients, contents(container, MAX_INGREDIENTS)) != null;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
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
    public ResourceLocation getId() {
        return id;
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
        @Override
        public BakingRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Ingredient> ingredients = RecipeUtil.readIngredients(json, "ingredients", 1, MAX_INGREDIENTS);
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int time = GsonHelper.getAsInt(json, "cookingtime", 200);
            float xp = GsonHelper.getAsFloat(json, "experience", 0.0F);
            return new BakingRecipe(id, ingredients, result, time, xp);
        }

        @Override
        public BakingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> ingredients = RecipeUtil.readIngredients(buffer);
            ItemStack result = buffer.readItem();
            int time = buffer.readVarInt();
            float xp = buffer.readFloat();
            return new BakingRecipe(id, ingredients, result, time, xp);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BakingRecipe recipe) {
            RecipeUtil.writeIngredients(buffer, recipe.ingredients);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.cookingTime);
            buffer.writeFloat(recipe.experience);
        }
    }
}
