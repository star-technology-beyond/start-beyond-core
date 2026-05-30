package com.startechnology.start_core.network.packets;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.startechnology.start_core.item.StarTItems;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolItems;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CPacketToggleSingleBlockMode implements IPacket {

    private int handOrdinal;

    public CPacketToggleSingleBlockMode(InteractionHand hand) {
        this.handOrdinal = hand.ordinal();
    }

    public CPacketToggleSingleBlockMode() {}

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(handOrdinal);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        handOrdinal = buf.readVarInt();
    }

    @Override
    public void execute(IHandlerContext handler) {
        Player player = handler.getPlayer();
        if (player == null) return;
        if (handOrdinal < 0 || handOrdinal >= InteractionHand.values().length) return;

        // get the stack in the hand of the player to toggle single block mode in
        InteractionHand hand = InteractionHand.values()[handOrdinal];
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof StarTMultitoolItem)) return;

        // single block mode toggled !
        StarTMultitoolMode.toggleSingleBlockMode(stack);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }
    }
}