package com.sugarplumstudios.sweettreats.client.screen;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.menu.BakeryOvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BakeryOvenScreen extends AbstractContainerScreen<BakeryOvenMenu> {
    private static final ResourceLocation TEXTURE = SweetTreats.id("textures/gui/bakery_oven.png");

    public BakeryOvenScreen(BakeryOvenMenu menu, Inventory inventory, Component title) {
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
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        if (menu.isLit()) {
            int flame = menu.getLitProgress(13);
            graphics.blit(TEXTURE, x + 13, y + 36 + 12 - flame, 176, 12 - flame, 14, flame + 1);
        }
        int arrow = menu.getBakeProgress(24);
        graphics.blit(TEXTURE, x + 88, y + 34, 176, 14, arrow + 1, 16);
    }
}
