package com.startechnology.start_core.machine.wind_turbine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.machine.StarTMachineUtils;
import dev.latvian.mods.kubejs.KubeJS;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

import static com.startechnology.start_core.StarTCore.START_REGISTRATE;

public class StarTWindTurbineMachines {
    public static final MultiblockMachineDefinition WIND_TURBINE_LV = registerWindTurbine(
        GTValues.LV,
        "Wind Turbine",
        () -> StarTMachineUtils.getKjsBlock("high_steam_machine_casing"),
        KubeJS.id("block/casings/basic/high_steam_machine_casing")
    );

    //  public static final MultiblockMachineDefinition WIND_TURBINE_MV = registerWindTurbine(
    //     GTValues.LV,
    //     "Mediuim ahh wind turbine",
    //     GTBlocks.CASING_BRONE_BRICKS.get(),
    //     GTBlocks.CASING_BRONZE_GEARBOX.get(),
    //     "bronze",
    //     GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks")
    // );

    //  public static final MultiblockMachineDefinition WIND_TURBINE_HV = registerWindTurbine(
    //     GTValues.LV,
    //     "High ahh wind turbine",
    //     GTBlocks.CASING_BRONE_BRICKS.get(),
    //     GTBlocks.CASING_BRONZE_GEARBOX.get(),
    //     "bronze",
    //     GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks")
    // );

    private static MultiblockMachineDefinition registerWindTurbine(
        int tier,
        String langValue,
        Supplier<Block> casing,
        ResourceLocation casingTexture
    ) {
        return START_REGISTRATE
            .multiblock(
                GTValues.VN[tier].toLowerCase() + "_wind_turbine",
                holder -> new StarTWindTurbineMachine(holder, tier)
            )
            .langValue("%s %s".formatted(GTValues.VNF[tier] + "§r", langValue))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(casing)
            .tooltips(
                Component.translatable("block.start_core.wind_controller.line"),
                Component.translatable("start_core.wind_controller.line0"),
                Component.translatable("block.start_core.breaker_line"),
                Component.translatable("start_core.wind_controller.line1"),
                Component.translatable("start_core.wind_controller.line2"),
                Component.translatable("block.start_core.breaker_line"),
                Component.translatable("start_core.wind_controller.line3"),
                Component.translatable("start_core.wind_controller.line4"),
                Component.empty(),
                Component.translatable("start_core.wind_controller.line5"),
                Component.translatable("block.start_core.breaker_line")
            )
            .pattern(definition -> FactoryBlockPattern.start()
                .aisle("##ABA##", "###C###", "#######", "#######", "#######", "#######", "###D###", "#######", "#######", "#######")
                .aisle("##AEA##", "##CAC##", "###A###", "###A###", "###A###", "###A###", "###W###", "#######", "#######", "#######")
                .aisle("##AAA##", "###@###", "#######", "###FGH#", "HGGFGH#", "HGGFGH#", "FFFMFFF", "#HGFGGH", "#HGFGGH", "#HGF###")
                .where("#", Predicates.any())
                .where("A", Predicates.blocks(StarTMachineUtils.getKjsBlock("high_steam_machine_casing")))
                .where("B", Predicates.blocks(GTMachines.ENERGY_OUTPUT_HATCH[tier].getBlock()))
                .where("C", Predicates.blocks(GTBlocks.STEEL_BRICKS_HULL.get()))
                .where("D", Predicates.blocks(GTBlocks.BRONZE_HULL.get()))
                .where("W", StarTWindTurbinePredicates.windTurbineBearing())
                .where("E", Predicates.blocks(GTMachines.FLUID_IMPORT_HATCH[tier].getBlock()))
                .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                .where("F", StarTWindTurbinePredicates.windTurbineBlade(
                    StarTMachineUtils.getGTCEuBlock("bronze_frame")))
                .where("G", StarTWindTurbinePredicates.windTurbineBlade(
                    GTBlocks.TREATED_WOOD_FENCE_GATE.get()))
                .where("H", StarTWindTurbinePredicates.windTurbineBlade(
                    GTBlocks.TREATED_WOOD_FENCE.get()))
                .where("M", StarTWindTurbinePredicates.windTurbineBlade(
                    GCYMBlocks.CASING_INDUSTRIAL_STEAM.get()))
                .build()
            )
            .workableCasingModel(
                casingTexture,
                StarTCore.resourceLocation("block/overlay/wind_turbine")
            )
            .register();
    }

    public static void init() {
    }
}
