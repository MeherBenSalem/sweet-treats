package com.sugarplumstudios.sweettreats.discovery;

import com.sugarplumstudios.sweettreats.recipe.BakingRecipe;
import com.sugarplumstudios.sweettreats.recipe.DecoratingRecipe;
import com.sugarplumstudios.sweettreats.recipe.MixingRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * One recipe as the recipe book shows it, built on the server and sent with the book.
 *
 * @param ingredients the item choices of each ingredient; for the decoration station the base comes first, then the
 *                    toppings; for a shaped crafting recipe the grid row by row ({@code width} x {@code height})
 * @param width       grid width of a shaped crafting recipe, 0 for every other recipe
 * @param container   the item choices of the container a mixing recipe uses up (empty if none)
 * @param time        baking or mixing time in ticks after the config's speed multipliers, 0 if untimed
 */
public record RecipeBookEntry(Category category, ResourceLocation id, ItemStack result, List<List<ItemStack>> ingredients,
                              int width, int height, List<ItemStack> container, int time) {
    public enum Category {
        BAKING, MIXING, DECORATING, CRAFTING
    }

    public boolean shaped() {
        return width > 0;
    }

    /** Every tracked recipe, per station in item order. */
    public static List<RecipeBookEntry> collect(RecipeManager manager, RegistryAccess access) {
        List<RecipeBookEntry> entries = new ArrayList<>();
        for (Recipe<?> recipe : RecipeDiscovery.trackedRecipes(manager)) {
            ItemStack result = recipe.getResultItem(access).copy();
            if (recipe instanceof BakingRecipe baking) {
                entries.add(new RecipeBookEntry(Category.BAKING, recipe.getId(), result, options(baking.getIngredients()), 0, 0,
                        List.of(), baking.getScaledCookingTime()));
            } else if (recipe instanceof MixingRecipe mixing) {
                entries.add(new RecipeBookEntry(Category.MIXING, recipe.getId(), result, options(mixing.getIngredients()), 0, 0,
                        mixing.needsContainer() ? options(mixing.getContainer()) : List.of(), mixing.getScaledMixingTime()));
            } else if (recipe instanceof DecoratingRecipe decorating) {
                entries.add(new RecipeBookEntry(Category.DECORATING, recipe.getId(), result, options(decorating.getIngredients()), 0, 0,
                        List.of(), 0));
            } else if (recipe instanceof ShapedRecipe shaped) {
                entries.add(new RecipeBookEntry(Category.CRAFTING, recipe.getId(), result, options(shaped.getIngredients()),
                        shaped.getWidth(), shaped.getHeight(), List.of(), 0));
            } else {
                entries.add(new RecipeBookEntry(Category.CRAFTING, recipe.getId(), result, options(recipe.getIngredients()), 0, 0,
                        List.of(), 0));
            }
        }
        entries.sort(Comparator.<RecipeBookEntry>comparingInt(entry -> entry.category().ordinal())
                .thenComparingInt(entry -> BuiltInRegistries.ITEM.getId(entry.result().getItem()))
                .thenComparing(RecipeBookEntry::id));
        return entries;
    }

    private static List<List<ItemStack>> options(List<Ingredient> ingredients) {
        List<List<ItemStack>> list = new ArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            list.add(options(ingredient));
        }
        return list;
    }

    private static List<ItemStack> options(Ingredient ingredient) {
        return Arrays.stream(ingredient.getItems()).map(ItemStack::copy).toList();
    }

    // ------------------------------------------------------------------ network

    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(category);
        buffer.writeResourceLocation(id);
        buffer.writeItem(result);
        buffer.writeVarInt(ingredients.size());
        for (List<ItemStack> choices : ingredients) {
            writeStacks(buffer, choices);
        }
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
        writeStacks(buffer, container);
        buffer.writeVarInt(time);
    }

    public static RecipeBookEntry read(FriendlyByteBuf buffer) {
        Category category = buffer.readEnum(Category.class);
        ResourceLocation id = buffer.readResourceLocation();
        ItemStack result = buffer.readItem();
        int count = buffer.readVarInt();
        List<List<ItemStack>> ingredients = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ingredients.add(readStacks(buffer));
        }
        int width = buffer.readVarInt();
        int height = buffer.readVarInt();
        List<ItemStack> container = readStacks(buffer);
        int time = buffer.readVarInt();
        return new RecipeBookEntry(category, id, result, ingredients, width, height, container, time);
    }

    private static void writeStacks(FriendlyByteBuf buffer, List<ItemStack> stacks) {
        buffer.writeVarInt(stacks.size());
        for (ItemStack stack : stacks) {
            buffer.writeItem(stack);
        }
    }

    private static List<ItemStack> readStacks(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(buffer.readItem());
        }
        return stacks;
    }
}
