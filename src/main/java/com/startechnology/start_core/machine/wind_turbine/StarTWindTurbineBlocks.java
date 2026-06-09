package com.startechnology.start_core.machine.wind_turbine;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllContraptionTypes;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.startechnology.start_core.machine.wind_turbine.client.StarTWindTurbineBearingRenderer;
import com.startechnology.start_core.machine.StarTMachineUtils;

import net.minecraft.core.Registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.resources.ResourceLocation;

import static com.startechnology.start_core.StarTCore.START_REGISTRATE;

import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;

public class StarTWindTurbineBlocks {
    public static final BlockEntry<StarTWindTurbineBearingBlock> WIND_TURBINE_BEARING =
        START_REGISTRATE
            .block("wind_turbine_bearing", StarTWindTurbineBearingBlock::new)
            .initialProperties(() -> AllBlocks.WINDMILL_BEARING.get())
            .blockstate((ctx, prov) -> {})
            .item()
            .model((ctx, prov) ->
                prov.withExistingParent(
                    ctx.getName(),
                    new ResourceLocation("create", "item/windmill_bearing")
                )
            )
            .build()
            .register();

    public static final BlockEntityEntry<StarTWindTurbineBearingBlockEntity> WIND_TURBINE_BEARING_BE =
        START_REGISTRATE
            .blockEntity("wind_turbine_bearing", StarTWindTurbineBearingBlockEntity::new)
            .renderer(() -> StarTWindTurbineBearingRenderer::new)
            .validBlocks(WIND_TURBINE_BEARING)
            .register();

    public static void init() {
    }
}
