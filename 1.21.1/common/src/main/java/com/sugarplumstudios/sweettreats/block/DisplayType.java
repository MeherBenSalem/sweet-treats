package com.sugarplumstudios.sweettreats.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Describes a food-presentation block: how many treats it holds, its shape and where each treat is drawn.
 * Positions are in pixels (0-16) for a north-facing block; the renderer rotates them with the block.
 */
public enum DisplayType {
    CAKE_DISPLAY(1, 1, Shapes.or(Block.box(0, 0, 0, 16, 2, 16), Block.box(1, 2, 1, 15, 15, 15)),
            List.of(new Spot(8, 2, 8, 0.55F, Pose.SPINNING, 0.8F))),
    BAKERY_COUNTER(1, 1, Shapes.block(),
            List.of(new Spot(8, 16, 8, 0.6F, Pose.FLAT, 0.85F))),
    CUPCAKE_STAND(3, 1, Shapes.or(
            Block.box(6, 0, 6, 10, 1, 10),
            Block.box(7, 1, 7, 9, 12, 9),
            Block.box(1, 2, 1, 15, 3, 15),
            Block.box(3, 7, 3, 13, 8, 13),
            Block.box(5, 12, 5, 11, 13, 11)),
            List.of(new Spot(8, 3, 4.5F, 0.34F, Pose.UPRIGHT, 0.3F),
                    new Spot(8, 8, 5.5F, 0.32F, Pose.UPRIGHT, 0.28F),
                    new Spot(8, 13, 8, 0.3F, Pose.UPRIGHT, 0.26F))),
    DESSERT_PLATE(1, 1, Block.box(2, 0, 2, 14, 1.5, 14),
            List.of(new Spot(8, 1, 8, 0.55F, Pose.FLAT, 0.6F))),
    GLASS_JAR(1, 64, Block.box(4, 0, 4, 12, 12, 12),
            List.of(new Spot(8, 0.6F, 8, 0.38F, Pose.PILE, 0.4F)));

    private final int slots;
    private final int maxPerSlot;
    private final VoxelShape shape;
    private final List<Spot> spots;

    DisplayType(int slots, int maxPerSlot, VoxelShape shape, List<Spot> spots) {
        this.slots = slots;
        this.maxPerSlot = maxPerSlot;
        this.shape = shape;
        this.spots = spots;
    }

    public int slots() {
        return slots;
    }

    public int maxPerSlot() {
        return maxPerSlot;
    }

    public VoxelShape shape() {
        return shape;
    }

    public Spot spot(int slot) {
        return spots.get(Math.min(slot, spots.size() - 1));
    }

    public enum Pose {
        /** Standing up, facing the front of the block. */
        UPRIGHT,
        /** Lying flat on the surface. */
        FLAT,
        /** Standing up and slowly turning, like a showcase. */
        SPINNING,
        /** Several copies piled up flat (glass jars), depending on the stack size. */
        PILE
    }

    /**
     * @param x          pixel position (centre)
     * @param y          pixel height of the surface the treat stands on
     * @param z          pixel position (centre)
     * @param itemScale  scale for regular items
     * @param pose       how the item is posed
     * @param blockScale scale used when the treat is a placeable cake/pie (rendered as a mini block)
     */
    public record Spot(float x, float y, float z, float itemScale, Pose pose, float blockScale) {
    }
}
