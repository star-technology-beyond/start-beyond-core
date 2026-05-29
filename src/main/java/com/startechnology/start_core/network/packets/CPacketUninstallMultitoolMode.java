package com.startechnology.start_core.network.packets;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CPacketUninstallMultitoolMode implements IPacket {

    private String toolTypeName;
    private int handOrdinal;

    public CPacketUninstallMultitoolMode(StarTMultitoolMode mode, InteractionHand hand) {
        this.toolTypeName = mode.toolType().name;
        this.handOrdinal = hand.ordinal();
    }

    public CPacketUninstallMultitoolMode() {
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

        // Get the tool in the players hand that did this interaction
        InteractionHand hand = InteractionHand.values()[handOrdinal];
        ItemStack multitool = player.getItemInHand(hand);
        if (!(multitool.getItem() instanceof StarTMultitoolItem))
            return;

        // ensure that its a valid multitool mode that we can
        // even eject to so the player doesnt eject out 9 billion neutronium
        GTToolType type = GTToolType.getTypes().get(toolTypeName);
        if (type == null)
            return;
        if (!StarTMultitoolMode.isInstalled(multitool, type))
            return;

        Material material = StarTMultitoolMode.getMaterialForType(multitool, type);
        if (material == null)
            return;

        StarTMultitoolMode.uninstall(multitool, type);

        // eject from the player, which will either add to the inventory
        // or if it cant, then drop the item out
        ItemStack ejected = ToolHelper.get(type, material);
        if (!player.getInventory().add(ejected)) {
            player.drop(ejected, false);
        }
    }
}