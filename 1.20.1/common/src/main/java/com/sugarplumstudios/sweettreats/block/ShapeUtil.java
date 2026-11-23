package com.sugarplumstudios.sweettreats.block;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Rotates north-facing shapes so they match blockstate y-rotations (east = 90, south = 180, west = 270).
 */
public final class ShapeUtil {
    private ShapeUtil() {
    }

    public static VoxelShape rotate(VoxelShape northShape, Direction facing) {
        if (facing == Direction.NORTH || facing.getAxis() == Direction.Axis.Y) {
            return northShape;
        }
        VoxelShape[] result = new VoxelShape[]{Shapes.empty()};
        northShape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            VoxelShape box = switch (facing) {
                case SOUTH -> Shapes.box(1 - x2, y1, 1 - z2, 1 - x1, y2, 1 - z1);
                case EAST -> Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2);
                case WEST -> Shapes.box(z1, y1, 1 - x2, z2, y2, 1 - x1);
                default -> Shapes.box(x1, y1, z1, x2, y2, z2);
            };
            result[0] = Shapes.or(result[0], box);
        });
        return result[0].optimize();
    }

    public static Map<Direction, VoxelShape> allHorizontal(VoxelShape northShape) {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            map.put(direction, rotate(northShape, direction));
        }
        return map;
    }
}
