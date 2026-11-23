package com.sugarplumstudios.sweettreats.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

import java.util.List;

/**
 * Decoration Station recipe: a base treat plus one or two toppings, decorated instantly.
 * <pre>{"type": "sweettreats:decorating", "base": {...}, "toppings": [...], "result": {...}}</pre>
 * The container passed to {@link #matches} holds: slot 0 = base, slots 1-2 = toppings.
 */
public class DecoratingRecipe implements Recipe<SweetInput> {
    public static final int MAX_TOPPINGS = 2;
    private final Ingredient base;
    private final NonNullList<Ingredient> toppings;
    private final ItemStack result;

    public DecoratingRecipe(Ingredient base, NonNullList<Ingredient> toppings, ItemStack result) {
        this.base = base;
        this.toppings = toppings;
        this.result = result;
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
    public ItemStack assemble(SweetInput inv, HolderLookup.Provider registries) {
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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DECORATING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DECORATING.get();
    }

    public static class Serializer implements RecipeSerializer<DecoratingRecipe> {
        private static final MapCodec<DecoratingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(r -> r.base),
                RecipeUtil.ingredientList(1, MAX_TOPPINGS).fieldOf("toppings").forGetter(r -> r.toppings),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result)
        ).apply(i, DecoratingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DecoratingRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<DecoratingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DecoratingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static DecoratingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            NonNullList<Ingredient> toppings = RecipeUtil.readIngredients(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new DecoratingRecipe(base, toppings, result);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, DecoratingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
            RecipeUtil.writeIngredients(buffer, recipe.toppings);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}
