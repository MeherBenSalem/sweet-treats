package com.sugarplumstudios.sweettreats.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.blockentity.MixingBowlBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the ingredients inside the mixing bowl. While mixing they swirl around and bob, like they are being stirred.
 */
public class MixingBowlRenderer implements BlockEntityRenderer<MixingBowlBlockEntity, MixingBowlRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public MixingBowlRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    /** What the renderer needs from a bowl, captured once per frame. */
    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState[] items = new ItemStackRenderState[MixingBowlBlockEntity.INPUT_COUNT];
        int count;
        boolean swirling;
        float time;

        State() {
            for (int i = 0; i < items.length; i++) {
                items[i] = new ItemStackRenderState();
            }
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MixingBowlBlockEntity bowl, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(bowl, state, partialTicks, cameraPosition, breakProgress);
        Level level = bowl.getLevel();
        List<ItemStack> shown = new ArrayList<>();
        for (int i = 0; i < MixingBowlBlockEntity.INPUT_COUNT; i++) {
            if (!bowl.getItem(i).isEmpty()) {
                shown.add(bowl.getItem(i));
            }
        }
        if (shown.isEmpty() && !bowl.getItem(MixingBowlBlockEntity.OUTPUT_SLOT).isEmpty()) {
            shown.add(bowl.getItem(MixingBowlBlockEntity.OUTPUT_SLOT));
        }
        state.count = level == null ? 0 : shown.size();
        state.swirling = bowl.isMixing() && SweetTreatsConfig.cookingAnimations();
        state.time = level == null ? 0.0F : level.getGameTime() + partialTicks;
        int seed = (int) bowl.getBlockPos().asLong();
        for (int i = 0; i < state.count; i++) {
            itemModelResolver.updateForTopItem(state.items[i], shown.get(i), ItemDisplayContext.FIXED, level, null, seed + i);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float spin = state.swirling ? state.time * 9.0F : 0.0F;
        for (int i = 0; i < state.count; i++) {
            float angle = spin + i * (360.0F / state.count);
            float radius = state.count == 1 ? 0.0F : 0.12F;
            float bob = state.swirling ? Mth.sin((state.time + i * 7) * 0.4F) * 0.015F : 0.0F;
            pose.pushPose();
            pose.translate(0.5D, 2.2D / 16.0D + i * 0.012D + bob, 0.5D);
            pose.mulPose(Axis.YP.rotationDegrees(angle));
            pose.translate(radius, 0.0D, 0.0D);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.scale(0.32F, 0.32F, 0.32F);
            state.items[i].submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
