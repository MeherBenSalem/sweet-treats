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

/**
 * Mixing Bowl recipe: 1-6 shapeless ingredients plus an optional container (e.g. a glass bottle for drinks).
 * <pre>{"type": "sweettreats:mixing", "ingredients": [...], "container": {...}, "result": {...}, "mixingtime": 100}</pre>
 * The container passed to {@link #matches} holds the 6 ingredient slots followed by the container slot.
 */
public class MixingRecipe implements Recipe<Container> {
    public static final int MAX_INGREDIENTS = 6;
    public static final int CONTAINER_SLOT = 6;
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final Ingredient container;
    private final ItemStack result;
    private final int mixingTime;

    public MixingRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, Ingredient container, ItemStack result, int mixingTime) {
        this.id = id;
        this.ingredients = ingredients;
        this.container = container;
        this.result = result;
        this.mixingTime = mixingTime;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        if (RecipeUtil.allocate(ingredients, BakingRecipe.contents(inv, MAX_INGREDIENTS)) == null) {
            return false;
        }
        if (container.isEmpty()) {
            return true;
        }
        return inv.getContainerSize() > CONTAINER_SLOT && container.test(inv.getItem(CONTAINER_SLOT));
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

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    /** The container that is consumed (empty ingredient if the recipe needs none). */
    public Ingredient getContainer() {
        return container;
    }

    public boolean needsContainer() {
        return !container.isEmpty();
    }

    public int getMixingTime() {
        return mixingTime;
    }

    public int getScaledMixingTime() {
        return Math.max(1, (int) Math.round(mixingTime / SweetTreatsConfig.mixingSpeedMultiplier()));
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModItems.MIXING_BOWL.get());
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
        return ModRecipes.MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MIXING.get();
    }

    public static class Serializer implements RecipeSerializer<MixingRecipe> {
        @Override
        public MixingRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Ingredient> ingredients = RecipeUtil.readIngredients(json, "ingredients", 1, MAX_INGREDIENTS);
            Ingredient container = json.has("container") ? Ingredient.fromJson(json.get("container")) : Ingredient.EMPTY;
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int time = GsonHelper.getAsInt(json, "mixingtime", 100);
            return new MixingRecipe(id, ingredients, container, result, time);
        }

        @Override
        public MixingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> ingredients = RecipeUtil.readIngredients(buffer);
            Ingredient container = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int time = buffer.readVarInt();
            return new MixingRecipe(id, ingredients, container, result, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MixingRecipe recipe) {
            RecipeUtil.writeIngredients(buffer, recipe.ingredients);
            recipe.container.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.mixingTime);
        }
    }
}
