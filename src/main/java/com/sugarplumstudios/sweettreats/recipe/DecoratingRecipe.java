package com.sugarplumstudios.sweettreats.recipe;

import com.google.gson.JsonObject;
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

import java.util.List;

/**
 * Decoration Station recipe: a base treat plus one or two toppings, decorated instantly.
 * <pre>{"type": "sweettreats:decorating", "base": {...}, "toppings": [...], "result": {...}}</pre>
 * The container passed to {@link #matches} holds: slot 0 = base, slots 1-2 = toppings.
 */
public class DecoratingRecipe implements Recipe<Container> {
    public static final int MAX_TOPPINGS = 2;
    private final ResourceLocation id;
    private final Ingredient base;
    private final NonNullList<Ingredient> toppings;
    private final ItemStack result;

    public DecoratingRecipe(ResourceLocation id, Ingredient base, NonNullList<Ingredient> toppings, ItemStack result) {
        this.id = id;
        this.base = base;
        this.toppings = toppings;
        this.result = result;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return inv.getContainerSize() >= 3 && base.test(inv.getItem(0)) && toppingAllocation(inv) != null;
    }

    /** How many items to take from topping slots 1 and 2, or null. */
    public int[] toppingAllocation(Container inv) {
        List<ItemStack> slots = List.of(inv.getItem(1), inv.getItem(2));
        return RecipeUtil.allocate(toppings, slots);
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
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

    public Ingredient getBase() {
        return base;
    }

    public NonNullList<Ingredient> getToppings() {
        return toppings;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> all = NonNullList.create();
        all.add(base);
        all.addAll(toppings);
        return all;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModItems.DECORATION_STATION.get());
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DECORATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DECORATING.get();
    }

    public static class Serializer implements RecipeSerializer<DecoratingRecipe> {
        @Override
        public DecoratingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient base = Ingredient.fromJson(json.get("base"));
            NonNullList<Ingredient> toppings = RecipeUtil.readIngredients(json, "toppings", 1, MAX_TOPPINGS);
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new DecoratingRecipe(id, base, toppings, result);
        }

        @Override
        public DecoratingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient base = Ingredient.fromNetwork(buffer);
            NonNullList<Ingredient> toppings = RecipeUtil.readIngredients(buffer);
            ItemStack result = buffer.readItem();
            return new DecoratingRecipe(id, base, toppings, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DecoratingRecipe recipe) {
            recipe.base.toNetwork(buffer);
            RecipeUtil.writeIngredients(buffer, recipe.toppings);
            buffer.writeItem(recipe.result);
        }
    }
}
