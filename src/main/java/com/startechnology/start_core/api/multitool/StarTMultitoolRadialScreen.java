package com.startechnology.start_core.api.multitool;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import com.startechnology.start_core.network.StarTNetwork;
import com.startechnology.start_core.network.packets.CPacketToggleSingleBlockMode;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    // gap between outer radius and buttons
    private static final int BUTTON_GAP = 42;

    // colour of the entire screen
    private static final int COL_SCREEN = 0x65000000;

    // highlight circle colour when hovering an item
    private static final int COL_HOVER_HIGHLIGHT_CIRCLE = 0x77FFAA00;

    // colour of the centre label
    private static final int COL_CENTRE_LABEL_NON_HOVER = 0xFFFFFFFF;
    private static final int COL_CENTRE_LABEL_HOVER = 0xFFEFC75E;

    // colour of the hints
    private static final int COL_HINT_BASE = 0xFFBFC7D5;
    private static final int COL_HINT_EJECT = 0xFFFF5555;

    // colour of the circle segments for a mode
    private static final int COL_SEGMENT = 0x99101418;
    private static final int COL_SEGMENT_HOVER = 0x88EFC75E;

    // current state of the radial screen
    private final ItemStack stack;
    private final InteractionHand hand;
    private StarTMultitoolMode hoveredMode;
    private StarTMultitoolMode selectedMode;
    private List<StarTMultitoolMode> installedModes;

    // stacks used for button icons, we need to lazily load these
    private ItemStack barrierStack;
    private ItemStack steelGearStack;

    protected StarTMultitoolRadialScreen(ItemStack stack, InteractionHand hand) {
        super(Component.translatable("key.start_core.multitool_selector"));
        this.stack = stack;
        this.hand = hand;
        this.installedModes = StarTMultitoolMode.getInstalled(stack);
        this.selectedMode = StarTMultitoolMode.getActive(stack);
    }

    @Override
    protected void init() {
        super.init();

        // lazily load the stacks, barrier should be guaranteed.
        barrierStack = new ItemStack(Items.BARRIER);

        // modifications to gt/in kjs may remove the steel gear,
        // since we want good support we just default to compass if unavailable
        try {
            ItemEntry<? extends Item> entry = GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.gear, GTMaterials.Steel);
            steelGearStack = (entry != null) ? new ItemStack(entry.get()) : new ItemStack(Items.COMPASS);
        } catch (Exception e) {
            steelGearStack = new ItemStack(Items.COMPASS);
        }
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

        // make buttons for the different config/modes
        boolean singleBlock = StarTMultitoolMode.isSingleBlockMode(stack);
        StarTMultitoolRadialButton singleBlockButton = singleBlockButton(singleBlock);
        StarTMultitoolRadialButton configButton = configButton();

        // full screen background
        graphics.fill(0, 0, width, height, COL_SCREEN);

        PoseStack pose = graphics.pose();

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

            // draw a small highlight circle behind the icon if hovered
            if (hovered) {
                fillCircle(pose.last().pose(), iconX + ICON_SIZE / 2, iconY + ICON_SIZE / 2, ICON_SIZE / 2 + 4,
                        COL_HOVER_HIGHLIGHT_CIRCLE);
            }

            if (!toolStack.isEmpty()) {
                graphics.renderItem(toolStack, iconX, iconY);
            }
        }

        // centre area for label & hints
        StarTMultitoolMode labelMode = hoveredMode == null ? selectedMode : hoveredMode;
        if (labelMode != null) {
            boolean isSneaking = hasShiftDown();
            int labelColor = hoveredMode == null ? COL_CENTRE_LABEL_NON_HOVER : COL_CENTRE_LABEL_HOVER;

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
                        centerX, centerY + 4, COL_HINT_EJECT);
            } else {
                graphics.drawCenteredString(font,
                        Component.translatable("key.start_core.multitool_selector.hint"),
                        centerX, centerY + 4, COL_HINT_BASE);
            }

            pose.popPose();
        }

        // render our buttons on either side of the wheel
        singleBlockButton.render(graphics, font, mouseX, mouseY, singleBlock);
        configButton.render(graphics, font, mouseX, mouseY, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // single blocok button for single block mode first as click priority
        boolean singleBlock = StarTMultitoolMode.isSingleBlockMode(stack);

        if (singleBlockButton(singleBlock).isHovered(mouseX, mouseY)) {
            StarTNetwork.NETWORK.sendToServer(new CPacketToggleSingleBlockMode(hand));
            StarTMultitoolMode.toggleSingleBlockMode(stack);
            return true;
        }

        // config button to show the auto select screen
        if (configButton().isHovered(mouseX, mouseY)) {
            minecraft.setScreen(new StarTMultitoolAutoSelectScreen(stack, hand));
            return true;
        }

        // fall through to circle
        StarTMultitoolMode mode = getMouseMode(width / 2, height / 2, mouseX, mouseY);
        if (mode != null) {
            if (hasShiftDown()) {
                eject(mode);
            } else {
                select(mode);
            }
            onClose();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
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
        // letting go should select the hovered mode
        if (StarTMultitoolClientEvents.OPEN_SELECTOR.matches(keyCode, scanCode)) {
            if (hoveredMode != null) {
                select(hoveredMode);
            }
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private StarTMultitoolRadialButton singleBlockButton(boolean singleBlock) {
        return new StarTMultitoolRadialButton(
                singleBlockButtonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT,
                barrierStack,
                Component.translatable(singleBlock
                        ? "item.start_core.gregtech_multitool.single_block_on"
                        : "item.start_core.gregtech_multitool.single_block_off"));
    }

    private StarTMultitoolRadialButton configButton() {
        return new StarTMultitoolRadialButton(
                autoSelectConfigButtonX(), buttonY(), BUTTON_WIDTH, BUTTON_HEIGHT,
                steelGearStack,
                Component.translatable("item.start_core.gregtech_multitool.auto_select_btn"));
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
            int color = mode == hoveredMode ? COL_SEGMENT_HOVER : COL_SEGMENT;
            addCircleSegment(buffer, matrix, centerX, centerY,
                    INNER_RADIUS, OUTER_RADIUS,
                    edgeAngle(idx - 0.46D), edgeAngle(idx + 0.46D), color);
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

    // should be to the right of the wheel
    private int singleBlockButtonX() {
        return width / 2 + OUTER_RADIUS + BUTTON_GAP;
    }

    // should be to the left of the wheel
    private int autoSelectConfigButtonX() {
        return (width / 2) - OUTER_RADIUS - BUTTON_WIDTH - BUTTON_GAP;
    }

    private int buttonY() {
        return height / 2 - BUTTON_HEIGHT / 2;
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
}