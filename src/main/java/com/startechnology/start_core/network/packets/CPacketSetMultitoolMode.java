package com.startechnology.start_core.network.packets;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CPacketSetMultitoolMode implements IPacket {
    private int modeOrdinal;
    private int handOrdinal;

    public CPacketSetMultitoolMode(StarTMultitoolMode mode, InteractionHand hand) {
        this.modeOrdinal = mode.ordinal();
        this.handOrdinal = hand.ordinal();
    }

    public CPacketSetMultitoolMode() {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(modeOrdinal);
        buf.writeVarInt(handOrdinal);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        modeOrdinal = buf.readVarInt();
        handOrdinal = buf.readVarInt();
    }

    @Override
    public void execute(IHandlerContext handler) {
        Player player = handler.getPlayer();
        if (player == null || modeOrdinal < 0 || modeOrdinal >= StarTMultitoolMode.VALUES.length ||
                handOrdinal < 0 || handOrdinal >= InteractionHand.values().length) {
            return;
        }
        ItemStack stack = player.getItemInHand(InteractionHand.values()[handOrdinal]);
        if (stack.getItem() instanceof StarTMultitoolItem) {
            StarTMultitoolItem.setMode(stack, StarTMultitoolMode.VALUES[modeOrdinal]);
        }
    }
}
