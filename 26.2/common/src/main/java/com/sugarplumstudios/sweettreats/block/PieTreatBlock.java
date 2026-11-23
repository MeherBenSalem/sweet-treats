package com.sugarplumstudios.sweettreats.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A 4-slice pie. Each bite removes one quarter (north-east, south-east, south-west, then the last one).
 */
public class PieTreatBlock extends SliceableTreatBlock {
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 3);
    private static final VoxelShape NW = Block.box(1, 0, 1, 8, 4, 8);
    private static final VoxelShape NE = Block.box(8, 0, 1, 15, 4, 8);
    private static final VoxelShape SE = Block.box(8, 0, 8, 15, 4, 15);
    private static final VoxelShape SW = Block.box(1, 0, 8, 8, 4, 15);
    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Shapes.or(NW, NE, SE, SW),
            Shapes.or(NW, SE, SW),
            Shapes.or(NW, SW),
            NW};

    public PieTreatBlock(Properties properties, int nutritionPerSlice, float saturationPerSlice) {
        super(properties, nutritionPerSlice, saturationPerSlice);
    }

    @Override
    public IntegerProperty bitesProperty() {
        return BITES;
    }

    @Override
    public int maxBites() {
        return 3;
    }

    @Override
    protected VoxelShape[] shapes() {
        return SHAPES;
    }
}
