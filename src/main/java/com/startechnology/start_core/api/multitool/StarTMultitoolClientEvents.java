package com.startechnology.start_core.api.multitool;

import com.mojang.blaze3d.platform.InputConstants;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.item.StarTItems;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolItems;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import com.startechnology.start_core.network.StarTNetwork;
import com.startechnology.start_core.network.packets.CPacketMiddleClickAutoSelect;
import com.startechnology.start_core.network.packets.CPacketReleaseSingleBlockLock;
import com.startechnology.start_core.network.packets.CPacketSetMultitoolMode;
import com.startechnology.start_core.network.packets.CPacketUninstallMultitoolMode;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = StarTCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class StarTMultitoolClientEvents {

    private static final String CATEGORY = "key.categories.start_core";
    public static final KeyMapping OPEN_SELECTOR = new KeyMapping(
            "key.start_core.multitool_selector",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            CATEGORY);

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SELECTOR);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // this will tint the multitool if it has an active mode
        // with the active colour of the material
        StarTMultitoolItems.MULTITOOLS.values().forEach(itemEntry -> {
            event.register((stack, tintIndex) -> {
                StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
                if (active == null) return 0xFFFFFF;
                return tintIndex == 0 ? active.material().getMaterialRGB() : 0xFFFFFF;
            }, itemEntry.get());
        });
    }

    @Mod.EventBusSubscriber(modid = StarTCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen != null || event.getAction() != GLFW.GLFW_PRESS ||
                    !OPEN_SELECTOR.matches(event.getKey(), event.getScanCode())) {
                return;
            }

            // opens the new multitool radial screen if the key is pressed
            HeldMultitool held = getHeldMultitool();
            if (held != null) {
                minecraft.setScreen(new StarTMultitoolRadialScreen(held.stack(), held.hand()));
            }
        }

        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen != null || minecraft.player == null || !minecraft.player.isShiftKeyDown()) {
                return;
            }
            HeldMultitool held = getHeldMultitool();
            if (held == null)
                return;

            StarTMultitoolMode next = StarTMultitoolMode.offset(held.stack(), event.getScrollDelta() > 0 ? -1 : 1);
            if (next != null)
                selectMode(held.stack(), held.hand(), next);
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onMouseButton(InputEvent.MouseButton event) {
            // single click mode should prevent for client
            // breaking multiple blocks at once until release
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && 
                event.getAction() == GLFW.GLFW_RELEASE) {

                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player == null) return;

                HeldMultitool held = getHeldMultitool();
                if (held == null) return;

                // only bother sending if single block mode is actually on
                if (StarTMultitoolMode.isSingleBlockMode(held.stack())) {
                    StarTNetwork.NETWORK.sendToServer(new CPacketReleaseSingleBlockLock(held.hand()));
                }
            }

            // middle click mode for auto-select
            if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE ||
                event.getAction() != GLFW.GLFW_PRESS) return;
 
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.screen != null) return;
    
            HeldMultitool held = getHeldMultitool();
            if (held == null) return;
    
            // determine the id of the block the player is looking at
            HitResult hit = minecraft.hitResult;
            if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;
    
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            var blockState = minecraft.level.getBlockState(pos);
            String blockId = ForgeRegistries.BLOCKS.getKey(blockState.getBlock()).toString();
    
            // mode select middle click to server
            StarTNetwork.NETWORK.sendToServer(new CPacketMiddleClickAutoSelect(held.hand(), blockId));
    
            // send message to client
            minecraft.player.displayClientMessage(
                    Component.translatable("item.start_core.gregtech_multitool.auto_select",
                            Component.literal(blockId).withStyle(ChatFormatting.GRAY))
                            .withStyle(ChatFormatting.AQUA),
                    true);
    
            event.setCanceled(true);

        }
    }

    static HeldMultitool getHeldMultitool() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null)
            return null;
        ItemStack main = minecraft.player.getMainHandItem();
        if (main.getItem() instanceof StarTMultitoolItem) {
            return new HeldMultitool(main, InteractionHand.MAIN_HAND);
        }
        ItemStack offhand = minecraft.player.getOffhandItem();
        if (offhand.getItem() instanceof StarTMultitoolItem) {
            return new HeldMultitool(offhand, InteractionHand.OFF_HAND);
        }
        return null;
    }

    static void selectMode(ItemStack stack, InteractionHand hand, StarTMultitoolMode mode) {
        StarTNetwork.NETWORK.sendToServer(new CPacketSetMultitoolMode(mode, hand));
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable("item.start_core.gregtech_multitool.selected_mode",
                            mode.displayName().withStyle(ChatFormatting.AQUA)),
                    true);
        }
    }

    static void ejectMode(ItemStack stack, InteractionHand hand, StarTMultitoolMode mode) {
        StarTNetwork.NETWORK.sendToServer(new CPacketUninstallMultitoolMode(mode, hand));
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable("item.start_core.gregtech_multitool.ejected_mode",
                            mode.displayName().withStyle(ChatFormatting.RED)),
                    true);
        }
    }

    record HeldMultitool(ItemStack stack, InteractionHand hand) {
    }
}