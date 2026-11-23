package com.sugarplumstudios.sweettreats.client.screen;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.menu.DecorationStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DecorationStationScreen extends AbstractContainerScreen<DecorationStationMenu> {
    private static final ResourceLocation TEXTURE = SweetTreats.id("textures/gui/decoration_station.png");

    public DecorationStationScreen(DecorationStationMenu menu, Inventory inventory, Component title) {
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
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.hasInvalidInputs()) {
            graphics.blit(TEXTURE, leftPos + 92, topPos + 35, 176, 0, 16, 16);
        }
    }
}
