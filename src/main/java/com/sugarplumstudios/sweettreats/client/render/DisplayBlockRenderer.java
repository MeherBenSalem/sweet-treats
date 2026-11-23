package com.sugarplumstudios.sweettreats.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.block.DisplayBlock;
import com.sugarplumstudios.sweettreats.block.DisplayType;
import com.sugarplumstudios.sweettreats.block.SliceableTreatBlock;
import com.sugarplumstudios.sweettreats.blockentity.DisplayBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Draws the treats placed on cake displays, counters, cupcake stands, plates and in glass jars.
 * Placeable cakes and pies are drawn as tiny real cakes rather than flat icons.
 */
public class DisplayBlockRenderer implements BlockEntityRenderer<DisplayBlockEntity> {
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderer;

    public DisplayBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(DisplayBlockEntity display, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Level level = display.getLevel();
        if (level == null || display.isEmpty()) {
            return;
        }
        BlockState state = display.getBlockState();
        Direction facing = state.hasProperty(DisplayBlock.FACING) ? state.getValue(DisplayBlock.FACING) : Direction.NORTH;
        DisplayType type = display.getDisplayType();
        BlockPos pos = display.getBlockPos();
        int light = type == DisplayType.BAKERY_COUNTER ? LevelRenderer.getLightColor(level, pos.above()) : packedLight;
        boolean animate = SweetTreatsConfig.cookingAnimations();
        float time = level.getGameTime() + partialTick;

        for (int slot = 0; slot < display.getItems().size(); slot++) {
            ItemStack stack = display.getItems().get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            DisplayType.Spot spot = type.spot(slot);
            pose.pushPose();
            // Rotate the whole layout with the block.
            pose.translate(0.5D, 0.0D, 0.5D);
            pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + 180.0F));
            pose.translate(-0.5D, 0.0D, -0.5D);
            pose.translate(spot.x() / 16.0D, spot.y() / 16.0D, spot.z() / 16.0D);

            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SliceableTreatBlock treat) {
                renderMiniTreat(treat, spot, animate && spot.pose() == DisplayType.Pose.SPINNING, time, pose, buffers, light);
            } else {
                renderItem(stack, spot, animate, time, pose, buffers, light, level, (int) pos.asLong() + slot);
            }
            pose.popPose();
        }
    }

    private void renderMiniTreat(SliceableTreatBlock treat, DisplayType.Spot spot, boolean spin, float time,
                                 PoseStack pose, MultiBufferSource buffers, int light) {
        float scale = spot.blockScale();
        if (spin) {
            pose.mulPose(Axis.YP.rotationDegrees(time * 1.5F));
        }
        pose.scale(scale, scale, scale);
        pose.translate(-0.5D, 0.0D, -0.5D);
        blockRenderer.renderSingleBlock(treat.defaultBlockState(), pose, buffers, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
    }

    private void renderItem(ItemStack stack, DisplayType.Spot spot, boolean animate, float time, PoseStack pose,
                            MultiBufferSource buffers, int light, Level level, int seed) {
        float scale = spot.itemScale();
        switch (spot.pose()) {
            case FLAT -> {
                pose.translate(0.0D, 0.02D, 0.0D);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.scale(scale, scale, scale);
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, level, seed);
            }
            case UPRIGHT, SPINNING -> {
                pose.translate(0.0D, scale / 2.0D, 0.0D);
                if (spot.pose() == DisplayType.Pose.SPINNING && animate) {
                    pose.mulPose(Axis.YP.rotationDegrees(time * 1.5F));
                }
                pose.scale(scale, scale, scale);
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, level, seed);
            }
            case PILE -> {
                // One layer per 8 items, up to 8 layers, gently fanned out.
                int layers = Math.min(8, 1 + (stack.getCount() - 1) / 8);
                for (int i = 0; i < layers; i++) {
                    pose.pushPose();
                    pose.translate(0.0D, 0.02D + i * 0.07D, 0.0D);
                    pose.mulPose(Axis.YP.rotationDegrees(i * 47.0F));
                    pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                    pose.scale(scale, scale, scale);
                    itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, level, seed + i);
                    pose.popPose();
                }
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(DisplayBlockEntity display) {
        return false;
    }
}
