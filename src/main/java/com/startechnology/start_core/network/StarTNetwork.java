package com.startechnology.start_core.network;

import com.lowdragmc.lowdraglib.networking.INetworking;
import com.lowdragmc.lowdraglib.networking.forge.LDLNetworkingImpl;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.network.packets.CPacketSetMultitoolMode;
import com.startechnology.start_core.network.packets.CPacketUninstallMultitoolMode;
import com.startechnology.start_core.network.packets.CPacketMiddleClickAutoSelect;
import com.startechnology.start_core.network.packets.CPacketReleaseSingleBlockLock;
import com.startechnology.start_core.network.packets.CPacketSaveAutoSelectRules;
import com.startechnology.start_core.network.packets.CPacketToggleSingleBlockMode;

public class StarTNetwork {
    public static final INetworking NETWORK = LDLNetworkingImpl.createNetworking(
            StarTCore.resourceLocation("network"), "0.0.1");

    public static void init() {
        NETWORK.registerC2S(CPacketSetMultitoolMode.class);
        NETWORK.registerC2S(CPacketUninstallMultitoolMode.class);
        NETWORK.registerC2S(CPacketReleaseSingleBlockLock.class);
        NETWORK.registerC2S(CPacketToggleSingleBlockMode.class);
        NETWORK.registerC2S(CPacketSaveAutoSelectRules.class);
        NETWORK.registerC2S(CPacketMiddleClickAutoSelect.class);
    }
}
