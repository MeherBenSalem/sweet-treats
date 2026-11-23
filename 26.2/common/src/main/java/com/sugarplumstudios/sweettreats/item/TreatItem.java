package com.sugarplumstudios.sweettreats.item;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipDisplay;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A food or drink with optional, config-controlled bonus effects and an optional container
 * (glass bottle, teacup...) that is handed back after it is consumed.
 */
public class TreatItem extends Item {
    private final boolean drink;
    @Nullable
    private final Supplier<? extends Item> container;
    private final List<TreatEffect> effects;

    public TreatItem(Builder builder) {
        super(builder.toProperties());
        this.drink = builder.drink;
        this.container = builder.container;
        this.effects = List.copyOf(builder.effects);
    }

    public static Builder builder(int nutrition, float saturation) {
        return new Builder(nutrition, saturation);
    }

    public boolean isDrink() {
        return drink;
    }

    public List<TreatEffect> getTreatEffects() {
        return effects;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        boolean creative = entity instanceof Player player && player.getAbilities().instabuild;
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && SweetTreatsConfig.foodEffects()) {
            double multiplier = SweetTreatsConfig.effectDurationMultiplier();
            for (TreatEffect effect : effects) {
                entity.addEffect(effect.createInstance(multiplier));
            }
        }

        if (container != null && !creative) {
            ItemStack empty = new ItemStack(container.get());
            if (result.isEmpty()) {
                return empty;
            }
            if (entity instanceof Player player && !player.getInventory().add(empty)) {
                player.drop(empty, false);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        if (!effects.isEmpty() && SweetTreatsConfig.foodEffects()) {
            double multiplier = SweetTreatsConfig.effectDurationMultiplier();
            for (TreatEffect effect : effects) {
                tooltip.accept(describe(effect.createInstance(multiplier), context.tickRate()));
            }
        }
    }

    public static Component describe(MobEffectInstance instance) {
        return describe(instance, 20.0F);
    }

    public static Component describe(MobEffectInstance instance, float tickRate) {
        MutableComponent text = Component.literal("✿ ").append(Component.translatable(instance.getDescriptionId()));
        if (instance.getAmplifier() > 0) {
            text.append(" ").append(Component.translatable("potion.potency." + instance.getAmplifier()));
        }
        text.append(" (").append(MobEffectUtil.formatDuration(instance, 1.0F, tickRate)).append(")");
        return text.withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    public record TreatEffect(Supplier<Holder<MobEffect>> effect, int seconds, int amplifier) {
        public MobEffectInstance createInstance(double durationMultiplier) {
            int ticks = Math.max(20, (int) Math.round(seconds * 20 * durationMultiplier));
            return new MobEffectInstance(effect.get(), ticks, amplifier);
        }
    }

    public static final class Builder {
        private final int nutrition;
        private final float saturation;
        private boolean fast;
        private boolean drink;
        @Nullable
        private Supplier<? extends Item> container;
        private final List<TreatEffect> effects = new ArrayList<>();

        private Builder(int nutrition, float saturation) {
            this.nutrition = nutrition;
            this.saturation = saturation;
        }

        public Builder fast() {
            this.fast = true;
            return this;
        }

        /** Makes this a drink that returns the given container once finished. */
        public Builder drink(Supplier<? extends Item> container) {
            this.drink = true;
            this.container = container;
            return this;
        }

        public Builder effect(Supplier<Holder<MobEffect>> effect, int seconds, int amplifier) {
            this.effects.add(new TreatEffect(effect, seconds, amplifier));
            return this;
        }

        private Item.Properties toProperties() {
            FoodProperties.Builder food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
            if (drink) {
                // Drinks can always be sipped, so their small effects stay useful.
                food.alwaysEdible();
            }
            // Since 1.21.2 how an item is eaten (time, animation, sound) is its consumable component.
            Consumable.Builder use = drink ? Consumables.defaultDrink() : Consumables.defaultFood();
            if (fast) {
                use.consumeSeconds(0.8F);
            }
            return ModItems.props().food(food.build(), use.build()).stacksTo(drink ? 16 : 64);
        }
    }
}
