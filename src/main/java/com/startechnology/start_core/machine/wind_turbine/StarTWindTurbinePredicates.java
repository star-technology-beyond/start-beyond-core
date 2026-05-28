package com.startechnology.start_core.machine.wind_turbine;

import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import java.util.List;
import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


import java.util.function.Predicate;

public class StarTWindTurbinePredicates {

    public static final String BEARING_KEY = "windTurbineBearing";
    public static final String CONTRAPTION_ACTIVE_KEY = "windTurbineBearingRunning";
    public static final String BLADE_POSITIONS_KEY = "bladePositions";

    private static Predicate<MultiblockState> createBearingPredicate() {
        return (MultiblockState blockWorldState) -> {
            BlockEntity be = blockWorldState.getTileEntity();
            if (be instanceof StarTWindTurbineBearingBlockEntity bearing) {
                blockWorldState.getMatchContext().set(BEARING_KEY, bearing.getBlockPos());
                // Set whether contraption is currently assembled
                // This is important for the predicates to ensure we
                // dont instantly unassemble on the bearing being assembled
                blockWorldState.getMatchContext().set(
                    CONTRAPTION_ACTIVE_KEY, 
                    bearing.isRunning()
                );
                return true;
            }
            return false;
        };
    }

    public static TraceabilityPredicate windTurbineBearing() {
        return new TraceabilityPredicate(createBearingPredicate(), () -> new BlockInfo[]{
            new BlockInfo(StarTWindTurbineBlocks.WIND_TURBINE_BEARING.get().defaultBlockState())
        });
    }

    public static TraceabilityPredicate windTurbineBlade(Block... blocks) {
        return new TraceabilityPredicate(blockWorldState -> {
            BlockState state = blockWorldState.getBlockState();
            for (Block block : blocks) {
                if (state.is(block)) {
                    // collect position into context for passing to the bearing
                    List<BlockPos> positions = blockWorldState.getMatchContext()
                            .getOrDefault(BLADE_POSITIONS_KEY, new ArrayList<>());
                    positions.add(blockWorldState.getPos());
                    blockWorldState.getMatchContext().set(BLADE_POSITIONS_KEY, positions);
                    return true;
                }
            }

            Boolean contraptionActive = blockWorldState.getMatchContext()
                    .getOrDefault(CONTRAPTION_ACTIVE_KEY, Boolean.FALSE);
            return contraptionActive;
        }, () -> new BlockInfo[]{ new BlockInfo(blocks[0].defaultBlockState()) });
    }
}