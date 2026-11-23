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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Mixing Bowl recipe: 1-6 shapeless ingredients plus an optional container (e.g. a glass bottle for drinks).
 * <pre>{"type": "sweettreats:mixing", "ingredients": [...], "container": {...}, "result": {...}, "mixingtime": 100}</pre>
 * The container passed to {@link #matches} holds the 6 ingredient slots followed by the container slot.
 */
public class MixingRecipe implements Recipe<SweetInput> {
    public static final int MAX_INGREDIENTS = 6;
    public static final int CONTAINER_SLOT = 6;
    private final NonNullList<Ingredient> ingredients;
    private final Ingredient container;
    private final ItemStack result;
    private final int mixingTime;

    public MixingRecipe(NonNullList<Ingredient> ingredients, Ingredient container, ItemStack result, int mixingTime) {
        this.ingredients = ingredients;
        this.container = container;
        this.result = result;
        this.mixingTime = mixingTime;
    }

    @Override
    public boolean matches(SweetInput inv, Level level) {
        if (RecipeUtil.allocate(ingredients, BakingRecipe.contents(inv, MAX_INGREDIENTS)) == null) {
            return false;
        }
        if (container.isEmpty()) {
            return true;
        }
        return inv.size() > CONTAINER_SLOT && container.test(inv.getItem(CONTAINER_SLOT));
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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MIXING.get();
    }

    public static class Serializer implements RecipeSerializer<MixingRecipe> {
        private static final MapCodec<MixingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RecipeUtil.ingredientList(1, MAX_INGREDIENTS).fieldOf("ingredients").forGetter(r -> r.ingredients),
                Ingredient.CODEC_NONEMPTY.optionalFieldOf("container")
                        .forGetter(r -> r.container.isEmpty() ? Optional.empty() : Optional.of(r.container)),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.INT.optionalFieldOf("mixingtime", 100).forGetter(r -> r.mixingTime)
        ).apply(i, (ingredients, container, result, time) ->
                new MixingRecipe(ingredients, container.orElse(Ingredient.EMPTY), result, time)));

        private static final StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<MixingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static MixingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            NonNullList<Ingredient> ingredients = RecipeUtil.readIngredients(buffer);
            Ingredient container = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            int time = buffer.readVarInt();
            return new MixingRecipe(ingredients, container, result, time);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, MixingRecipe recipe) {
            RecipeUtil.writeIngredients(buffer, recipe.ingredients);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.container);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeVarInt(recipe.mixingTime);
        }
    }
}
