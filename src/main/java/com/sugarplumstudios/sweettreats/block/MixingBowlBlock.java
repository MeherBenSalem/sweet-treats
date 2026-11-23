package com.sugarplumstudios.sweettreats.block;

import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.blockentity.MixingBowlBlockEntity;
import com.sugarplumstudios.sweettreats.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * The Mixing Bowl block. Ingredients swirl inside it while they are being mixed.
 */
public class MixingBowlBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.join(
            Shapes.or(Block.box(3, 1, 3, 13, 6, 13), Block.box(4, 0, 4, 12, 1, 12)),
            Block.box(4, 2, 4, 12, 6, 12),
            BooleanOp.ONLY_FIRST);

    public MixingBowlBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixingBowlBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.MIXING_BOWL.get(), MixingBowlBlockEntity::serverTick);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, bowl, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl) {
            bowl.dropContents();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!SweetTreatsConfig.cookingParticles() || random.nextInt(3) != 0) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl && bowl.isMixing()) {
            level.addParticle(ParticleTypes.WHITE_ASH,
                    pos.getX() + 0.3D + random.nextDouble() * 0.4D, pos.getY() + 0.45D, pos.getZ() + 0.3D + random.nextDouble() * 0.4D,
                    0.0D, 0.02D, 0.0D);
        }
    }
}
