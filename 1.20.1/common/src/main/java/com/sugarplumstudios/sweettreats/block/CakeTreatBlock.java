package com.sugarplumstudios.sweettreats.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A 7-slice layer cake (same shape as the vanilla cake).
 */
public class CakeTreatBlock extends SliceableTreatBlock {
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 6);
    private static final VoxelShape[] SHAPES = new VoxelShape[7];

    static {
        for (int bites = 0; bites < 7; bites++) {
            SHAPES[bites] = Block.box(1 + bites * 2, 0, 1, 15, 8, 15);
        }
    }

    public CakeTreatBlock(Properties properties, int nutritionPerSlice, float saturationPerSlice) {
        super(properties, nutritionPerSlice, saturationPerSlice);
    }

    @Override
    public IntegerProperty bitesProperty() {
        return BITES;
    }

    @Override
    public int maxBites() {
        return 6;
    }

    @Override
    protected VoxelShape[] shapes() {
        return SHAPES;
    }
}
