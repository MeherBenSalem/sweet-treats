package com.sugarplumstudios.sweettreats.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.blockentity.MixingBowlBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the ingredients inside the mixing bowl. While mixing they swirl around and bob, like they are being stirred.
 */
public class MixingBowlRenderer implements BlockEntityRenderer<MixingBowlBlockEntity> {
    private final ItemRenderer itemRenderer;

    public MixingBowlRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(MixingBowlBlockEntity bowl, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Level level = bowl.getLevel();
        if (level == null) {
            return;
        }
        List<ItemStack> shown = new ArrayList<>();
        for (int i = 0; i < MixingBowlBlockEntity.INPUT_COUNT; i++) {
            if (!bowl.getItem(i).isEmpty()) {
                shown.add(bowl.getItem(i));
            }
        }
        if (shown.isEmpty() && !bowl.getItem(MixingBowlBlockEntity.OUTPUT_SLOT).isEmpty()) {
            shown.add(bowl.getItem(MixingBowlBlockEntity.OUTPUT_SLOT));
        }
        if (shown.isEmpty()) {
            return;
        }

        boolean swirling = bowl.isMixing() && SweetTreatsConfig.cookingAnimations();
        float time = level.getGameTime() + partialTick;
        float spin = swirling ? time * 9.0F : 0.0F;
        int seed = (int) bowl.getBlockPos().asLong();

        for (int i = 0; i < shown.size(); i++) {
            float angle = spin + i * (360.0F / shown.size());
            float radius = shown.size() == 1 ? 0.0F : 0.12F;
            float bob = swirling ? Mth.sin((time + i * 7) * 0.4F) * 0.015F : 0.0F;
            pose.pushPose();
            pose.translate(0.5D, 2.2D / 16.0D + i * 0.012D + bob, 0.5D);
            pose.mulPose(Axis.YP.rotationDegrees(angle));
            pose.translate(radius, 0.0D, 0.0D);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.scale(0.32F, 0.32F, 0.32F);
            itemRenderer.renderStatic(shown.get(i), ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, pose, buffers, level, seed + i);
            pose.popPose();
        }
    }
}
