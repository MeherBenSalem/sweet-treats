package com.sugarplumstudios.sweettreats.block;

import net.minecraft.world.item.ItemStack;
import com.sugarplumstudios.sweettreats.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Purely decorative bakery blocks: the tea set, the wall-mounted bakery sign and the chalkboard menu
 * (which also opens the recipe book when clicked).
 */
public class DecorBlock extends Block {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public enum Kind {
        TEA_SET(Shapes.or(Block.box(1, 0, 1, 15, 1, 15), Block.box(3, 1, 6, 12, 8, 11),
                Block.box(11, 1, 2, 14, 3, 5), Block.box(2, 1, 11, 5, 3, 14)), false),
        BAKERY_SIGN(Block.box(0, 3, 14, 16, 13, 16), true),
        MENU_BOARD(Shapes.or(Block.box(1, 4, 7, 15, 16, 8), Block.box(2, 0, 7, 14, 4, 8),
                Block.box(7.5, 0, 8, 8.5, 13, 11)), false);

        private final VoxelShape northShape;
        private final boolean wallMounted;

        Kind(VoxelShape northShape, boolean wallMounted) {
            this.northShape = northShape;
            this.wallMounted = wallMounted;
        }
    }

    private final Kind kind;
    private final Map<Direction, VoxelShape> shapes;

    public DecorBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        this.shapes = ShapeUtil.allHorizontal(kind.northShape);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        if (kind.wallMounted && clicked.getAxis().isHorizontal()) {
            return defaultBlockState().setValue(FACING, clicked);
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        return ShapeUtil.itemResult(interact(state, level, pos, player, hand, hit));
    }

    private InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind != Kind.MENU_BOARD) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
            ModNetwork.openRecipeBook(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
