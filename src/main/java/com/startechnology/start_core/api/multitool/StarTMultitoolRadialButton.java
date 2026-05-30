package com.startechnology.start_core.api.multitool;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class StarTMultitoolRadialButton {
    final int x, y, width, height;
    final Component label;

    private static final int ICON_SIZE = 16;
    private static final int COLOR_DEFAULT = 0x99101418;
    private static final int COLOR_ACTIVE  = 0x88EFC75E;

    private static final ItemStack BARRIER_STACK = new ItemStack(Items.BARRIER);

    StarTMultitoolRadialButton(int x, int y, int width, int height, Component label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label;
    }

    boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    void render(GuiGraphics graphics, net.minecraft.client.gui.Font font, double mouseX, double mouseY, boolean active) {
        boolean hovered = isHovered(mouseX, mouseY);
        int bg = hovered ? (active ? COLOR_DEFAULT : COLOR_ACTIVE) : (active ? COLOR_ACTIVE : COLOR_DEFAULT);

        // background fill
        graphics.fill(x, y, x + width, y + height, bg);

        // barrier icon centred in the button square
        int iconX = x + (width - ICON_SIZE) / 2;
        int iconY = y + (height - ICON_SIZE) / 2;
        graphics.renderItem(BARRIER_STACK, iconX, iconY);

        // label rendered below the button
        graphics.drawCenteredString(font, label, x + width / 2, y + height + 4, 0xFFFFFFFF);
    }
}