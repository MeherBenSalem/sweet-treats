package com.sugarplumstudios.sweettreats.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.block.DisplayBlock;
import com.sugarplumstudios.sweettreats.block.DisplayType;
import com.sugarplumstudios.sweettreats.block.SliceableTreatBlock;
import com.sugarplumstudios.sweettreats.blockentity.DisplayBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the treats placed on cake displays, counters, cupcake stands, plates and in glass jars.
 * Placeable cakes and pies are drawn as small cakes from their item model (block entity renderers can no longer
 * draw a single block model since 1.21.9).
 */
public class DisplayBlockRenderer implements BlockEntityRenderer<DisplayBlockEntity, DisplayBlockRenderer.State> {
    /** The most slots any display has. */
    private static final int MAX_SLOTS = 16;

    private final ItemModelResolver itemModelResolver;

    public DisplayBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    /** What the renderer needs from a display, captured once per frame. */
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        DisplayType type = DisplayType.CAKE_DISPLAY;
        int light;
        boolean animate;
        float time;
        int slots;
        final ItemStackRenderState[] items = new ItemStackRenderState[MAX_SLOTS];
        final boolean[] treats = new boolean[MAX_SLOTS];
        final int[] counts = new int[MAX_SLOTS];

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
    public void extractRenderState(DisplayBlockEntity display, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(display, state, partialTicks, cameraPosition, breakProgress);
        Level level = display.getLevel();
        BlockState block = display.getBlockState();
        state.facing = block.hasProperty(DisplayBlock.FACING) ? block.getValue(DisplayBlock.FACING) : Direction.NORTH;
        state.type = display.getDisplayType();
        BlockPos pos = display.getBlockPos();
        state.light = state.type == DisplayType.BAKERY_COUNTER && level != null
                ? LightCoordsUtil.getLightCoords(level, pos.above()) : state.lightCoords;
        state.animate = SweetTreatsConfig.cookingAnimations();
        state.time = level == null ? 0.0F : level.getGameTime() + partialTicks;
        state.slots = level == null ? 0 : Math.min(MAX_SLOTS, display.getItems().size());
        int seed = (int) pos.asLong();
        for (int slot = 0; slot < state.slots; slot++) {
            ItemStack stack = display.getItems().get(slot);
            state.treats[slot] = stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SliceableTreatBlock;
            state.counts[slot] = stack.getCount();
            itemModelResolver.updateForTopItem(state.items[slot], stack, ItemDisplayContext.FIXED, level, null, seed + slot);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int slot = 0; slot < state.slots; slot++) {
            ItemStackRenderState item = state.items[slot];
            if (item.isEmpty()) {
                continue;
            }
            DisplayType.Spot spot = state.type.spot(slot);
            pose.pushPose();
            // Rotate the whole layout with the block.
            pose.translate(0.5D, 0.0D, 0.5D);
            pose.rotate(Axis.YP.rotationDegrees(-state.facing.toYRot() + 180.0F));
            pose.translate(-0.5D, 0.0D, -0.5D);
            pose.translate(spot.x() / 16.0D, spot.y() / 16.0D, spot.z() / 16.0D);

            if (state.treats[slot]) {
                submitMiniTreat(item, spot, state.animate && spot.pose() == DisplayType.Pose.SPINNING, state.time, pose, collector, state.light);
            } else {
                submitItem(item, state.counts[slot], spot, state.animate, state.time, pose, collector, state.light);
            }
            pose.popPose();
        }
    }

    private static void submitMiniTreat(ItemStackRenderState item, DisplayType.Spot spot, boolean spin, float time,
                                        PoseStack pose, SubmitNodeCollector collector, int light) {
        float scale = spot.blockScale();
        if (spin) {
            pose.rotate(Axis.YP.rotationDegrees(time * 1.5F));
        }
        // A cake's item model is its block model, drawn centred and at half size in the FIXED context: double the
        // scale to match the old mini block and lift it so the bottom of the cake sits on the surface.
        pose.translate(0.0D, scale / 2.0D, 0.0D);
        pose.scale(scale * 2.0F, scale * 2.0F, scale * 2.0F);
        item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }

    private static void submitItem(ItemStackRenderState item, int count, DisplayType.Spot spot, boolean animate, float time,
                                   PoseStack pose, SubmitNodeCollector collector, int light) {
        float scale = spot.itemScale();
        switch (spot.pose()) {
            case FLAT -> {
                pose.translate(0.0D, 0.02D, 0.0D);
                pose.rotate(Axis.XP.rotationDegrees(90.0F));
                pose.scale(scale, scale, scale);
                item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
            }
            case UPRIGHT, SPINNING -> {
                pose.translate(0.0D, scale / 2.0D, 0.0D);
                if (spot.pose() == DisplayType.Pose.SPINNING && animate) {
                    pose.rotate(Axis.YP.rotationDegrees(time * 1.5F));
                }
                pose.scale(scale, scale, scale);
                item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
            }
            case PILE -> {
                // One layer per 8 items, up to 8 layers, gently fanned out.
                int layers = Math.min(8, 1 + (count - 1) / 8);
                for (int i = 0; i < layers; i++) {
                    pose.pushPose();
                    pose.translate(0.0D, 0.02D + i * 0.07D, 0.0D);
                    pose.rotate(Axis.YP.rotationDegrees(i * 47.0F));
                    pose.rotate(Axis.XP.rotationDegrees(90.0F));
                    pose.scale(scale, scale, scale);
                    item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
                    pose.popPose();
                }
            }
        }
    }
}
