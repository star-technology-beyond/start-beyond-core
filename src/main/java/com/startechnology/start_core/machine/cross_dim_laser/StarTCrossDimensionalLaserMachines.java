package com.startechnology.start_core.machine.cross_dim_laser;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
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

public class StarTCrossDimensionalLaserMachines {

    public static MultiblockMachineDefinition CROSS_DIMENSIONAL_LASER = StarTCore.START_REGISTRATE
        .multiblock("cross_dimensional_laser", StarTCrossDimensionalLaserMachine::new)
        .appearanceBlock(() -> GTBlocks.ADVANCED_COMPUTER_CASING.get())
        .recipeType(GTRecipeTypes.DUMMY_RECIPES)
        .rotationState(RotationState.NON_Y_AXIS)
        .langValue("Cross-Dimensional Laser Tunneling Array [CDLTA]")
        .tooltips(
            Component.translatable("block.start_core.cross_dimensional_laser_line"),
            Component.translatable("start_core.cross_dimensional_laser_line.line0"),
            Component.translatable("block.start_core.breaker_line"),
            Component.translatable("start_core.cross_dimensional_laser_line.line1"),
            Component.translatable("start_core.cross_dimensional_laser_line.line2"),
            Component.translatable("block.start_core.breaker_line"),
            Component.translatable("start_core.cross_dimensional_laser_line.line3"),
            Component.translatable("start_core.cross_dimensional_laser_line.line4"),
            Component.empty(),
            Component.translatable("start_core.cross_dimensional_laser_line.line5"),
            Component.translatable("block.start_core.breaker_line")
        )
        .pattern(definition -> FactoryBlockPattern.start()
            .aisle("######ABA######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############") 
            .aisle("#####AAAAA#####", "######CCC######", "######DID######", "#######D#######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############") 
            .aisle("######AAA######", "######CCC######", "######CCC######", "######DDD######", "#######B#######", "#######B#######", "#######B#######", "######EFE######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############") 
            .aisle("#######A#######", "#######G#######", "######HCH######", "#######H#######", "###############", "###############", "###############", "#####E####E####", "#######B#######", "#######B#######", "#######B#######", "######EFE######", "###############", "###############", "###############", "###############") 
            .aisle("###############", "#######G#######", "######HCH######", "#######H#######", "###############", "###############", "###############", "###########E###", "###############", "###############", "###############", "###############", "####E##B##E####", "#######B#######", "#######B#######", "######EEE######") 
            .aisle("#A###########A#", "######HGH######", "#####HHCHH#####", "######HHH######", "#######H#######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############") 
            .aisle("AAA###AAA###AAA", "#CC##HDDDH##CC#", "#DCHHHDCDHHHDD#", "##D##HDDDH##D##", "#######D#######", "#######G#######", "#######D#######", "##E####G####E##", "#######D#######", "###############", "###############", "###E#######E###", "###############", "###############", "###############", "####E#####E####") 
            .aisle("BAAA##AAA##AAAB", "#CCGGGDDDGGGCC#", "#ICCCCCCCCCCCI#", "#DDHHHDCDHHHDD#", "##B##HDKDH##B##", "##B###GKG###B##", "##B###DKD###B##", "##F###GKG###F##", "###B##DKD##B###", "###B###K###B###", "###B#######B###", "###F###F###F###", "####B#####B####", "####B##F##B####", "####B#####B####", "####E##F##E####") 
            .aisle("AAA###AAA###AAA", "#CC##HDDDH##CC#", "#DCHHHDCDHHHCD#", "##D##HDDDH##D##", "#######D#######", "#######G#######", "#######D#######", "##E####G####E##", "#######D#######", "###############", "###############", "###E#######E###", "###############", "###############", "###############", "####E#####E####") 
            .aisle("#A###########A#", "######HGH######", "#####HHCHH#####", "######HHH######", "#######H#######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############") 
            .aisle("###############", "#######G#######", "######HCH######", "#######H#######", "###############", "###############", "###############", "###E#######E###", "###############", "###############", "###############", "###############", "####E##B##E####", "#######B#######", "#######B#######", "######EEE######") 
            .aisle("#######A#######", "#######G#######", "######HCH######", "#######H#######", "###############", "###############", "###############", "####E#####E####", "#######B#######", "#######B#######", "#######B#######", "######EFE######", "###############", "###############", "###############", "###############") 
            .aisle("######AAA######", "######CCC######", "######CCC######", "######DDD######", "#######B#######", "#######B#######", "#######B#######", "######EFE######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############") 
            .aisle("#####AAAAA#####", "######CCC######", "######D@D######", "#######D#######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############") 
            .aisle("######ABA######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############") 
            .where("#", Predicates.any())
            .where("A", Predicates.blocks(GTBlocks.CASING_HSSE_STURDY.get()))
            .where("I", Predicates.blocks(StarTMachineUtils.getKjsBlock("prismalium_casing"))
                .or(Predicates.blocks(GTMachines.ITEM_IMPORT_BUS[GTValues.ULV].getBlock()).setExactLimit(1))
                .or(Predicates.abilities(
                    PartAbility.INPUT_LASER,
                    PartAbility.OUTPUT_LASER
                ).setExactLimit(1))
            )
            .where("B", Predicates.blocks(StarTMachineUtils.getKjsBlock("prismalium_casing")))
            .where("C", Predicates.blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
            .where("D", Predicates.blocks(GTBlocks.HIGH_POWER_CASING.get()))
            .where("E", Predicates.blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
            .where("F", Predicates.blocks(GTBlocks.FUSION_COIL.get()))
            .where("G", Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTCEuAPI.materialManager.getMaterial("europium"))))
            .where("H", Predicates.blocks(StarTMachineUtils.getKjsBlock("reinforced_fusion_glass")))
            .where("K", Predicates.blocks(StarTMachineUtils.getKjsBlock("superalloy_casing")))
            .where("@", Predicates.controller(Predicates.blocks(definition.get())))
            .build()
        ).sidedWorkableCasingModel(
            GTCEu.id("block/casings/hpca/advanced_computer_casing"),
            StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
        )
        .register();

        public static final void init() {};


}
