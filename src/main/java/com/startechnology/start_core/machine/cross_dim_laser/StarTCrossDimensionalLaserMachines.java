package com.startechnology.start_core.machine.cross_dim_laser;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
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
            .aisle("#AA#BBB#AA#", "AAA#BBB#AAA", "AAA#BBB#AAA", "#####C#####", "BBB#CCC#BBB", "BBBCCCCCBBB", "BBB#CCC#BBB", "#####C#####", "AAA#BBB#AAA", "AAA#BBB#AAA", "#AA#BBB#AA#") 
            .aisle("AAA#BBB#AAA", "ADDDDDDDDDA", "ADA#BDB#ADA", "#D##CDC##D#", "BDBCCDCCBDB", "BDDDDEDDDDB", "BDBCCDCCBDB", "#D##CDC##D#", "ADA#BDB#ADA", "ADDDDDDDADA", "AAA#DBD#AAA") 
            .aisle("#AA#BBB#AA#", "AAA#BFB#AAA", "AAA#BBB#AAA", "#####C#####", "BBB#CCC#BBB", "BBBCC#CCBBB", "BBB#CCC#BBB", "#####C#####", "AAA#BDB#AAA", "AAA#DGD#AAA", "#AA#DBD#AA#") 
            .where("#", Predicates.any())
            .where("A", Predicates.blocks(GTBlocks.HIGH_POWER_CASING.get()))
            .where("B", Predicates.blocks(GTBlocks.COMPUTER_CASING.get()))
            .where("C", Predicates.blocks(StarTMachineUtils.getKjsBlock("reinforced_fusion_glass")))
            .where("D", Predicates.blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
            .where("E", Predicates.abilities(PartAbility.INPUT_LASER)
                .or(Predicates.abilities(PartAbility.OUTPUT_LASER))
                .setExactLimit(1))
            .where("@", Predicates.controller(Predicates.blocks(definition.get())))
            .where("G", Predicates.blocks(GTMachines.ITEM_IMPORT_BUS[GTValues.ULV].getBlock()))
            .build()
        ).workableCasingModel(
            GTCEu.id("block/casings/hpca/computer_casing"),
            GTCEu.id("block/multiblock/hpca")
        )
        .register();

        public static final void init() {};


}
