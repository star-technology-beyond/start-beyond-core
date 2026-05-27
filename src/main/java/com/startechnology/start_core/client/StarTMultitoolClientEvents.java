package com.startechnology.start_core.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import com.startechnology.start_core.network.StarTNetwork;
import com.startechnology.start_core.network.packets.CPacketSetMultitoolMode;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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

    @Mod.EventBusSubscriber(modid = StarTCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen != null || event.getAction() != GLFW.GLFW_PRESS ||
                    !OPEN_SELECTOR.matches(event.getKey(), event.getScanCode())) {
                return;
            }
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
            if (held == null) {
                return;
            }
            StarTMultitoolMode mode = StarTMultitoolMode.get(held.stack()).offset(event.getScrollDelta() > 0 ? -1 : 1);
            selectMode(held.stack(), held.hand(), mode);
            event.setCanceled(true);
        }
    }

    static HeldMultitool getHeldMultitool() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return null;
        }
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
        StarTMultitoolItem.setMode(stack, mode);
        StarTNetwork.NETWORK.sendToServer(new CPacketSetMultitoolMode(mode, hand));
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable("item.start_core.gregtech_multitool.mode",
                    mode.displayName()), true);
        }
    }

    record HeldMultitool(ItemStack stack, InteractionHand hand) {
    }
}
