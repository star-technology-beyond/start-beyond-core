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

        // get active mode from uninstalling for updating
        // item variant
        StarTMultitoolMode activeNow = StarTMultitoolMode.getActive(multitool);
        ItemStack newStack;

        if (activeNow == null) {
            // revert to empty
            newStack = new ItemStack(StarTMultitoolItems.MULTITOOL_EMPTY.get());
        } else {
            // new id
            var nextItemEntry = StarTMultitoolItems.MULTITOOLS.get(activeNow.toolType());
            newStack = new ItemStack(nextItemEntry != null ? nextItemEntry.get() : StarTMultitoolItems.MULTITOOL_EMPTY.get());
        }

        // copy over nbt
        if (multitool.hasTag()) {
            newStack.setTag(multitool.getTag().copy());
        }

        // overwrite item in hand with new stack
        player.setItemInHand(hand, newStack);

        // sync back to client
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }

        // get the exact tool entry to eject
        var toolEntry = GTMaterialItems.TOOL_ITEMS.get(material, type);
        if (toolEntry == null) return;

        // reference item for ejecting
        IGTTool gtTool = (IGTTool) toolEntry.get().asItem();
        ItemStack ejected;

        // we need to preserve energy emptiness if
        // its an electric item, but for that we need
        // to retrieve the max energy from the power unit 
        if (gtTool.isElectric()) {
            ItemStack powerUnit = StarTItems.EXTENDED_POWER_UNITS.get(gtTool.getElectricTier()).asStack();
            IElectricItem powerUnitCap = GTCapabilityHelper.getElectricItem(powerUnit);
            long maxCharge = powerUnitCap != null ? powerUnitCap.getMaxCharge() : GTValues.V[gtTool.getElectricTier()] * 100L;
            ejected = gtTool.get(0L, maxCharge);
        } else {
            ejected = gtTool.get();
        }

        if (!player.getInventory().add(ejected)) {
            player.drop(ejected, false);
        }
    }
}