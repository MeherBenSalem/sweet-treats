package com.sugarplumstudios.sweettreats.discovery;

import com.sugarplumstudios.sweettreats.recipe.BakingRecipe;
import com.sugarplumstudios.sweettreats.recipe.DecoratingRecipe;
import com.sugarplumstudios.sweettreats.recipe.MixingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.util.context.ContextMap;

import java.util.ArrayList;
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
public record RecipeBookEntry(Category category, Identifier id, ItemStack result, List<List<ItemStack>> ingredients,
                              int width, int height, List<ItemStack> container, int time) {
    public enum Category {
        BAKING, MIXING, DECORATING, CRAFTING
    }

    public boolean shaped() {
        return width > 0;
    }

    /** Every tracked recipe, per station in item order. */
    public static List<RecipeBookEntry> collect(RecipeManager manager, Level level) {
        ContextMap context = SlotDisplayContext.fromLevel(level);
        List<RecipeBookEntry> entries = new ArrayList<>();
        for (RecipeHolder<?> holder : RecipeDiscovery.trackedRecipes(manager)) {
            Recipe<?> recipe = holder.value();
            Identifier id = holder.id().identifier();
            ItemStack result = resultOf(recipe, context);
            if (recipe instanceof BakingRecipe baking) {
                entries.add(new RecipeBookEntry(Category.BAKING, id, result, options(baking.getIngredients(), context), 0, 0,
                        List.of(), baking.getScaledCookingTime()));
            } else if (recipe instanceof MixingRecipe mixing) {
                entries.add(new RecipeBookEntry(Category.MIXING, id, result, options(mixing.getIngredients(), context), 0, 0,
                        mixing.needsContainer() ? options(mixing.getContainer(), context) : List.of(), mixing.getScaledMixingTime()));
            } else if (recipe instanceof DecoratingRecipe decorating) {
                entries.add(new RecipeBookEntry(Category.DECORATING, id, result, options(decorating.getIngredients(), context), 0, 0,
                        List.of(), 0));
            } else {
                // Crafting recipes only describe themselves through their display since 1.21.2.
                RecipeDisplay display = recipe.display().isEmpty() ? null : recipe.display().get(0);
                if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                    entries.add(new RecipeBookEntry(Category.CRAFTING, id, result, slotOptions(shaped.ingredients(), context),
                            shaped.width(), shaped.height(), List.of(), 0));
                } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
                    entries.add(new RecipeBookEntry(Category.CRAFTING, id, result, slotOptions(shapeless.ingredients(), context),
                            0, 0, List.of(), 0));
                }
            }
        }
        entries.sort(Comparator.<RecipeBookEntry>comparingInt(entry -> entry.category().ordinal())
                .thenComparingInt(entry -> BuiltInRegistries.ITEM.getId(entry.result().getItem()))
                .thenComparing(RecipeBookEntry::id));
        return entries;
    }

    /** What a tracked recipe makes: our recipes know it, crafting recipes show it in their display. */
    public static ItemStack resultOf(Recipe<?> recipe, ContextMap context) {
        if (recipe instanceof BakingRecipe baking) {
            return baking.getResult();
        }
        if (recipe instanceof MixingRecipe mixing) {
            return mixing.getResult();
        }
        if (recipe instanceof DecoratingRecipe decorating) {
            return decorating.getResult();
        }
        for (RecipeDisplay display : recipe.display()) {
            ItemStack result = display.result().resolveForFirstStack(context);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<List<ItemStack>> options(List<Ingredient> ingredients, ContextMap context) {
        List<List<ItemStack>> list = new ArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            list.add(options(ingredient, context));
        }
        return list;
    }

    private static List<ItemStack> options(Ingredient ingredient, ContextMap context) {
        return List.copyOf(ingredient.display().resolveForStacks(context));
    }

    private static List<List<ItemStack>> slotOptions(List<SlotDisplay> slots, ContextMap context) {
        List<List<ItemStack>> list = new ArrayList<>(slots.size());
        for (SlotDisplay slot : slots) {
            list.add(List.copyOf(slot.resolveForStacks(context)));
        }
        return list;
    }

    // ------------------------------------------------------------------ network

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(category);
        buffer.writeIdentifier(id);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, result);
        buffer.writeVarInt(ingredients.size());
        for (List<ItemStack> choices : ingredients) {
            writeStacks(buffer, choices);
        }
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
        writeStacks(buffer, container);
        buffer.writeVarInt(time);
    }

    public static RecipeBookEntry read(RegistryFriendlyByteBuf buffer) {
        Category category = buffer.readEnum(Category.class);
        Identifier id = buffer.readIdentifier();
        ItemStack result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
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

    private static void writeStacks(RegistryFriendlyByteBuf buffer, List<ItemStack> stacks) {
        buffer.writeVarInt(stacks.size());
        for (ItemStack stack : stacks) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        }
    }

    private static List<ItemStack> readStacks(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
        }
        return stacks;
    }
}
