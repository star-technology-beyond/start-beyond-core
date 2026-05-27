package com.startechnology.start_core.integration.jade.provider;

import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.api.capability.StarTCapabilityHelper;
import com.startechnology.start_core.machine.abyssal_harvester.StarTAbyssalHarvesterMachine;
import com.startechnology.start_core.machine.cross_dim_laser.CrossDimensionalLaserSavedData;
import com.startechnology.start_core.machine.cross_dim_laser.StarTCrossDimensionalLaserMachine;
import com.startechnology.start_core.machine.cross_dim_laser.StarTCrossDimensionalLaserMachine.CrossDimensionalLaserDirection;
import com.startechnology.start_core.machine.cross_dim_laser.StarTCrossDimensionalLaserMachine.LinkedStatus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class StarTCrossDimensionalLaserProvider extends CapabilityBlockProvider<StarTCrossDimensionalLaserMachine> {
    
    public StarTCrossDimensionalLaserProvider() {
        super(StarTCore.resourceLocation("cross_dimensional_laser"));
    }

    @Override
    protected @Nullable StarTCrossDimensionalLaserMachine getCapability(Level level, BlockPos pos,
            @Nullable Direction side) {
        return StarTCapabilityHelper.getCrossDimensionalLaserMachine(level, pos, side);
    }

    @Override
    protected void write(CompoundTag data, StarTCrossDimensionalLaserMachine capability) {
        CrossDimensionalLaserDirection direction = capability.getDirection();

        if (!Objects.isNull(direction)) {
            data.putInt("direction", direction.ordinal());
        }
        
        LinkedStatus linkedStatus = capability.getLinkStatus();

        if (!Objects.isNull(linkedStatus)) {
            data.putInt("link_status", linkedStatus.ordinal());
        }

        capability.getPartnerGlobalPos().ifPresent(
            parnter -> {
                data.put("partner_pos", CrossDimensionalLaserSavedData.writeGlobalPos(parnter));
            }
        );
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
            BlockEntity blockEntity, IPluginConfig config) {
        if (capData.contains("direction"))
        {
            int direction_ordinal = capData.getInt("direction");
            CrossDimensionalLaserDirection direction = CrossDimensionalLaserDirection.values()[direction_ordinal];

            tooltip.add(Component.translatable(direction == CrossDimensionalLaserDirection.SENDER
                ? "ui.start_core.cross_dimensional_laser.sender"
                : "ui.start_core.cross_dimensional_laser.receiver"));
        }

        if (capData.contains("link_status"))
        {
            int link_status_ordinal = capData.getInt("link_status");
            LinkedStatus link_status = LinkedStatus.values()[link_status_ordinal];

            tooltip.add(Component.translatable(link_status == LinkedStatus.Linked
                ? "ui.start_core.cross_dimensional_laser.linked"
                : "ui.start_core.cross_dimensional_laser.unlinked"));
            
            if (capData.contains("partner_pos") && link_status == LinkedStatus.Linked) {
                CompoundTag partner_pos_tag = capData.getCompound("partner_pos");
                GlobalPos partner = CrossDimensionalLaserSavedData.readGlobalPos(partner_pos_tag);

                tooltip.add(
                        Component.translatable(
                                "ui.start_core.cross_dimensional_laser.linked_location_dim",
                                partner.dimension().location()));

                tooltip.add(
                        Component.translatable(
                                "ui.start_core.cross_dimensional_laser.linked_location_coords",
                                partner.pos().getX(),
                                partner.pos().getY(),
                                partner.pos().getZ()));
            
            }
        }
    }
    
}
