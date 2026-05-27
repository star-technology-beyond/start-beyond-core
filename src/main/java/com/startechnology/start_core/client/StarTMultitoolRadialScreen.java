package com.startechnology.start_core.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class StarTMultitoolRadialScreen extends Screen {
    private static final int INNER_RADIUS = 24;
    private static final int LABEL_RADIUS = 58;
    private static final int OUTER_RADIUS = 84;

    private final ItemStack stack;
    private final InteractionHand hand;
    private StarTMultitoolMode hoveredMode;
    private StarTMultitoolMode selectedMode;

    protected StarTMultitoolRadialScreen(ItemStack stack, InteractionHand hand) {
        super(Component.translatable("key.start_core.multitool_selector"));
        this.stack = stack;
        this.hand = hand;
        this.selectedMode = StarTMultitoolMode.get(stack);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (StarTMultitoolClientEvents.getHeldMultitool() == null) {
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int centerY = height / 2 + 24;
        hoveredMode = getMouseMode(centerX, centerY, mouseX, mouseY);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        drawAnnulus(pose.last().pose(), centerX, centerY);
        pose.popPose();

        for (StarTMultitoolMode mode : StarTMultitoolMode.VALUES) {
            double angle = centerAngle(mode);
            int x = centerX + (int) Math.round(Math.cos(angle) * LABEL_RADIUS);
            int y = centerY + (int) Math.round(Math.sin(angle) * LABEL_RADIUS);
            boolean hovered = mode == hoveredMode;
            boolean active = mode == selectedMode;
            int color = hovered ? 0xFFFFFFFF : active ? 0xFF66D9EF : 0xFFBFC7D5;
            graphics.drawCenteredString(font, mode.id().substring(0, 1).toUpperCase(), x, y - 4, color);
        }

        StarTMultitoolMode labelMode = hoveredMode == null ? selectedMode : hoveredMode;
        int labelColor = hoveredMode == null ? 0xFFFFFFFF : 0xFFEFC75E;
        graphics.drawCenteredString(font, labelMode.displayName(), centerX, centerY - 5, labelColor);
        graphics.drawCenteredString(font, Component.translatable("key.start_core.multitool_selector.hint"),
                centerX, centerY + 9, 0xFFBFC7D5);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        StarTMultitoolMode mode = getMouseMode(width / 2, height / 2 + 24, mouseX, mouseY);
        if (mode != null) {
            select(mode);
            onClose();
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        select(selectedMode.offset(delta > 0 ? -1 : 1));
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (StarTMultitoolClientEvents.OPEN_SELECTOR.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void select(StarTMultitoolMode mode) {
        selectedMode = mode;
        StarTMultitoolClientEvents.selectMode(stack, hand, mode);
    }

    private StarTMultitoolMode getMouseMode(int centerX, int centerY, double mouseX, double mouseY) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < INNER_RADIUS || distance > OUTER_RADIUS) {
            return null;
        }
        double angle = Math.atan2(dy, dx) + Math.PI / 2.0D;
        if (angle < 0) {
            angle += Math.PI * 2.0D;
        }
        int index = (int) Math.floor((angle / (Math.PI * 2.0D)) * StarTMultitoolMode.VALUES.length + 0.5D);
        return StarTMultitoolMode.VALUES[Math.floorMod(index, StarTMultitoolMode.VALUES.length)];
    }

    private void drawAnnulus(Matrix4f matrix, int centerX, int centerY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (StarTMultitoolMode mode : StarTMultitoolMode.VALUES) {
            int color = mode == hoveredMode ? 0xAAEFC75E : mode == selectedMode ? 0x8858C7F3 : 0xCC121820;
            double start = edgeAngle(mode.ordinal() - 0.46D);
            double end = edgeAngle(mode.ordinal() + 0.46D);
            addAnnulusSegment(buffer, matrix, centerX, centerY, INNER_RADIUS, OUTER_RADIUS, start, end, color);
        }

        addAnnulusSegment(buffer, matrix, centerX, centerY, 0, INNER_RADIUS - 2, 0, Math.PI * 2.0D, 0xE0202630);
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }

    private void addAnnulusSegment(BufferBuilder buffer, Matrix4f matrix, int centerX, int centerY, int innerRadius,
                                   int outerRadius, double start, double end, int color) {
        int steps = 8;
        for (int i = 0; i < steps; i++) {
            double a0 = start + (end - start) * i / steps;
            double a1 = start + (end - start) * (i + 1) / steps;
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
        return edgeAngle(mode.ordinal());
    }

    private double edgeAngle(double index) {
        return (Math.PI * 2.0D * index / StarTMultitoolMode.VALUES.length) - Math.PI / 2.0D;
    }
}
