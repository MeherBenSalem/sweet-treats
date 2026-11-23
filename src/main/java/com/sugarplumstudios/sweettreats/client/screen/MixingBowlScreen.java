package com.sugarplumstudios.sweettreats.client.screen;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.menu.MixingBowlMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MixingBowlScreen extends AbstractContainerScreen<MixingBowlMenu> {
    private static final ResourceLocation TEXTURE = SweetTreats.id("textures/gui/mixing_bowl.png");

    public MixingBowlScreen(MixingBowlMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(MixingBowlMenu.CONTAINER_X, MixingBowlMenu.CONTAINER_Y, 16, 16, mouseX, mouseY)
                && menu.getCarried().isEmpty() && !menu.slots.get(6).hasItem()) {
            graphics.renderTooltip(font, Component.translatable("gui.sweettreats.mixing_bowl.container_hint"), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getMixProgress(24);
        graphics.blit(TEXTURE, leftPos + 88, topPos + 35, 176, 0, progress + 1, 16);
    }
}
