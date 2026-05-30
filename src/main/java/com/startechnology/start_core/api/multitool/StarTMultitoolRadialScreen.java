package com.startechnology.start_core.api.multitool;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import com.startechnology.start_core.network.StarTNetwork;
import com.startechnology.start_core.network.packets.CPacketToggleSingleBlockMode;
import com.startechnology.start_core.network.packets.CPacketUninstallMultitoolMode;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.List;

public class StarTMultitoolRadialScreen extends Screen {

    // This is the radius at which the dark for the icon selection
    // begins
    private static final int INNER_RADIUS = 62;

    // This is where the icon sits in the circle
    private static final int ICON_RADIUS = 80;

    // This is the radius for which the dark circle ends
    private static final int OUTER_RADIUS = 98;

    // Size of the icon drawn
    private static final int ICON_SIZE = 16;

    // single block lock button width/height 
    private static final int BUTTON_WIDTH = 26;
    private static final int BUTTON_HEIGHT = 26;

    private final ItemStack stack;
    private final InteractionHand hand;
    private StarTMultitoolMode hoveredMode;
    private StarTMultitoolMode selectedMode;
    private List<StarTMultitoolMode> installedModes;

    protected StarTMultitoolRadialScreen(ItemStack stack, InteractionHand hand) {
        super(Component.translatable("key.start_core.multitool_selector"));
        this.stack = stack;
        this.hand = hand;
        this.installedModes = StarTMultitoolMode.getInstalled(stack);
        this.selectedMode = StarTMultitoolMode.getActive(stack);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (StarTMultitoolClientEvents.getHeldMultitool() == null) {
            onClose();
            return;
        }
        installedModes = StarTMultitoolMode.getInstalled(stack);
        if (installedModes.isEmpty()) {
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // no modes no gui
        if (installedModes.isEmpty())
            return;

        int centerX = width / 2;
        int centerY = height / 2;
        hoveredMode = getMouseMode(centerX, centerY, mouseX, mouseY);

        boolean singleBlock = StarTMultitoolMode.isSingleBlockMode(stack);
        StarTMultitoolRadialButton singleBlockButton = new StarTMultitoolRadialButton(
            buttonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT,
            Component.translatable(
                singleBlock
                    ? "item.start_core.gregtech_multitool.single_block_on"
                    : "item.start_core.gregtech_multitool.single_block_off"
            )
        );

        // full screen background
        graphics.fill(0, 0, width, height, 0x65000000);

        PoseStack pose = graphics.pose();

        // draw the circle segments like
        // effortless building
        pose.pushPose();
        drawModeCircleSegments(pose.last().pose(), centerX, centerY);
        pose.popPose();

        // draw item icons for each mode
        for (StarTMultitoolMode mode : installedModes) {
            double angle = centerAngle(mode);

            // item icon position
            int iconX = centerX + (int) Math.round(Math.cos(angle) * ICON_RADIUS) - ICON_SIZE / 2;
            int iconY = centerY + (int) Math.round(Math.sin(angle) * ICON_RADIUS) - ICON_SIZE / 2;

            ItemStack toolStack = getRepresentativeStack(mode);

            boolean hovered = mode == hoveredMode;
            boolean active = mode == selectedMode;

            // draw a small highlight circle behind the icon if active or hovered
            if (hovered || active) {
                int bgColor = hovered ? 0x77FFAA00 : 0x6658C7F3;
                fillCircle(pose.last().pose(), iconX + ICON_SIZE / 2, iconY + ICON_SIZE / 2, ICON_SIZE / 2 + 4,
                        bgColor);
            }

            // render the item
            if (!toolStack.isEmpty()) {
                graphics.renderItem(toolStack, iconX, iconY);
            }
        }

        // center area to show label & hints
        StarTMultitoolMode labelMode = hoveredMode == null ? selectedMode : hoveredMode;

        if (labelMode != null) {
            boolean isSneaking = hasShiftDown();
            int labelColor = hoveredMode == null ? 0xFFFFFFFF : 0xFFEFC75E;

            // translate the next rendering in the pose
            // 250z up so we render ontop of items while in the pose
            pose.pushPose();
            pose.translate(0, 0, 250);

            // draw the name of the mode
            graphics.drawCenteredString(font, labelMode.displayName(), centerX, centerY - 9, labelColor);

            // helper hint text
            if (isSneaking && hoveredMode != null) {
                graphics.drawCenteredString(font,
                        Component.translatable("key.start_core.multitool_selector.eject")
                                .withStyle(ChatFormatting.RED),
                        centerX, centerY + 4, 0xFFFF5555);
            } else {
                graphics.drawCenteredString(font,
                        Component.translatable("key.start_core.multitool_selector.hint"),
                        centerX, centerY + 4, 0xFFBFC7D5);
            }

            pose.popPose(); 
        }

        singleBlockButton.render(graphics, font, mouseX, mouseY, singleBlock);
    }

    private ItemStack getRepresentativeStack(StarTMultitoolMode mode) {
        try {
            ItemStack result = ToolHelper.get(mode.toolType(), mode.material());
            if (result != null && !result.isEmpty())
                return result;
        } catch (Exception ignored) {
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // radial button for single block mode first as click priority
        boolean singleBlock = StarTMultitoolMode.isSingleBlockMode(stack);
        StarTMultitoolRadialButton singleBlockButton = new StarTMultitoolRadialButton(
            buttonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty()
        );
        if (singleBlockButton.isHovered(mouseX, mouseY)) {
            StarTNetwork.NETWORK.sendToServer(new CPacketToggleSingleBlockMode(hand));
            StarTMultitoolMode.toggleSingleBlockMode(stack);
            return true;
        }


        StarTMultitoolMode mode = getMouseMode(width / 2, height / 2, mouseX, mouseY);
        if (mode != null) {
            if (hasShiftDown()) {
                eject(mode);
                onClose();
            } else {
                select(mode);
                onClose();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button); // Better default fallback
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (installedModes.isEmpty())
            return true;
        StarTMultitoolMode next = StarTMultitoolMode.offset(stack, delta > 0 ? -1 : 1);
        if (next != null)
            select(next);
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // letting to should select the hovered mode
        if (StarTMultitoolClientEvents.OPEN_SELECTOR.matches(keyCode, scanCode)) {
            if (hoveredMode != null) {
                select(hoveredMode);
            }
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void select(StarTMultitoolMode mode) {
        selectedMode = mode;
        StarTMultitoolClientEvents.selectMode(stack, hand, mode);
    }

    private void eject(StarTMultitoolMode mode) {
        StarTMultitoolClientEvents.ejectMode(stack, hand, mode);
    }

    // calculate what mode we're on at the mouse position
    private StarTMultitoolMode getMouseMode(int centerX, int centerY, double mouseX, double mouseY) {
        if (installedModes.isEmpty())
            return null;
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < INNER_RADIUS || distance > OUTER_RADIUS)
            return null;
        double angle = Math.atan2(dy, dx) + Math.PI / 2.0D;
        if (angle < 0)
            angle += Math.PI * 2.0D;
        int index = (int) Math.floor((angle / (Math.PI * 2.0D)) * installedModes.size() + 0.5D);
        return installedModes.get(Math.floorMod(index, installedModes.size()));
    }

    private void drawModeCircleSegments(Matrix4f matrix, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // we need to disable culling/depth test
        // for our polygons to render
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        // draw a segment per mode
        for (StarTMultitoolMode mode : installedModes) {
            int idx = installedModes.indexOf(mode);

            // if the mode is hovered then change the colour
            int color = mode == hoveredMode ? 0x88EFC75E : mode == selectedMode ? 0x7058C7F3 : 0x99101418;
            double start = edgeAngle(idx - 0.46D);
            double end = edgeAngle(idx + 0.46D);

            // add the circle segment for this mode
            addCircleSegment(buffer, matrix, centerX, centerY, INNER_RADIUS, OUTER_RADIUS, start, end, color);
        }

        BufferUploader.drawWithShader(buffer.end());

        // we dont want to break the rest of gui rendering so restore all the settings we changed
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void fillCircle(Matrix4f matrix, int cx, int cy, int radius, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        addCircleSegment(buffer, matrix, cx, cy, 0, radius, 0, Math.PI * 2.0D, color);
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void addCircleSegment(BufferBuilder buffer, Matrix4f matrix, int centerX, int centerY,
            int innerRadius, int outerRadius, double start, double end, int color) {
        int steps = 12;
        for (int i = 0; i < steps; i++) {
            double a0 = start + (end - start) * i / steps;
            double a1 = start + (end - start) * (i + 1) / steps;

            // draw all the vertices of the polygon such that the segment can be drawn
            // i hate trigonometry
            // plzzz i hate trig plzz work
            vertex(buffer, matrix, centerX + Math.cos(a0) * innerRadius, centerY + Math.sin(a0) * innerRadius, color);
            vertex(buffer, matrix, centerX + Math.cos(a0) * outerRadius, centerY + Math.sin(a0) * outerRadius, color);
            vertex(buffer, matrix, centerX + Math.cos(a1) * outerRadius, centerY + Math.sin(a1) * outerRadius, color);
            vertex(buffer, matrix, centerX + Math.cos(a0) * innerRadius, centerY + Math.sin(a0) * innerRadius, color);
            vertex(buffer, matrix, centerX + Math.cos(a1) * outerRadius, centerY + Math.sin(a1) * outerRadius, color);
            vertex(buffer, matrix, centerX + Math.cos(a1) * innerRadius, centerY + Math.sin(a1) * innerRadius, color);
        }
    }

    private void vertex(BufferBuilder buffer, Matrix4f matrix, double x, double y, int color) {
        buffer.vertex(matrix, (float) x, (float) y, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >>> 24) & 255)
                .endVertex();
    }

    private double centerAngle(StarTMultitoolMode mode) {
        return edgeAngle(installedModes.indexOf(mode));
    }

    private double edgeAngle(double index) {
        return (Math.PI * 2.0D * index / installedModes.size()) - Math.PI / 2.0D;
    }

    // return location of button (should be to the right of the wheel)
    private int buttonX() { 
        return width / 2 + OUTER_RADIUS + 42; 
    }

    private int buttonY() { 
        return height / 2 - BUTTON_HEIGHT / 2;
    }
}