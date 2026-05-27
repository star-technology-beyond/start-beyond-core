package com.startechnology.start_core.machine.wind_turbine;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StarTWindTurbineBearingBlockEntity extends WindmillBearingBlockEntity {
    
    public StarTWindTurbineBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}