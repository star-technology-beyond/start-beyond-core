package com.startechnology.start_core.api.multitool;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.startechnology.start_core.item.multitool.StarTMultitoolAutoSelectRules;
import com.startechnology.start_core.item.multitool.StarTMultitoolAutoSelectRules.Rule;
import com.startechnology.start_core.network.StarTNetwork;
import com.startechnology.start_core.network.packets.CPacketSaveAutoSelectRules;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StarTMultitoolAutoSelectScreen extends Screen {

    // panel
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 212;
    private static final int ROW_H = 18;
    private static final int VISIBLE_ROWS = 7;
    private static final int DD_ROWS = 5;
    private static final int DD_ITEM_H = 14;

    // layout
    private static final int PADDING_OUTER = 4;
    private static final int PADDING_INNER = 8;

    private static final int LIST_TOP_OFFSET = 35;
    private static final int ADD_ROW_MARGIN_TOP = 5;
    private static final int BOTTOM_SECTION_OFFSET = 24;

    // header with title bits layout
    private static final int TITLE_Y = 5;
    private static final int COL_TITLE_Y = 21;
    private static final int HEADER_DIVIDER_Y = 33;
    private static final int DIVIDER_THICKNESS = 1;

    // coloumn offsets and widths
    private static final int COL_HANDLE_X = 4;
    private static final int COL_HANDLE_W = 12;
    private static final int COL_REGEX_X = 18;
    private static final int COL_BADGE_X = 222;
    private static final int COL_BADGE_W = 90;
    private static final int COL_DEL_X = 316;
    private static final int COL_DEL_W = 14;

    // row offsets
    private static final int ROW_TEXT_Y = 5;
    private static final int BADGE_MARGIN_Y = 1;
    private static final int BADGE_SHRINK_Y = 2;
    private static final int DEL_BTN_MARGIN_Y = 3;
    private static final int DEL_BTN_SHRINK_Y = 6;
    private static final int TEXT_ELLIPSIS_TRIM = 6;

    // add row (adds new regex to list) offsets for
    // elements and widths
    private static final int ADD_ROW_ELEMENT_H = 16;
    private static final int ADD_FIELD_X = 4;
    private static final int ADD_FIELD_W = 196;
    private static final int ADD_FIELD_Y = 4;
    private static final int ADD_TICK_X = 9;
    private static final int ADD_ICON_Y = 7;
    private static final int ADD_BTN_X = 314;
    private static final int ADD_BTN_W = 22;
    private static final int ADD_BTN_Y = 3;
    private static final int ADD_BADGE_X = 222;

    // dropdown layout
    private static final int DD_BORDER_THICKNESS = 1;
    private static final int DD_TEXT_Y = 3;
    private static final int DD_ANCHOR_OFFSET_Y = 22;
    private static final int SCROLL_ICON_OFFSET = 7;
    private static final int SCROLL_ICON_Y_TOP = 2;
    private static final int SCROLL_ICON_Y_BOT = 10;

    // save button area layout
    private static final int SAVE_MARGIN_Y = 3;
    private static final int SAVE_BTN_H = 16;
    private static final int SAVE_BTN_TEXT_ADJUST = 4;
    private static final int SAVE_BTN_BOTTOM_MARGIN = 6;

    // constraints for texts to ensure we dont get
    // funky overlay
    private static final int MAX_LABEL_LEN = 15;
    private static final int TRUNCATED_LABEL_LEN = 14;
    private static final int REGEX_MAX_LEN = 256;

    // z ordering of elements
    private static final int Z_FLOAT_ROW = 350;
    private static final int Z_DROPDOWN = 400;

    // colours

    // colour of the entire screen, should match radial screen
    private static final int COL_SCREEN = 0x65000000;

    // colour of the panel all the selection is on
    private static final int COL_PANEL = 0xBB0D1117;

    // element divider
    private static final int COL_DIVIDER = 0x66EFC75E;

    // alternate colours of rows for visibility
    private static final int COL_ROW_A = 0xFF101418;
    private static final int COL_ROW_B = 0xFF141820;

    // hover/drag row colours
    private static final int COL_ROW_HOV = 0x55EFC75E;
    private static final int COL_ROW_DRAG = 0xFF0C1014;

    // element colours
    private static final int COL_FLOAT_BG = 0xFF101820;
    private static final int COL_DROP_LINE = 0xFFEFC75E;
    private static final int COL_ACCENT = 0xFFEFC75E;
    private static final int COL_TEXT = 0xFFBFC7D5;
    private static final int COL_DIM = 0xFF697A8E;
    private static final int COL_ERR = 0xFFFF5555;
    private static final int COL_OK = 0xFF55FF88;
    private static final int COL_BTN = 0x88101418;
    private static final int COL_BTN_HOV = 0x88EFC75E;

    // dropdown should contain a list of all the tool names
    // for selection
    private static final List<String> ALL_TOOLS = GTToolType.getTypes().values().stream()
            .map(t -> t.name)
            .sorted()
            .toList();

    // current state of the item and menu
    private final ItemStack stack;
    private final InteractionHand hand;
    private final List<Rule> rules;
    private int listScroll = 0;
    private int hoveredRow = -1;

    // state for dragged row
    private int draggedIdx = -1;
    private int dragTargetIdx = -1;
    private double dragY = 0;

    // state for dropdown
    private int dropdownRow = -1;
    private boolean addDropdown = false;
    private int dropdownScroll = 0;

    // state for the add row
    private EditBox regexField;
    private int addToolIdx = 0;

    public StarTMultitoolAutoSelectScreen(ItemStack stack, InteractionHand hand) {
        super(Component.translatable("screen.start_core.auto_select_rules"));
        this.stack = stack;
        this.hand = hand;
        this.rules = new ArrayList<>(StarTMultitoolAutoSelectRules.getRules(stack));
        if (this.rules.isEmpty()) {
            this.rules.addAll(StarTMultitoolAutoSelectRules.DEFAULTS);
        }
    }

    private int panelX() {
        return (width - PANEL_W) / 2;
    }

    private int panelY() {
        return (height - PANEL_H) / 2;
    }

    private int listStartY() {
        return panelY() + LIST_TOP_OFFSET;
    }

    private int addRowY() {
        return listStartY() + (VISIBLE_ROWS * ROW_H) + ADD_ROW_MARGIN_TOP;
    }

    private int bottomY() {
        return addRowY() + BOTTOM_SECTION_OFFSET;
    }

    @Override
    protected void init() {
        super.init();

        // initialise the regex box for entering new regex elements to the tool
        regexField = new EditBox(font, panelX() + ADD_FIELD_X, addRowY() + ADD_FIELD_Y, ADD_FIELD_W, DD_ITEM_H,
                Component.empty());
        regexField.setMaxLength(REGEX_MAX_LEN);
        regexField.setHint(Component.translatable("item.start_core.gregtech_multitool.regex_pattern_placeholder")
                .withStyle(ChatFormatting.DARK_GRAY));
        regexField.setCanLoseFocus(true);
        addWidget(regexField);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        hoveredRow = -1;

        // render each component of the screen
        renderBackgroundLayer(g);
        renderHeaders(g);
        renderRuleList(g, mouseX, mouseY);
        renderAddRow(g, mouseX, mouseY, partialTicks);
        renderSave(g, mouseX, mouseY);

        renderFloatingDragRow(g, mouseX, mouseY);
        renderDropdownOverlay(g, mouseX, mouseY);
        renderTooltips(g, mouseX, mouseY);
    }

    private void renderBackgroundLayer(GuiGraphics g) {
        // full bg colour (dim whole screen)
        g.fill(0, 0, width, height, COL_SCREEN);

        // panel covers all elements
        g.fill(panelX(), panelY(), panelX() + PANEL_W, panelY() + PANEL_H, COL_PANEL);
    }

    private void renderHeaders(GuiGraphics g) {
        // render the header text (at the top above the rows)
        int px = panelX();
        int py = panelY();

        // title of entire panel
        g.drawString(font,
                Component.translatable("item.start_core.gregtech_multitool.auto_select_rules_header")
                        .withStyle(ChatFormatting.BOLD),
                px + PADDING_INNER, py + TITLE_Y, COL_ACCENT, false);

        // column titles
        g.drawString(font, Component.translatable("item.start_core.gregtech_multitool.pattern_header"),
                px + COL_REGEX_X, py + COL_TITLE_Y, COL_DIM, false);
        g.drawString(font, Component.translatable("item.start_core.gregtech_multitool.tool_header"),
                px + COL_BADGE_X + BOTTOM_SECTION_OFFSET, py + COL_TITLE_Y, COL_DIM, false);

        // add divider to rows
        g.fill(px + PADDING_OUTER, py + HEADER_DIVIDER_Y, px + PANEL_W - PADDING_OUTER,
                py + HEADER_DIVIDER_Y + DIVIDER_THICKNESS, COL_DIVIDER);
    }

    private void renderRuleList(GuiGraphics g, int mouseX, int mouseY) {
        int px = panelX();
        int listY = listStartY();

        boolean isAnyRowDragging = (draggedIdx >= 0);

        // draw all visible rows
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int idx = i + listScroll;
            if (idx >= rules.size())
                break;

            // check if we are dragging/hovering this row
            int rowY = listY + i * ROW_H;
            boolean isDragged = (idx == draggedIdx);
            boolean isHovered = !isDragged && !isAnyRowDragging
                    && hit(mouseX, mouseY, px, rowY, PANEL_W - PADDING_OUTER / 2, ROW_H);

            if (isHovered)
                hoveredRow = idx;

            // change row bg colour depending on dragging or hovered
            // or just normal (alternate A & B for visibility)
            int rowBgColor = isDragged ? COL_ROW_DRAG : isHovered ? COL_ROW_HOV : (i % 2 == 0) ? COL_ROW_A : COL_ROW_B;
            g.fill(px, rowY, px + PANEL_W, rowY + ROW_H, rowBgColor);

            // we dont want to render the dragged row in the row list,
            // since we want to render it at the current location we're dragging
            if (!isDragged) {
                renderRow(g, mouseX, mouseY, idx, rules.get(idx), rowY, false, isAnyRowDragging);
            }
        }

        // render the drop target line for the current dragging row
        renderDropTargetLine(g);
        renderScrollIndicators(g, listY, rules.size(), listScroll, VISIBLE_ROWS, px + PANEL_W - SCROLL_ICON_Y_BOT);
    }

    // renders a "drop target line" this is basically
    // just a display of where the currently dragged row
    // will go whehn dropped
    private void renderDropTargetLine(GuiGraphics g) {
        if (draggedIdx >= 0) {
            int lineVis = dragTargetIdx - listScroll;
            if (lineVis >= 0 && lineVis <= VISIBLE_ROWS) {
                int lineY = listStartY() + lineVis * ROW_H;
                g.fill(panelX() + PADDING_OUTER, lineY - DIVIDER_THICKNESS, panelX() + PANEL_W - PADDING_OUTER,
                        lineY + DIVIDER_THICKNESS, COL_DROP_LINE);
            }
        }
    }

    private void renderAddRow(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        int px = panelX();
        int ary = addRowY();

        // divider from the rows section to the add row section
        g.fill(px + PADDING_OUTER, ary - DIVIDER_THICKNESS * 2, px + PANEL_W - PADDING_OUTER, ary - DIVIDER_THICKNESS,
                COL_DIVIDER);

        // update the regex entry field to match offsets properly
        regexField.setX(px + ADD_FIELD_X);
        regexField.setY(ary + ADD_FIELD_Y);
        regexField.render(g, mouseX, mouseY, partialTicks);

        // draw whether or not the current regex is valid
        // to show the user instead of just having a greyed out add button
        String rawRegex = regexField.getValue();
        if (!rawRegex.isEmpty()) {
            boolean isValid = isValidRegex(rawRegex);
            g.drawString(font, isValid ? "✔" : "✘", px + ADD_FIELD_X + ADD_FIELD_W + ADD_TICK_X, ary + ADD_ICON_Y,
                    isValid ? COL_OK : COL_ERR, false);
        }

        // draw the tool badge (this starts the dropdown)
        boolean isBadgeHovered = !addDropdown
                && hit(mouseX, mouseY, px + ADD_BADGE_X, ary + ADD_BTN_Y, COL_BADGE_W, ADD_ROW_ELEMENT_H);
        drawToolBadge(g, px + ADD_BADGE_X, ary + ADD_BTN_Y, COL_BADGE_W, ADD_ROW_ELEMENT_H, ALL_TOOLS.get(addToolIdx),
                isBadgeHovered, addDropdown);

        // draw the button for adding the row
        // it should only allow adding if the regex is valid & not empty
        boolean canAdd = !rawRegex.isEmpty() && isValidRegex(rawRegex);
        boolean isAddHovered = canAdd
                && hit(mouseX, mouseY, px + ADD_BTN_X, ary + ADD_BTN_Y, ADD_BTN_W, ADD_ROW_ELEMENT_H);

        g.fill(px + ADD_BTN_X, ary + ADD_BTN_Y, px + ADD_BTN_X + ADD_BTN_W, ary + ADD_BTN_Y + ADD_ROW_ELEMENT_H,
                isAddHovered ? COL_BTN_HOV : COL_BTN);
        g.drawCenteredString(font, Component.translatable("item.start_core.gregtech_multitool.add_btn"), px + ADD_BTN_X + ADD_BTN_W / 2, ary + ADD_ICON_Y,
                canAdd ? (isAddHovered ? COL_ACCENT : COL_TEXT) : COL_DIM);
    }

    private void renderSave(GuiGraphics g, int mouseX, int mouseY) {
        int px = panelX();
        int by = bottomY();

        // draw the divider between add section/save section
        g.fill(px + PADDING_OUTER, by, px + PANEL_W - PADDING_OUTER, by + DIVIDER_THICKNESS, COL_DIVIDER);

        // draw the save button
        int hitHeight = panelY() + PANEL_H - by - SAVE_BTN_BOTTOM_MARGIN;
        boolean isSaveHovered = hit(mouseX, mouseY, px + PADDING_OUTER, by + SAVE_MARGIN_Y,
                PANEL_W - (PADDING_OUTER * 2), hitHeight);

        g.fill(px + PADDING_OUTER, by + SAVE_MARGIN_Y, px + PANEL_W - PADDING_OUTER, by + SAVE_MARGIN_Y + SAVE_BTN_H,
                isSaveHovered ? COL_BTN_HOV : COL_BTN);
        g.drawCenteredString(font, Component.translatable("item.start_core.gregtech_multitool.save_btn"), px + PANEL_W / 2, by + SAVE_MARGIN_Y + SAVE_BTN_H / 2 - SAVE_BTN_TEXT_ADJUST,
                isSaveHovered ? COL_ACCENT : COL_TEXT);
    }

    private void renderFloatingDragRow(GuiGraphics g, int mouseX, int mouseY) {
        // draw the dragged row specially 
        // (we dont want to render it amongst all normal rows)
        if (draggedIdx >= 0 && draggedIdx < rules.size()) {
            int px = panelX();
            int floatY = (int) dragY - ROW_H / 2;

            // draw above everything else to cover
            // the dropdown and stuff
            g.pose().pushPose();
            g.pose().translate(0, 0, Z_FLOAT_ROW);

            // GRahhh i dont know whhy ts always rendering under shit
            // just draw a special bg colour under the floating row lmfaooo
            // this works
            g.fill(px, floatY, px + PANEL_W, floatY + ROW_H, COL_FLOAT_BG);
            g.fill(px + PADDING_OUTER, floatY, px + PANEL_W - PADDING_OUTER, floatY + DIVIDER_THICKNESS, COL_ACCENT);
            g.fill(px + PADDING_OUTER, floatY + ROW_H - DIVIDER_THICKNESS, px + PANEL_W - PADDING_OUTER, floatY + ROW_H,
                    COL_ACCENT);
            
            // render the row above other elements at the current mouse pos
            renderRow(g, mouseX, mouseY, draggedIdx, rules.get(draggedIdx), floatY, true, false);
            g.pose().popPose();
        }
    }

    private void renderDropdownOverlay(GuiGraphics g, int mouseX, int mouseY) {
        // renders the overlay for a dropdown
        if (dropdownRow >= 0 || addDropdown) {
            g.pose().pushPose();

            // draw above other elements so we can see it better
            g.pose().translate(0, 0, Z_DROPDOWN);
            renderDropdown(g, mouseX, mouseY);
            g.pose().popPose();
        }
    }

    private void renderTooltips(GuiGraphics g, int mouseX, int mouseY) {
        // render hover tooltips if necessary for a rule
        // since rules can overflow the little row
        // and we want to show the entire rule
        if (hoveredRow >= 0 && hoveredRow < rules.size()) {
            String regex = rules.get(hoveredRow).regex();
            if (font.width(regex) > COL_BADGE_X - COL_REGEX_X - PADDING_OUTER) {
                g.renderComponentTooltip(font, List.of(Component.literal(regex).withStyle(ChatFormatting.YELLOW)),
                        mouseX, mouseY);
            }
        }
    }

    private void renderRow(GuiGraphics g, int mouseX, int mouseY, int idx, Rule rule, int rowY, boolean isFloating,
            boolean isDragging) {
        int px = panelX();
        
        // draw the little drag icon
        g.drawString(font, "≡", px + COL_HANDLE_X + DIVIDER_THICKNESS, rowY + ROW_TEXT_Y,
                isFloating ? COL_ACCENT : COL_DIM, false);
            
        // draw the rule text, truncate to fit the size tho
        // (hover handles full string)
        int maxTextW = COL_BADGE_X - COL_REGEX_X - PADDING_OUTER;
        String text = rule.regex();
        if (font.width(text) > maxTextW) {
            text = font.plainSubstrByWidth(text, maxTextW - TEXT_ELLIPSIS_TRIM) + "…";
        }
        g.drawString(font, text, px + COL_REGEX_X, rowY + ROW_TEXT_Y, rule.isValid() ? COL_TEXT : COL_ERR, false);

        // draw the tool dropdown badge (click opens dropdown)
        boolean badgeOpen = !isFloating && dropdownRow == idx;
        boolean badgeHov = !isFloating && !badgeOpen && !isDragging
                && hit(mouseX, mouseY, px + COL_BADGE_X, rowY + BADGE_MARGIN_Y, COL_BADGE_W, ROW_H - BADGE_SHRINK_Y);
        drawToolBadge(g, px + COL_BADGE_X, rowY + BADGE_MARGIN_Y, COL_BADGE_W, ROW_H - BADGE_SHRINK_Y,
                rule.toolTypeName(), badgeHov, badgeOpen);
        
        // draw the little delete icon which should delete this row
        boolean delHov = !isFloating && !isDragging
                && hit(mouseX, mouseY, px + COL_DEL_X, rowY + DEL_BTN_MARGIN_Y, COL_DEL_W, ROW_H - DEL_BTN_SHRINK_Y);
        g.drawCenteredString(font, "✕", px + COL_DEL_X + COL_DEL_W / 2, rowY + ROW_TEXT_Y,
                delHov ? COL_ERR : 0x55FF5555);
    }

    private void renderDropdown(GuiGraphics g, int mouseX, int mouseY) {
        // calculate the geometry of the dropdown.
        // this is a helper that decides whether we should 
        // flip upwards or go downwards depending on the location
        // of the dropdown
        DropdownGeometry geo = calculateDropdownGeometry();
        int curTypeIdx = (dropdownRow >= 0 && dropdownRow < rules.size())
                ? ALL_TOOLS.indexOf(rules.get(dropdownRow).toolTypeName())
                : addToolIdx;

        // draw the dropdown
        g.fill(geo.x, geo.y, geo.x + geo.width, geo.y + geo.height, 0xFF0A0E12);
        g.fill(geo.x, geo.y, geo.x + geo.width, geo.y + DD_BORDER_THICKNESS, COL_ACCENT);
        g.fill(geo.x, geo.y + geo.height - DD_BORDER_THICKNESS, geo.x + geo.width, geo.y + geo.height, COL_ACCENT);
        g.fill(geo.x, geo.y, geo.x + DD_BORDER_THICKNESS, geo.y + geo.height, 0x44EFC75E);
        g.fill(geo.x + geo.width - DD_BORDER_THICKNESS, geo.y, geo.x + geo.width, geo.y + geo.height, 0x44EFC75E);

        // add all the tool rows for selection
        for (int i = 0; i < DD_ROWS; i++) {
            int tIdx = i + dropdownScroll;
            if (tIdx >= ALL_TOOLS.size())
                break;

            // draw the dropdown for the tool w bg
            // that changes if selected or if hovered if necessary
            int itemY = geo.y + DD_BORDER_THICKNESS + i * DD_ITEM_H;
            boolean isSelected = (tIdx == curTypeIdx);
            boolean isHovered = hit(mouseX, mouseY, geo.x, itemY, geo.width, DD_ITEM_H);

            if (isSelected)
                g.fill(geo.x, itemY, geo.x + geo.width, itemY + DD_ITEM_H, 0x55EFC75E);
            else if (isHovered)
                g.fill(geo.x, itemY, geo.x + geo.width, itemY + DD_ITEM_H, 0x22EFC75E);

            g.drawCenteredString(font, toolDisplayName(ALL_TOOLS.get(tIdx)), geo.x + geo.width / 2, itemY + DD_TEXT_Y,
                    isSelected ? COL_ACCENT : isHovered ? COL_TEXT : COL_DIM);
        }

        renderScrollIndicators(g, geo.y, ALL_TOOLS.size(), dropdownScroll, DD_ROWS,
                geo.x + geo.width - SCROLL_ICON_OFFSET);
    }

    private void drawToolBadge(GuiGraphics g, int x, int y, int width, int height, String toolName, boolean isHovered,
            boolean isOpen) {
        g.fill(x, y, x + width, y + height, (isOpen || isHovered) ? COL_BTN_HOV : COL_BTN);
        g.drawCenteredString(font, toolDisplayName(toolName) + " ▾", x + width / 2, y + height / 2 - DD_TEXT_Y,
                COL_ACCENT);
    }

    private void renderScrollIndicators(GuiGraphics g, int startY, int totalItems, int currentScroll, int visibleSlots,
            int xOffset) {
        if (currentScroll > 0) {
            g.drawString(font, "▲", xOffset, startY + SCROLL_ICON_Y_TOP, COL_DIM, false);
        }
        if (currentScroll + visibleSlots < totalItems) {
            g.drawString(font, "▼", xOffset,
                    startY + visibleSlots * (totalItems == ALL_TOOLS.size() ? DD_ITEM_H : ROW_H) - SCROLL_ICON_Y_BOT,
                    COL_DIM, false);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
       // propority dropdown > list > add row > footer
       // since dropdown can draw ontop of list/add row
       // we dont want it to fall through
        if (dropdownRow >= 0 || addDropdown) {
            return handleDropdownClick(mouseX, mouseY);
        }
        if (handleListClick(mouseX, mouseY))
            return true;
        if (handleAddRowClick(mouseX, mouseY))
            return true;
        if (handleFooterClick(mouseX, mouseY))
            return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleDropdownClick(double mouseX, double mouseY) {
        // get the dropdown geometry again
        // this tells us the positions of the dropdown
        // and automatically handles the flip up/go down handling
        DropdownGeometry geo = calculateDropdownGeometry();

        // check if mouse falls in geometry position
        if (hit(mouseX, mouseY, geo.x, geo.y, geo.width, geo.height)) {
            int tIdx = (int) ((mouseY - geo.y - DD_BORDER_THICKNESS) / DD_ITEM_H) + dropdownScroll;
            
            // find rule for the specific index we click on
            if (tIdx >= 0 && tIdx < ALL_TOOLS.size()) {
                if (dropdownRow >= 0 && dropdownRow < rules.size()) {
                    Rule r = rules.get(dropdownRow);
                    rules.set(dropdownRow, new Rule(r.regex(), ALL_TOOLS.get(tIdx)));
                } else {
                    addToolIdx = tIdx;
                }
            }
        }

        // if out of the geometry then close
        closeDropdown();
        return true;
    }

    private boolean handleListClick(double mouseX, double mouseY) {
        int px = panelX();
        int listY = listStartY();

        // check if we landed in any row in the visible rendered rows
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int idx = i + listScroll;
            if (idx >= rules.size())
                break;

            int rowY = listY + i * ROW_H;
            if (!hit(mouseX, mouseY, px, rowY, PANEL_W, ROW_H))
                continue;

            // we hit this row! calculate what exactly we hit

            // delete
            if (hit(mouseX, mouseY, px + COL_DEL_X, rowY + DEL_BTN_MARGIN_Y, COL_DEL_W, ROW_H - DEL_BTN_SHRINK_Y)) {
                rules.remove(idx);
                listScroll = Math.max(0, Math.min(listScroll, rules.size() - VISIBLE_ROWS));
                return true;
            }

            // hit the dropdown tool badge, update state
            // to show dropdown for this row
            if (hit(mouseX, mouseY, px + COL_BADGE_X, rowY + BADGE_MARGIN_Y, COL_BADGE_W, ROW_H - BADGE_SHRINK_Y)) {
                dropdownRow = idx;
                dropdownScroll = Math.max(0, ALL_TOOLS.indexOf(rules.get(idx).toolTypeName()) - 2);
                addDropdown = false;
                return true;
            }

            // hit the drag, update state to
            // be dragging this row
            if (hit(mouseX, mouseY, px + COL_HANDLE_X, rowY, COL_HANDLE_W, ROW_H)) {
                draggedIdx = idx;
                dragY = mouseY;
                dragTargetIdx = idx;
                return true;
            }
        }
        return false;
    }

    private boolean handleAddRowClick(double mouseX, double mouseY) {
        int px = panelX();
        int ary = addRowY();

        // hit the dropdown tool badge, show
        // dropodown here
        if (hit(mouseX, mouseY, px + ADD_BADGE_X, ary + ADD_BTN_Y, COL_BADGE_W, ADD_ROW_ELEMENT_H)) {
            addDropdown = true;
            dropdownRow = -1;
            dropdownScroll = Math.max(0, addToolIdx - 2);
            return true;
        }

        // hit the add button, try add this rule
        if (hit(mouseX, mouseY, px + ADD_BTN_X, ary + ADD_BTN_Y, ADD_BTN_W, ADD_ROW_ELEMENT_H)) {
            tryAddRule();
            return true;
        }

        return false;
    }

    private boolean handleFooterClick(double mouseX, double mouseY) {
        int by = bottomY();
        int hitHeight = panelY() + PANEL_H - by - SAVE_BTN_BOTTOM_MARGIN;

        // save button
        if (hit(mouseX, mouseY, panelX() + PADDING_INNER, by + SAVE_MARGIN_Y, PANEL_W - (PADDING_INNER * 2),
                hitHeight)) {
            saveAndClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragYOffset) {
        // if we are dragging, then update the position
        // of the dragged row to our mouse Y so it follows nicely
        if (draggedIdx >= 0) {
            dragY = mouseY;
            int slot = (int) ((mouseY - listStartY()) / ROW_H) + listScroll;
            dragTargetIdx = Math.max(0, Math.min(rules.size(), slot));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragYOffset);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // update the drag state to handle
        // release so that we put the rule
        // in the correct position
        if (draggedIdx >= 0) {
            // original location/final location
            // and ensure it falls in the rules!!
            int from = draggedIdx;
            int to = (dragTargetIdx > from) ? dragTargetIdx - 1 : dragTargetIdx;
            to = Math.max(0, Math.min(rules.size() - 1, to));

            if (from != to) {
                rules.add(to, rules.remove(from));
            }
            draggedIdx = -1;
            dragTargetIdx = -1;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // with a dropdown open we should scroll the dropdown
        if (dropdownRow >= 0 || addDropdown) {
            int max = Math.max(0, ALL_TOOLS.size() - DD_ROWS);
            dropdownScroll = (int) Math.max(0, Math.min(max, dropdownScroll - delta));
            return true;
        }

        // scroll the list itself if no dropdown
        // (no point testing list panel hit since QOL if we can scroll anywhere)
        int max = Math.max(0, rules.size() - VISIBLE_ROWS);
        listScroll = (int) Math.max(0, Math.min(max, listScroll - delta));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        // esc should be intercepted to close dropdown
        if (key == 256 && (dropdownRow >= 0 || addDropdown)) {
            closeDropdown();
            return true;
        }

        // forward key presses to regex field so it updates
        if (regexField.keyPressed(key, scanCode, modifiers)) {
            return true;
        }

        // enter in regex field should try add thhe rule
        // currently in the regex field if its valid/focused
        if (key == 257 && regexField.isFocused()) {
            tryAddRule();
            return true;
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (regexField.charTyped(codePoint, modifiers))
            return true;

        return super.charTyped(codePoint, modifiers);
    }


    private void tryAddRule() {
        // ensure the regex is valid and not empty if we try add
        String raw = regexField.getValue().trim();
        if (raw.isEmpty() || !isValidRegex(raw))
            return;

        // add to the end
        rules.add(new Rule(raw, ALL_TOOLS.get(addToolIdx)));
        regexField.setValue("");
        listScroll = Math.max(0, rules.size() - VISIBLE_ROWS);
    }

    private void saveAndClose() {
        // update on client side first before server
        // for faster feedback
        StarTMultitoolAutoSelectRules.setRules(stack, rules);
        StarTNetwork.NETWORK.sendToServer(new CPacketSaveAutoSelectRules(hand, rules));
        onClose();
    }

    private void closeDropdown() {
        dropdownRow = -1;
        addDropdown = false;
        dropdownScroll = 0;
    }

    private static boolean hit(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static boolean isValidRegex(String s) {
        try {
            Pattern.compile(s);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private static String toolDisplayName(String toolTypeName) {
        if (toolTypeName == null || toolTypeName.isEmpty())
            return "?";

        // cutting out ( and ) lets it fit in dropdown with 15 chars!
        String full = Component.translatable("item.gtceu.tool." + toolTypeName, "")
                .getString()
                .trim()
                .replace("(", "")
                .replace(")", "");
        return shortLabel(full);
    }

    private static String shortLabel(String text) {
        // truncate label so it fits in max label length
        // mainly for dropdowns
        if (text == null || text.isEmpty())
            return "?";
        return text.length() > MAX_LABEL_LEN ? text.substring(0, TRUNCATED_LABEL_LEN) + "." : text;
    }


    private DropdownGeometry calculateDropdownGeometry() {
        int x = panelX() + COL_BADGE_X;
        int width = COL_BADGE_W;
        int height = (DD_ROWS * DD_ITEM_H) + (DD_BORDER_THICKNESS * 2);
        int anchorY;

        // find out the anchor for the actual dropdown
        // to calculate if we should flip upwards or keep downwards
        // for dropdown panel bounds checking
        if (dropdownRow >= 0 && dropdownRow < rules.size()) {
            anchorY = listStartY() + (dropdownRow - listScroll + 1) * ROW_H;
        } else {
            anchorY = addRowY() + DD_ANCHOR_OFFSET_Y;
        }

        // flip upward if dropdown exceeds panel bounds
        if (anchorY + height > panelY() + PANEL_H - PADDING_OUTER) {
            anchorY -= ((dropdownRow >= 0 ? ROW_H : ROW_H) + height);
        }

        return new DropdownGeometry(x, anchorY, width, height);
    }

    private record DropdownGeometry(int x, int y, int width, int height) {
    }
}