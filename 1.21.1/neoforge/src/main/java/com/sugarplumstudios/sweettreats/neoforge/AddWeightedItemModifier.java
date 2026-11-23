package com.sugarplumstudios.sweettreats.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.List;

/**
 * Adds one weighted-random item to a loot table with a (configurable) chance.
 * Used for seeds from grass and cherries from cherry leaves.
 * <pre>{"type": "sweettreats:add_weighted_item", "conditions": [...], "toggle": "grass_seeds", "chance": 0.08,
 *  "entries": [{"item": "sweettreats:strawberry_seeds", "weight": 4}]}</pre>
 * {@code toggle} is either {@code grass_seeds} or {@code cherry_leaves} and maps to a config switch.
 */
public class AddWeightedItemModifier extends LootModifier {
    public static final MapCodec<AddWeightedItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
            .and(Codec.STRING.fieldOf("toggle").forGetter(m -> m.toggle))
            .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
            .and(Entry.CODEC.listOf().fieldOf("entries").forGetter(m -> m.entries))
            .apply(inst, AddWeightedItemModifier::new));

    private final String toggle;
    private final float chance;
    private final List<Entry> entries;
    private final int totalWeight;

    public AddWeightedItemModifier(LootItemCondition[] conditions, String toggle, float chance, List<Entry> entries) {
        super(conditions);
        this.toggle = toggle;
        this.chance = chance;
        this.entries = entries;
        this.totalWeight = entries.stream().mapToInt(Entry::weight).sum();
    }

    private boolean enabled() {
        return switch (toggle) {
            case "grass_seeds" -> SweetTreatsConfig.grassSeedDrops();
            case "cherry_leaves" -> SweetTreatsConfig.cherryLeafDrops();
            default -> true;
        };
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (totalWeight <= 0 || !enabled()) {
            return loot;
        }
        double effectiveChance = chance * SweetTreatsConfig.dropChanceMultiplier();
        if (context.getRandom().nextDouble() >= effectiveChance) {
            return loot;
        }
        int roll = context.getRandom().nextInt(totalWeight);
        for (Entry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                loot.add(new ItemStack(entry.item(), entry.count()));
                break;
            }
        }
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public record Entry(Item item, int weight, int count) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Entry::item),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(Entry::weight),
                Codec.INT.optionalFieldOf("count", 1).forGetter(Entry::count)
        ).apply(inst, Entry::new));
    }
}
