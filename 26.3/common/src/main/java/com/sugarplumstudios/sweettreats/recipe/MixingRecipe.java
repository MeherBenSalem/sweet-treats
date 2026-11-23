package com.sugarplumstudios.sweettreats.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

import java.util.List;
import java.util.Optional;

/**
 * Mixing Bowl recipe: up to 6 shapeless ingredients, optionally poured into a container (glass bottle, teacup...).
 * <pre>{"type": "sweettreats:mixing", "ingredients": [...], "container": {...}, "result": {...}, "mixingtime": 100}</pre>
 * The input passed to {@link #matches} holds the 6 ingredient slots followed by the container slot.
 */
public record MixingRecipe(List<Ingredient> ingredients, Optional<Ingredient> container, ItemStackTemplate result, int mixingTime)
        implements Recipe<SweetInput> {
    public static final int MAX_INGREDIENTS = 6;
    public static final int CONTAINER_SLOT = 6;

    public static final MapCodec<MixingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecipeUtil.ingredientList(1, MAX_INGREDIENTS).fieldOf("ingredients").forGetter(MixingRecipe::ingredients),
            Ingredient.CODEC.optionalFieldOf("container").forGetter(MixingRecipe::container),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(MixingRecipe::result),
            Codec.INT.optionalFieldOf("mixingtime", 100).forGetter(MixingRecipe::mixingTime)
    ).apply(i, MixingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_INGREDIENTS)), MixingRecipe::ingredients,
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), MixingRecipe::container,
            ItemStackTemplate.STREAM_CODEC, MixingRecipe::result,
            ByteBufCodecs.VAR_INT, MixingRecipe::mixingTime,
            MixingRecipe::new);

    public static RecipeSerializer<MixingRecipe> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }

    @Override
    public boolean matches(SweetInput inv, Level level) {
        if (RecipeUtil.allocate(ingredients, BakingRecipe.contents(inv, MAX_INGREDIENTS)) == null) {
            return false;
        }
        if (container.isEmpty()) {
            return true;
        }
        return inv.size() > CONTAINER_SLOT && container.get().test(inv.getItem(CONTAINER_SLOT));
    }

    @Override
    public ItemStack assemble(SweetInput inv) {
        return result.create();
    }

    public ItemStack getResult() {
        return result.create();
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /** The container that is consumed; only call when {@link #needsContainer()}. */
    public Ingredient getContainer() {
        return container.orElseThrow();
    }

    /** True when {@code stack} is the container this recipe needs. */
    public boolean acceptsContainer(ItemStack stack) {
        return container.isPresent() && container.get().test(stack);
    }

    public boolean needsContainer() {
        return container.isPresent();
    }

    public int getMixingTime() {
        return mixingTime;
    }

    public int getScaledMixingTime() {
        return Math.max(1, (int) Math.round(mixingTime / SweetTreatsConfig.mixingSpeedMultiplier()));
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
    public RecipeSerializer<MixingRecipe> getSerializer() {
        return ModRecipes.MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<MixingRecipe> getType() {
        return ModRecipes.MIXING.get();
    }
}
