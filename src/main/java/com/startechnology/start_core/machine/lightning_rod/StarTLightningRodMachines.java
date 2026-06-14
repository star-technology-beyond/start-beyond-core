package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.startechnology.start_core.machine.StarTMachineUtils;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.GTValues.LV;


public class StarTLightningRodMachines {
    public static MultiblockMachineDefinition[] LIGHTING_RODS = StarTMachineUtils.registerTieredMultis(
            "lightning_rod",
            StarTLightningRodMachine::new,
            StarTLightningRodMachines::buildLightingRod,
            LV, MV, HV, EV
        );

        private static MultiblockMachineDefinition buildLightingRod(
            int tier,
            MultiblockMachineBuilder builder
        ){
            return builder
                .langValue("%s Lighting Rod".formatted(GTValues.VNF[tier] + "§r"))
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .pattern(definition -> switch (tier) {
                    case MV -> FactoryBlockPattern.start()
                        .aisle("AAA")
                        .aisle("B@A")
                        .aisle("AAA")
                        .where("A", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("aluminium_frame")))
                        .where("B", Predicates.ability(PartAbility.OUTPUT_ENERGY))
                        .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
                    case HV -> FactoryBlockPattern.start()
                        .aisle("AAA")
                        .aisle("B@A")
                        .aisle("AAA")
                        .where("A", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("stainless_steel_frame")))
                        .where("B", Predicates.ability(PartAbility.OUTPUT_ENERGY))
                        .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
                    case EV -> FactoryBlockPattern.start()
                        .aisle("AAA")
                        .aisle("B@A")
                        .aisle("AAA")
                        .where("A", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("black_steel_frame")))
                        .where("B", Predicates.ability(PartAbility.OUTPUT_ENERGY))
                        .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
                    default -> FactoryBlockPattern.start()
                        .aisle("AAA")
                        .aisle("B@A")
                        .aisle("AAA")
                        .where("A", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("steel_frame")))
                        .where("B", Predicates.ability(PartAbility.OUTPUT_ENERGY))
                        .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
                })

                .register();

        }
    public static void init() {
    }
}
