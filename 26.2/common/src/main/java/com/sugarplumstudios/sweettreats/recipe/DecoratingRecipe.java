package com.sugarplumstudios.sweettreats.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
 * Decoration Station recipe: a base treat plus one or two toppings, decorated instantly.
 * <pre>{"type": "sweettreats:decorating", "base": {...}, "toppings": [...], "result": {...}}</pre>
 * The input passed to {@link #matches} holds: slot 0 = base, slots 1-2 = toppings.
 */
public record DecoratingRecipe(Ingredient base, List<Ingredient> toppings, ItemStackTemplate result) implements Recipe<SweetInput> {
    public static final int MAX_TOPPINGS = 2;

    public static final MapCodec<DecoratingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("base").forGetter(DecoratingRecipe::base),
            RecipeUtil.ingredientList(1, MAX_TOPPINGS).fieldOf("toppings").forGetter(DecoratingRecipe::toppings),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(DecoratingRecipe::result)
    ).apply(i, DecoratingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DecoratingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, DecoratingRecipe::base,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_TOPPINGS)), DecoratingRecipe::toppings,
            ItemStackTemplate.STREAM_CODEC, DecoratingRecipe::result,
            DecoratingRecipe::new);

    public static RecipeSerializer<DecoratingRecipe> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    @Override
    public boolean matches(SweetInput inv, Level level) {
        return inv.size() >= 3 && base.test(inv.getItem(0))
                && RecipeUtil.allocate(toppings, List.of(inv.getItem(1), inv.getItem(2))) != null;
    }

    /** How many items to take from topping slots 1 and 2, or null. */
    public int[] toppingAllocation(Container inv) {
        List<ItemStack> slots = List.of(inv.getItem(1), inv.getItem(2));
        return RecipeUtil.allocate(toppings, slots);
    }

    @Override
    public ItemStack assemble(SweetInput inv) {
        return result.create();
    }

    public ItemStack getResult() {
        return result.create();
    }

    public Ingredient getBase() {
        return base;
    }

    public List<Ingredient> getToppings() {
        return toppings;
    }

    /** The base followed by the toppings. */
    public List<Ingredient> getIngredients() {
        List<Ingredient> all = new ArrayList<>(toppings.size() + 1);
        all.add(base);
        all.addAll(toppings);
        return all;
    }

    @Override
    public boolean isSpecial() {
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
    public RecipeSerializer<DecoratingRecipe> getSerializer() {
        return ModRecipes.DECORATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<DecoratingRecipe> getType() {
        return ModRecipes.DECORATING.get();
    }
}
