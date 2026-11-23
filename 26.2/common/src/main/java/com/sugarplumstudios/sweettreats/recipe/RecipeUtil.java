package com.sugarplumstudios.sweettreats.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.sugarplumstudios.sweettreats.platform.Services;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Shapeless, stack-aware ingredient matching shared by the oven, mixing bowl and decoration station.
 * <p>
 * Every ingredient must be satisfied by one item from some slot, a slot may satisfy several ingredients
 * if it holds enough items (e.g. two sugar from a single stack), and every non-empty slot must be used.
 */
public final class RecipeUtil {
    private RecipeUtil() {
    }

    /**
     * @return how many items to take from each slot, or {@code null} if the slots do not match the ingredients.
     */
    @Nullable
    public static int[] allocate(List<Ingredient> ingredients, List<ItemStack> slots) {
        int n = ingredients.size();
        int slotCount = slots.size();
        int nonEmpty = 0;
        int[] remaining = new int[slotCount];
        for (int s = 0; s < slotCount; s++) {
            ItemStack stack = slots.get(s);
            if (!stack.isEmpty()) {
                nonEmpty++;
                remaining[s] = stack.getCount();
                boolean usable = false;
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.test(stack)) {
                        usable = true;
                        break;
                    }
                }
                if (!usable) {
                    return null;
                }
            }
        }
        if (n == 0 || nonEmpty == 0 || nonEmpty > n) {
            return null;
        }

        // Most constrained ingredients first keeps the search tiny.
        Integer[] order = new Integer[n];
        int[] candidates = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
            for (int s = 0; s < slotCount; s++) {
                if (remaining[s] > 0 && ingredients.get(i).test(slots.get(s))) {
                    candidates[i]++;
                }
            }
            if (candidates[i] == 0) {
                return null;
            }
        }
        Arrays.sort(order, Comparator.comparingInt(i -> candidates[i]));

        int[] used = new int[slotCount];
        return search(0, order, ingredients, slots, remaining, used) ? used : null;
    }

    private static boolean search(int k, Integer[] order, List<Ingredient> ingredients, List<ItemStack> slots, int[] remaining, int[] used) {
        if (k == order.length) {
            for (int s = 0; s < slots.size(); s++) {
                if (!slots.get(s).isEmpty() && used[s] == 0) {
                    return false;
                }
            }
            return true;
        }
        // Prune: the slots still unused must be coverable by the ingredients left.
        int unusedSlots = 0;
        for (int s = 0; s < slots.size(); s++) {
            if (!slots.get(s).isEmpty() && used[s] == 0) {
                unusedSlots++;
            }
        }
        if (unusedSlots > order.length - k) {
            return false;
        }
        Ingredient ingredient = ingredients.get(order[k]);
        for (int s = 0; s < slots.size(); s++) {
            if (remaining[s] > 0 && ingredient.test(slots.get(s))) {
                remaining[s]--;
                used[s]++;
                if (search(k + 1, order, ingredients, slots, remaining, used)) {
                    return true;
                }
                remaining[s]++;
                used[s]--;
            }
        }
        return false;
    }

    /**
     * Removes the allocated items from a machine inventory, leaving crafting remainders (buckets, bottles) behind.
     * Remainders go back into the slot when it empties, otherwise they pop out above the block.
     */
    public static void consume(Container handler, int firstSlot, int[] used, Level level, BlockPos pos) {
        for (int s = 0; s < used.length; s++) {
            if (used[s] <= 0) {
                continue;
            }
            ItemStack stack = handler.getItem(firstSlot + s);
            ItemStack remainder = Services.PLATFORM.craftingRemainder(stack);
            ItemStack left = stack.copy();
            left.shrink(used[s]);
            if (!remainder.isEmpty()) {
                remainder.setCount(Math.min(remainder.getMaxStackSize(), remainder.getCount() * used[s]));
                if (left.isEmpty()) {
                    handler.setItem(firstSlot + s, remainder);
                    continue;
                }
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, remainder);
            }
            handler.setItem(firstSlot + s, left);
        }
    }

    /**
     * Finds the matching recipe with the most ingredients (ties broken by id) so results are deterministic.
     * Recipes live on the server only, so this finds nothing on a client.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <R extends Recipe<SweetInput>> R findBest(Level level, RecipeType<R> type, Container inventory) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return null;
        }
        SweetInput input = SweetInput.of(inventory);
        RecipeHolder<?> best = null;
        int bestSize = -1;
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            if (holder.value().getType() != type || !((R) holder.value()).matches(input, level)) {
                continue;
            }
            int size = ingredientCount(holder.value());
            Identifier id = holder.id().identifier();
            if (size > bestSize || (size == bestSize && best != null && id.compareTo(best.id().identifier()) < 0)) {
                best = holder;
                bestSize = size;
            }
        }
        return best == null ? null : (R) best.value();
    }

    private static int ingredientCount(Recipe<?> recipe) {
        if (recipe instanceof BakingRecipe baking) {
            return baking.getIngredients().size();
        }
        if (recipe instanceof MixingRecipe mixing) {
            return mixing.getIngredients().size();
        }
        if (recipe instanceof DecoratingRecipe decorating) {
            return decorating.getIngredients().size();
        }
        return 0;
    }

    // ------------------------------------------------------------------ codec helpers

    /** A JSON list of {@code min}..{@code max} ingredients. */
    public static Codec<List<Ingredient>> ingredientList(int min, int max) {
        return Ingredient.CODEC.listOf().validate(list -> list.size() < min || list.size() > max
                ? DataResult.error(() -> "Expected between " + min + " and " + max + " ingredients, got " + list.size())
                : DataResult.success(list));
    }
}
