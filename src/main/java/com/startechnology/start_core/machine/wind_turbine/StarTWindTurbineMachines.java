package com.startechnology.start_core.machine.wind_turbine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.startechnology.start_core.StarTCore;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;

import static com.startechnology.start_core.StarTCore.START_REGISTRATE;

public class StarTWindTurbineMachines {
    public static final MultiblockMachineDefinition WIND_TURBINE_LV = registerWindTurbine(
        GTValues.LV,
        "Basic ahh wind turbine",
        GTBlocks.CASING_BRONZE_BRICKS,
        GTBlocks.CASING_BRONZE_GEARBOX,
        "bronze",
        GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks")
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
        Supplier<Block> gearbox,
        String frameMaterial,
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
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                .aisle("CCC", "C@C", "CCC")
                .where("C", Predicates.blocks(casing.get())
                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                    .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1)))
                .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                .build()
            )
            .shapeInfos(definition -> List.of(MultiblockShapeInfo.builder()
                .aisle("CCC", "I@E", "CCC")
                .where('C', casing.get())
                .where('I', GTMachines.FLUID_IMPORT_HATCH[tier], Direction.SOUTH)
                .where('E', GTMachines.ENERGY_OUTPUT_HATCH[tier], Direction.SOUTH)
                .where('@', definition, Direction.NORTH)
                .build()))
            .workableCasingModel(
                casingTexture,
                // StarTCore.resourceLocation("block/wind_turbine/overlay")
                StarTCore.resourceLocation("block/solar/overlay/ev")
            )
            .register();
    }

    public static void init() {
    }
}
