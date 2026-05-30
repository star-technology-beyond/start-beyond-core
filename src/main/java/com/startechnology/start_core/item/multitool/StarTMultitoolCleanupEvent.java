package com.startechnology.start_core.item.multitool;

import com.startechnology.start_core.StarTCore;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarTCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StarTMultitoolCleanupEvent {

    // we dont want to hold the lock and leak memory if a player leaves
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        StarTMultitoolItem.clearSwingLock(event.getEntity().getUUID());
    }
}
