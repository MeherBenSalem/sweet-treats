package com.sugarplumstudios.sweettreats.fabric;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import java.util.function.Supplier;

/**
 * The Fabric side of what Forge reads from data/sweettreats/loot_modifiers and data/sweettreats/forge/biome_modifier:
 * seeds from grass, cherries from cherry leaves, and wild plant patches.
 */
final class FabricWorld {
    private FabricWorld() {
    }

    static void register() {
        registerLoot();
        registerWorldgen();
    }

    private static void registerLoot() {
        Identifier grass = Identifier.withDefaultNamespace("blocks/short_grass");
        Identifier tallGrass = Identifier.withDefaultNamespace("blocks/tall_grass");
        Identifier fern = Identifier.withDefaultNamespace("blocks/fern");
        Identifier cherryLeaves = Identifier.withDefaultNamespace("blocks/cherry_leaves");
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            Identifier id = key.identifier();
            // The config is read when the loot tables load (Forge reads it on every drop).
            if (SweetTreatsConfig.grassSeedDrops()) {
                if (id.equals(grass)) {
                    table.withPool(seeds(0.07F));
                } else if (id.equals(tallGrass)) {
                    table.withPool(seeds(0.08F));
                } else if (id.equals(fern)) {
                    table.withPool(seeds(0.05F));
                }
            }
            if (SweetTreatsConfig.cherryLeafDrops() && id.equals(cherryLeaves)) {
                table.withPool(entry(entry(pool(0.06F), ModItems.CHERRY, 3), ModItems.CHERRY_PIT, 1));
            }
        });
    }

    private static LootPool.Builder seeds(float chance) {
        LootPool.Builder pool = pool(chance);
        entry(pool, ModItems.STRAWBERRY_SEEDS, 5);
        entry(pool, ModItems.LAVENDER_SEEDS, 4);
        entry(pool, ModItems.VANILLA_SEEDS, 2);
        entry(pool, ModItems.PEACH_PIT, 1);
        entry(pool, ModItems.CHERRY_PIT, 1);
        return pool;
    }

    private static LootPool.Builder pool(float chance) {
        float effective = (float) Math.min(1.0D, chance * SweetTreatsConfig.dropChanceMultiplier());
        return LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(effective));
    }

    /** Adds a weighted item entry (26.3 has no public LootPoolSingletonContainer builder type to name). */
    private static LootPool.Builder entry(LootPool.Builder pool, Supplier<? extends Item> item, int weight) {
        return pool.add(LootItem.lootTableItem(item.get()).setWeight(weight));
    }

    private static void registerWorldgen() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.MEADOW, Biomes.FLOWER_FOREST, Biomes.PLAINS,
                        Biomes.SUNFLOWER_PLAINS, Biomes.CHERRY_GROVE),
                GenerationStep.Decoration.VEGETAL_DECORATION, placed("patch_wild_lavender"));
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.FOREST,
                        Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.MEADOW),
                GenerationStep.Decoration.VEGETAL_DECORATION, placed("patch_wild_strawberries"));
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_JUNGLE),
                GenerationStep.Decoration.VEGETAL_DECORATION, placed("patch_wild_vanilla"));
    }

    private static ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, SweetTreats.id(name));
    }
}
