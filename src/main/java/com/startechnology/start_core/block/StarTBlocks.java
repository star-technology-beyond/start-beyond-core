package com.startechnology.start_core.block;

import com.startechnology.start_core.block.fusion.StarTFusionBlocks;
import com.startechnology.start_core.block.solar.StarTSolarCellBlocks;
import com.startechnology.start_core.machine.wind_turbine.StarTWindTurbineBlocks;

public class StarTBlocks {

    public static void init() {
        StarTFusionBlocks.init();
        StarTSolarCellBlocks.init();
        StarTWindTurbineBlocks.init();
    }

}
