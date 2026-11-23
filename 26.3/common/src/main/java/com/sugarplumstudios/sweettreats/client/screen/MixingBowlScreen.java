package com.sugarplumstudios.sweettreats.client.screen;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.menu.MixingBowlMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MixingBowlScreen extends AbstractContainerScreen<MixingBowlMenu> {
    private static final Identifier TEXTURE = SweetTreats.id("textures/gui/mixing_bowl.png");

    public MixingBowlScreen(MixingBowlMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (isHovering(MixingBowlMenu.CONTAINER_X, MixingBowlMenu.CONTAINER_Y, 16, 16, mouseX, mouseY)
                && menu.getCarried().isEmpty() && !menu.slots.get(6).hasItem()) {
            graphics.setTooltipForNextFrame(font, Component.translatable("gui.sweettreats.mixing_bowl.container_hint"), mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0F, 0F, imageWidth, imageHeight, 256, 256);
        int progress = menu.getMixProgress(24);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 88, topPos + 35, 176F, 0F, progress + 1, 16, 256, 256);
    }
}
