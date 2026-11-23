package com.sugarplumstudios.sweettreats.fabric;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
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
        ResourceLocation grass = Blocks.GRASS.getLootTable();
        ResourceLocation tallGrass = Blocks.TALL_GRASS.getLootTable();
        ResourceLocation fern = Blocks.FERN.getLootTable();
        ResourceLocation cherryLeaves = Blocks.CHERRY_LEAVES.getLootTable();
        LootTableEvents.MODIFY.register((resources, lootManager, id, table, source) -> {
            if (!source.isBuiltin()) {
                return;
            }
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
                table.withPool(pool(0.06F).add(entry(ModItems.CHERRY, 3)).add(entry(ModItems.CHERRY_PIT, 1)));
            }
        });
    }

    private static LootPool.Builder seeds(float chance) {
        return pool(chance)
                .add(entry(ModItems.STRAWBERRY_SEEDS, 5))
                .add(entry(ModItems.LAVENDER_SEEDS, 4))
                .add(entry(ModItems.VANILLA_SEEDS, 2))
                .add(entry(ModItems.PEACH_PIT, 1))
                .add(entry(ModItems.CHERRY_PIT, 1));
    }

    private static LootPool.Builder pool(float chance) {
        float effective = (float) Math.min(1.0D, chance * SweetTreatsConfig.dropChanceMultiplier());
        return LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(effective));
    }

    private static LootPoolSingletonContainer.Builder<?> entry(Supplier<? extends Item> item, int weight) {
        return LootItem.lootTableItem(item.get()).setWeight(weight);
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
