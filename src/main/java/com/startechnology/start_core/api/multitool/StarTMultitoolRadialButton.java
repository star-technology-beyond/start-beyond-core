package com.startechnology.start_core.api.multitool;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StarTMultitoolRadialButton {
    final int x, y, width, height;
    final Component label;

    private static final int ICON_SIZE = 16;
    private static final int COL_DEFAULT = 0x99101418;
    private static final int COL_ACTIVE = 0x88EFC75E;

    final ItemStack icon;

    StarTMultitoolRadialButton(int x, int y, int width, int height,
            ItemStack icon, Component label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.icon = icon;
        this.label = label;
    }

    boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    void render(GuiGraphics graphics, Font font, double mouseX, double mouseY, boolean active) {
        boolean hovered = isHovered(mouseX, mouseY);
        int bg = hovered ? (active ? COL_DEFAULT : COL_ACTIVE)
                : (active ? COL_ACTIVE : COL_DEFAULT);

        graphics.fill(x, y, x + width, y + height, bg);

        int iconX = x + (width - ICON_SIZE) / 2;
        int iconY = y + (height - ICON_SIZE) / 2;
        graphics.renderItem(icon, iconX, iconY);

        graphics.drawCenteredString(font, label, x + width / 2, y + height + 4, 0xFFFFFFFF);
    }
}