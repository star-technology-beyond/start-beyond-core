package com.startechnology.start_core.network.packets;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import com.startechnology.start_core.item.StarTItems;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolItems;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CPacketReleaseSingleBlockLock implements IPacket {

    private int handOrdinal;

    public CPacketReleaseSingleBlockLock(InteractionHand hand) {
        this.handOrdinal = hand.ordinal();
    }

    public CPacketReleaseSingleBlockLock() {}

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

        // remove the uuid from swing lock to permit them to
        // continue breaking blocks again
        StarTMultitoolItem.clearSwingLock(player.getUUID());
    }
}