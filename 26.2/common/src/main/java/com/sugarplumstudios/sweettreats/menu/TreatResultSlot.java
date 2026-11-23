package com.sugarplumstudios.sweettreats.menu;

import com.sugarplumstudios.sweettreats.discovery.RecipeDiscovery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Output slot of the oven and mixing bowl. Taking items out discovers their recipes and awards experience.
 */
public class TreatResultSlot extends MachineSlot {
    private final Player player;
    @Nullable
    private final Consumer<Player> onTakeCallback;
    private int removeCount;

    public TreatResultSlot(Player player, Container container, int index, int x, int y, @Nullable Consumer<Player> onTakeCallback) {
        super(container, index, x, y);
        this.player = player;
        this.onTakeCallback = onTakeCallback;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (hasItem()) {
            removeCount += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        checkTakeAchievements(stack);
        super.onTake(player, stack);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        removeCount += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(player, removeCount);
        if (player instanceof ServerPlayer serverPlayer) {
            RecipeDiscovery.discoverByResult(serverPlayer, stack);
            if (onTakeCallback != null) {
                onTakeCallback.accept(serverPlayer);
            }
        }
        removeCount = 0;
    }
}
