package com.sugarplumstudios.sweettreats.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

/** The slots a Sweet Treats machine offers its recipes, in slot order. */
public record SweetInput(List<ItemStack> items) implements RecipeInput {
    public static SweetInput of(Container container) {
        List<ItemStack> items = new ArrayList<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); i++) {
            items.add(container.getItem(i));
        }
        return new SweetInput(items);
    }

    @Override
    public ItemStack getItem(int index) {
        return index < items.size() ? items.get(index) : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return items.size();
    }
}
