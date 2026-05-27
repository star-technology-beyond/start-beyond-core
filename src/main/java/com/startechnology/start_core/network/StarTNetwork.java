package com.startechnology.start_core.network;

import com.lowdragmc.lowdraglib.networking.INetworking;
import com.lowdragmc.lowdraglib.networking.forge.LDLNetworkingImpl;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.network.packets.CPacketSetMultitoolMode;

public class StarTNetwork {
    public static final INetworking NETWORK = LDLNetworkingImpl.createNetworking(
            StarTCore.resourceLocation("network"), "0.0.1");

    public static void init() {
        NETWORK.registerC2S(CPacketSetMultitoolMode.class);
    }
}
