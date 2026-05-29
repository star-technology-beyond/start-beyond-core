package com.startechnology.start_core.network.packets;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CPacketSetMultitoolMode implements IPacket {

    private String toolTypeName;
    private int handOrdinal;

    public CPacketSetMultitoolMode(StarTMultitoolMode mode, InteractionHand hand) {
        this.toolTypeName = mode.toolType().name;
        this.handOrdinal = hand.ordinal();
    }

    public CPacketSetMultitoolMode() {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(toolTypeName);
        buf.writeVarInt(handOrdinal);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        toolTypeName = buf.readUtf();
        handOrdinal = buf.readVarInt();
    }

    @Override
    public void execute(IHandlerContext handler) {
        Player player = handler.getPlayer();
        if (player == null) 
            return;

        if (handOrdinal < 0 || handOrdinal >= InteractionHand.values().length) 
            return;

        // get the item in the player hand that executed this
        InteractionHand hand = InteractionHand.values()[handOrdinal];
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof StarTMultitoolItem)) return;

        // ensure that its a valid multitool mode that we can
        // even swap to so the player doesnt cheat in neutronium evil
        // obliterator 9000 in ueuvuvluv
        GTToolType type = GTToolType.getTypes().get(toolTypeName);
        if (type == null) return;
        if (!StarTMultitoolMode.isInstalled(stack, type)) return;

        StarTMultitoolMode.setActive(stack, new StarTMultitoolMode(type,
                StarTMultitoolMode.getMaterialForType(stack, type)));

        // sync the updated item NBT back to the client so the client-side stack
        // has the correct behaviors tag
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }
    }
}