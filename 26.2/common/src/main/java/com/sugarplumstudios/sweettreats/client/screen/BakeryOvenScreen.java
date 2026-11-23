package com.sugarplumstudios.sweettreats.client.screen;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.menu.BakeryOvenMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class BakeryOvenScreen extends AbstractContainerScreen<BakeryOvenMenu> {
    private static final Identifier TEXTURE = SweetTreats.id("textures/gui/bakery_oven.png");

    public BakeryOvenScreen(BakeryOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0F, 0F, imageWidth, imageHeight, 256, 256);
        if (menu.isLit()) {
            int flame = menu.getLitProgress(13);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 13, y + 36 + 12 - flame, 176F, 12 - flame, 14, flame + 1, 256, 256);
        }
        int arrow = menu.getBakeProgress(24);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 88, y + 34, 176F, 14F, arrow + 1, 16, 256, 256);
    }
}
