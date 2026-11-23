package com.sugarplumstudios.sweettreats.discovery;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.platform.Services;
import com.sugarplumstudios.sweettreats.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks which Sweet Treats recipes each player has discovered.
 * <p>
 * A recipe is discovered the first time the player makes or obtains its result (machine output, crafting, pickup).
 * Data lives with the player (on Forge in the player's persisted NBT) so it survives death and relogging.
 */
public final class RecipeDiscovery {
    public static final String DISCOVERED_KEY = "sweettreats:discovered_recipes";
    public static final String RECEIVED_BOOK_KEY = "sweettreats:received_recipe_book";

    private RecipeDiscovery() {
    }

    public static CompoundTag persistedData(Player player) {
        return Services.PLATFORM.playerData(player);
    }

    public static Set<ResourceLocation> getDiscovered(Player player) {
        ListTag list = persistedData(player).getList(DISCOVERED_KEY, Tag.TAG_STRING);
        Set<ResourceLocation> set = new LinkedHashSet<>();
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                set.add(id);
            }
        }
        return set;
    }

    public static boolean isDiscovered(Player player, ResourceLocation recipeId) {
        return getDiscovered(player).contains(recipeId);
    }

    /** @return true if the recipe was newly discovered */
    public static boolean discover(Player player, ResourceLocation recipeId) {
        CompoundTag data = persistedData(player);
        ListTag list = data.getList(DISCOVERED_KEY, Tag.TAG_STRING);
        String id = recipeId.toString();
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(id)) {
                return false;
            }
        }
        list.add(StringTag.valueOf(id));
        data.put(DISCOVERED_KEY, list);
        Services.PLATFORM.playerDataChanged(player);
        return true;
    }

    /** Every recipe shown in the Sweet Treats recipe book: oven, mixing bowl, decoration station and our crafting recipes. */
    public static List<RecipeHolder<?>> trackedRecipes(RecipeManager manager) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        recipes.addAll(manager.getAllRecipesFor(ModRecipes.BAKING.get()));
        recipes.addAll(manager.getAllRecipesFor(ModRecipes.MIXING.get()));
        recipes.addAll(manager.getAllRecipesFor(ModRecipes.DECORATING.get()));
        manager.getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe.id().getNamespace().equals(SweetTreats.MOD_ID))
                .forEach(recipes::add);
        return recipes;
    }

    /**
     * Discovers every tracked recipe whose result is the given item and tells the player about it.
     */
    public static void discoverByResult(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        RegistryAccess access = player.level().registryAccess();
        boolean anyNew = false;
        for (RecipeHolder<?> recipe : trackedRecipes(player.server.getRecipeManager())) {
            ItemStack result = recipe.value().getResultItem(access);
            if (!result.isEmpty() && result.is(stack.getItem()) && discover(player, recipe.id())) {
                anyNew = true;
            }
        }
        if (anyNew && SweetTreatsConfig.discoveryMessages()) {
            player.displayClientMessage(Component.translatable("message.sweettreats.discovered",
                    stack.getHoverName().copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7F, 1.6F);
        }
    }

    /**
     * Discovers the recipes of every tracked result the player carries. Used where the loader has no crafted or
     * pickup events (Fabric), so treats crafted or picked up are noticed in the inventory instead.
     */
    public static void discoverFromInventory(ServerPlayer player) {
        RegistryAccess access = player.level().registryAccess();
        Set<ResourceLocation> discovered = getDiscovered(player);
        Set<Item> undiscoveredResults = new HashSet<>();
        for (RecipeHolder<?> recipe : trackedRecipes(player.server.getRecipeManager())) {
            if (!discovered.contains(recipe.id())) {
                ItemStack result = recipe.value().getResultItem(access);
                if (!result.isEmpty()) {
                    undiscoveredResults.add(result.getItem());
                }
            }
        }
        if (undiscoveredResults.isEmpty()) {
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && undiscoveredResults.remove(stack.getItem())) {
                discoverByResult(player, stack);
            }
        }
    }
}
