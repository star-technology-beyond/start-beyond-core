package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.block.fusion.StarTFusionBlocks;
import com.startechnology.start_core.machine.StarTMachineUtils;
import com.startechnology.start_core.machine.StarTPartAbility;
import dev.latvian.mods.kubejs.KubeJS;
import net.minecraft.resources.ResourceLocation;


import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.startechnology.start_core.StarTCore.START_REGISTRATE;


public class StarTDysonSwarmMachines {

    private static final ResourceLocation[] railgunModuleIds = { StarTCore.resourceLocation("uhv_dyson_railgun_module"), StarTCore.resourceLocation("uev_dyson_railgun_module"), StarTCore.resourceLocation("uiv_dyson_railgun_module") };
    private static final ResourceLocation[] collectorModuleIds = { StarTCore.resourceLocation("uhv_dyson_collector_module"), StarTCore.resourceLocation("uev_dyson_collector_module"), StarTCore.resourceLocation("uiv_dyson_collector_module") };

    public static final MultiblockMachineDefinition T1_STELLAR_RAILGUN = START_REGISTRATE
            .multiblock("uhv_dyson_railgun_module", (holder) -> new StarTDysonSwarmRailgunModule(holder, UHV, StarTCore.resourceLocation("dyson_swarm_monitor")))
            .langValue("Basic Stellar Railgun Module [BSRM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(() -> StarTMachineUtils.getKjsBlock("superdense_machine_casing"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" A   A ", " A B A ", "   B   ", "   B   ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("AC A CA", "ADDDDDA", " D E D ", " D E D ", " D E D ", " D E D ", "   E   ", "       ", "   E   ", "       ", "       ", "       ", "       ", "       ")
                    .aisle(" A A A ", " DFFFD ", "       ", " G   G ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("  AHA  ", " DFIFD ", " E   E ", " E   E ", " E   E ", " E   E ", " E   E ", "       ", " I   I ", "       ", " E   E ", "       ", "       ", "       ")
                    .aisle(" A A A ", " DFFFD ", " D   D ", " D   D ", "       ", " G   G ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("ACAGACA", "AAGJGAA", " A G A ", " DD@DD ", " D G D ", " DGGGD ", " D G D ", " DGGGD ", "   G   ", "  GGG  ", "   G   ", "   G   ", "   G   ", "   G   ")
                    .aisle(" A   A ", " A   A ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .where(" ", Predicates.any())
                    .where("A", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("hsse_frame")))
                    .where("B", Predicates.blocks(StarTMachineUtils.getKjsBlock("enriched_naquadah_engine_intake_casing")))
                    .where("C", Predicates.blocks(StarTMachineUtils.getKjsBlock("enriched_naquadah_pipe_casing")))
                    .where("D", Predicates.blocks(StarTMachineUtils.getKjsBlock("superdense_machine_casing"))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(StarTPartAbility.ABSOLUTE_PARALLEL_HATCH).setMaxGlobalLimited(1)))
                    .where("E", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("superconducting_coil")))
                    .where("F", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("atomic_casing")))
                    .where("G", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("neutronium_frame")))
                    .where("H", Predicates.abilities(StarTPartAbility.MODULAR_NODE_INTERFACE))
                    .where("I", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("high_power_casing")))
                    .where("J", Predicates.abilities(PartAbility.INPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            ).workableCasingModel(
                    KubeJS.id("block/casings/abydos_multis/superdense_machine_casing"),
                    StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
            ).register();

    public static final MultiblockMachineDefinition T2_STELLAR_RAILGUN = START_REGISTRATE
            .multiblock("uev_dyson_railgun_module", (holder) -> new StarTDysonSwarmRailgunModule(holder, UEV, StarTCore.resourceLocation("dyson_swarm_monitor")))
            .langValue("Advanced Stellar Railgun Module [ASRM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(() -> StarTMachineUtils.getKjsBlock("draneko_casing"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("       ", "   A   ", "   A   ", "   A   ", "   A   ", "   A   ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle(" AAAAA ", " B A B ", "  CDC  ", "       ", "       ", "  DCD  ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("A     A", "   E   ", "B     B", "B     B", "       ", "       ", "       ", "       ", "D     D", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("A  F  A", "  BBB  ", "G     G", "       ", "B     B", "B     B", "       ", "       ", "C     C", "       ", "       ", "D     D", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("       ", " EBEBE ", "G     G", "       ", "G     G", "       ", "B     B", "B     B", "D     D", "       ", "       ", "C     C", "       ", "       ", "D     D", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("A     A", "G BBB G", "       ", "G     G", "       ", "G     G", "       ", "       ", "B     B", "B     B", "B     B", "D     D", "       ", "       ", "C     C", "       ", "       ", "D     D", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("A     A", "A  E  A", "       ", "G     G", "       ", "B     B", "B     B", "B     B", "       ", "       ", "G     G", "B     B", "B     B", "B     B", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("GAA AAG", " AAEAA ", " B   B ", " B   B ", " B   B ", "  B B  ", " GB BG ", "  BAB  ", " G A G ", "  GAG  ", "   A   ", " G A G ", "  GAG  ", "   A   ", " B A B ", " BGAGB ", " B A B ", "  BAB  ", "  BAB  ", "  BAB  ", "   A   ", "   A   ", "   A   ", "   A   ")
                    .aisle(" GAHAG ", " GGAGG ", "  G@G  ", "  GAG  ", "   A   ", "   A   ", "   A   ", "   A   ", "   G   ", "   G   ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .where(" ", Predicates.any())
                    .where("A", Predicates.blocks(StarTMachineUtils.getKjsBlock("draneko_casing"))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(StarTPartAbility.ABSOLUTE_PARALLEL_HATCH).setMaxGlobalLimited(1)))
                    .where("B", Predicates.blocks(StarTMachineUtils.getKjsBlock("advanced_assembly_casing")))
                    .where("C", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("superconducting_coil")))
                    .where("D", Predicates.blocks(StarTFusionBlocks.ADVANCED_FUSION_COIL.get()))
                    .where("E", Predicates.blocks(StarTMachineUtils.getKjsBlock("noble_mixing_casing")))
                    .where("F", Predicates.abilities(StarTPartAbility.MODULAR_NODE_INTERFACE))
                    .where("G", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("astrenalloy_nx_frame")))
                    .where("H", Predicates.abilities(PartAbility.INPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            ).workableCasingModel(
                    KubeJS.id("block/casings/end_multis/draneko_casing"),
                    StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
            ).register();

    public static final MultiblockMachineDefinition T3_STELLAR_RAILGUN = START_REGISTRATE
            .multiblock("uiv_dyson_railgun_module", (holder) -> new StarTDysonSwarmRailgunModule(holder, UIV, StarTCore.resourceLocation("dyson_swarm_monitor")))
            .langValue("Advanced II Stellar Railgun Module [A2SRM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(() -> StarTMachineUtils.getKjsBlock("draneko_casing"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("A", "A", "A")
                    .aisle("C", "@", "B")
                    .aisle("A", "A", "A")
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(StarTPartAbility.ABSOLUTE_PARALLEL_HATCH).setMaxGlobalLimited(1)))
                    .where("B", Predicates.abilities(StarTPartAbility.MODULAR_NODE_INTERFACE))
                    .where("C", Predicates.abilities(PartAbility.INPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            ).workableCasingModel(
                    KubeJS.id("block/casings/end_multis/draneko_casing"),
                    StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
            ).register();

    public static final MultiblockMachineDefinition T1_PHOTONIC_ACCUMULATOR = START_REGISTRATE
            .multiblock("uhv_dyson_collector_module", (holder) -> new StarTDysonSwarmCollectorModule(holder, UHV, StarTCore.resourceLocation("dyson_swarm_monitor")))
            .langValue("Basic Photonic Accumulator Module [BPAM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .generator(true)
            .appearanceBlock(() -> StarTMachineUtils.getKjsBlock("superdense_machine_casing"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("     ", "  A  ", "  A  ", "  A  ", "     ", "     ", "     ", "     ", "     ")
                    .aisle("B   B", "BCCCB", "DEFED", "DEFED", "DEFED", "GCCCG", "H   H", "     ", "     ")
                    .aisle("  D  ", "CIEIC", "E   E", "E   E", "E   E", "CCCCC", " JKJ ", "  C  ", "     ")
                    .aisle(" DLD ", "CEEEC", "F M F", "F M F", "F M F", "CCFCC", " KFK ", " CFC ", "  F  ")
                    .aisle("  D  ", "CIEIC", "E   E", "E   E", "E   E", "CCCCC", " JKJ ", "  C  ", "     ")
                    .aisle("B   B", "BC@CB", "DEOED", "DEOED", "DEOED", "GCNCG", "H   H", "     ", "     ")
                    .where(" ", Predicates.any())
                    .where("A", Predicates.blocks(StarTMachineUtils.getKjsBlock("enriched_naquadah_engine_intake_casing")))
                    .where("B", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("nonconducting_casing")))
                    .where("C", Predicates.blocks(StarTMachineUtils.getKjsBlock("superdense_machine_casing")))
                    .where("D", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("neutronium_frame")))
                    .where("E", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("high_power_casing")))
                    .where("F", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("superconducting_coil")))
                    .where("G", Predicates.blocks(StarTMachineUtils.getKjsBlock("melodium_casing")))
                    .where("H", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("birmabright_frame")))
                    .where("I", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("advanced_computer_casing")))
                    .where("J", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("computer_heat_vent")))
                    .where("K", Predicates.blocks(StarTMachineUtils.getKjsBlock("zalloy_coil_block")))
                    .where("L", Predicates.abilities(StarTPartAbility.MODULAR_NODE_INTERFACE))
                    .where("M", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("uhv_ultimate_battery")))
                    .where("N", Predicates.abilities(PartAbility.OUTPUT_LASER)) //"gtceu:uhv_4096a_laser_source_hatch"
                    .where("O", Predicates.blocks(StarTMachineUtils.getKjsBlock("reinforced_fusion_glass")))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            ).workableCasingModel(
                    KubeJS.id("block/casings/abydos_multis/superdense_machine_casing"),
                    StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
            ).register();

    public static final MultiblockMachineDefinition T2_PHOTONIC_ACCUMULATOR = START_REGISTRATE
            .multiblock("uev_dyson_collector_module", (holder) -> new StarTDysonSwarmCollectorModule(holder, UEV, StarTCore.resourceLocation("dyson_swarm_monitor")))
            .langValue("Advanced Photonic Accumulator Module [APAM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .generator(true)
            .appearanceBlock(() -> StarTMachineUtils.getKjsBlock("advanced_assembly_casing"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(" A   A ", " ABCBA ", "  CCC  ", " ABCBA ", " A   A ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("AA   AA", "ADAAADA", " E   E ", "ADAAADA", "AAFFFAA", " G   G ", " G   G ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("AA   AA", "BDDDDDB", "B HIH B", "BDDDDDB", "AJJJJJA", "GJBBBJG", "GJBKBJG", " AEEEA ", " JBKBJ ", " JBBBJ ", " GJJJG ", "       ", "       ", "  AAA  ", "   G   ", "       ", "       ", "       ")
                    .aisle("   L   ", "BADDDAB", "K IHI K", "BADDDAB", "GCM MCG", " B E B ", " EM ME ", " J E J ", " EM ME ", " B E B ", " JM MJ ", "  ANA  ", "  AAA  ", " A   A ", "       ", "   G   ", "       ", "       ")
                    .aisle("   A   ", "BADDDAB", "K HIH K", "BADDDAB", " C E C ", " BM MB ", " E E E ", " JM MJ ", " E E E ", " BM MB ", " J E J ", "  NJN  ", "  AAA  ", " A A A ", " G A G ", "  GAG  ", "   A   ", "   E   ")
                    .aisle("   A   ", "BADDDAB", "K IHI K", "BADDDAB", "GCM MCG", " B E B ", " EM ME ", " J E J ", " EM ME ", " B E B ", " JM MJ ", "  ANA  ", "  AAA  ", " A   A ", "       ", "   G   ", "       ", "       ")
                    .aisle("AA   AA", "BDDDDDB", "BDHIH B", "BDDDDDB", "AJJJJJA", "GJBBBJG", "GJBKBJG", " AEEEA ", " JBKBJ ", " JBBBJ ", " GJJJG ", "       ", "       ", "  AAA  ", "   G   ", "       ", "       ", "       ")
                    .aisle("AA   AA", "ADAAADA", " E   E ", "ADAAADA", "AACCCAA", " G   G ", " G   G ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle(" A   A ", " ABOBA ", "  B@B  ", " ABBBA ", " A   A ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .where(" ", Predicates.any())
                    .where("A", Predicates.blocks(StarTMachineUtils.getKjsBlock("draneko_casing")))
                    .where("B", Predicates.blocks(StarTMachineUtils.getKjsBlock("advanced_assembly_casing")))
                    .where("C", Predicates.blocks(StarTMachineUtils.getKjsBlock("enriched_naquadah_engine_intake_casing")))
                    .where("D", Predicates.blocks(StarTMachineUtils.getKjsBlock("extreme_temperature_smelting_casing")))
                    .where("E", Predicates.blocks(StarTMachineUtils.getKjsBlock("magmada_alloy_coil_block")))
                    .where("F", Predicates.blocks(StarTMachineUtils.getKjsBlock("enriched_naquadah_firebox_casing")))
                    .where("G", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("astrenalloy_nx_frame")))
                    .where("H", Predicates.blocks(StarTMachineUtils.getKjsBlock("reinforced_brimstone_casing")))
                    .where("I", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("uhv_ultimate_battery")))
                    .where("J", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("atomic_casing")))
                    .where("K", Predicates.blocks(StarTMachineUtils.getKjsBlock("draco_resilient_fusion_glass")))
                    .where("L", Predicates.abilities(StarTPartAbility.MODULAR_NODE_INTERFACE))
                    .where("M", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("superconducting_coil")))
                    .where("N", Predicates.blocks(StarTMachineUtils.getKjsBlock("melodium_casing")))
                    .where("O", Predicates.abilities(PartAbility.OUTPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            ).workableCasingModel(
                    KubeJS.id("block/casings/threading/advanced_assembly_casing"),
                    StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
            ).register();

    public static final MultiblockMachineDefinition T3_PHOTONIC_ACCUMULATOR = START_REGISTRATE
            .multiblock("uiv_dyson_collector_module", (holder) -> new StarTDysonSwarmCollectorModule(holder, UIV, StarTCore.resourceLocation("dyson_swarm_monitor")))
            .langValue("Advanced II Photonic Accumulator Module [A2PAM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .generator(true)
            .appearanceBlock(() -> StarTMachineUtils.getKjsBlock("advanced_assembly_casing"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("A", "A", "A")
                    .aisle("B", "@", "C")
                    .aisle("A", "A", "A")
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where("B", Predicates.abilities(StarTPartAbility.MODULAR_NODE_INTERFACE))
                    .where("C", Predicates.abilities(PartAbility.OUTPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            ).workableCasingModel(
                    KubeJS.id("block/casings/end_multis/draneko_casing"),
                    StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
            ).register();

    public static final MultiblockMachineDefinition DYSON_SWARM_MONITOR = START_REGISTRATE
            .multiblock("dyson_swarm_monitor", (holder) -> new StarTDysonSwarmMonitor(holder, List.of(railgunModuleIds), List.of(collectorModuleIds)))
            .langValue("Dyson Swarm Monitor [DSM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(() -> StarTMachineUtils.getGTCEuBlock("atomic_casing"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("            AA AA            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("           BBBBBBB           ", "           ABB BBA           ", "            B   B            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("        ABBBBBCBBBBBA        ", "           DE F ED           ", "              F              ", "            EDFDE            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("       GBBBBBBCBBBBBBG       ", "       BBED E   E DEBB       ", "            D   D            ", "            E D E            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("      GBBBBBBBBBBBBBBBG      ", "      BB E  E F E  E BB      ", "      A  E    F    E  A      ", "            D F D            ", "            EDFDE            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("     GBBBBBBBBCBBBBBBBBG     ", "     BB   E E   E E   BB     ", "     A    D D   D D    A     ", "          E       E          ", "            E   E            ", "              D              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("    GHHHHBBBBBBBBBBBHHHHG    ", "    BBI   E E F E E   JBB    ", "      I       F       J      ", "          E D F D E          ", "              F              ", "            E F E            ", "            EDFDE            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("   ABKKKHHHBBBCBBBHHHLLLBA   ", "    B      EE   EE      B    ", "    I   I  D     D  J   J    ", "     III             JJJ     ", "      I    ED   DE    J      ", "            E   E            ", "                             ", "            E D E            ", "            E D E            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("   KKKCKKKHHBBCBBHHLLLCLLM   ", "  I       IIE F EJJ       J  ", "   I     I    F    J     J   ", "            D F D            ", "              F              ", "           E  F  E           ", "            E F E            ", "            D F D            ", "              F              ", "            E F E            ", "            EDFDE            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle(" IKKKKKKKKKHHHHHHHLLLLLLLLMJ ", " XXXXXXXXX  E   E  XXXXXXXXX ", "            D   D            ", "                             ", "                             ", "                             ", "            E   E            ", "                             ", "            D   D            ", "                             ", "           E     E           ", "              N              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle(" KKKKKCKKKKKHHCHHLLLLLCLLLLM ", " XXXXXXXXX N OOO N XXXXXXXXX ", "           N POP N           ", "            NPOPN            ", "            NPOPN            ", "             OPO             ", "             OPO             ", "             OPO             ", "             P P             ", "            NP PN            ", "           E P P E           ", "            NOOON            ", "              D              ", "             EEE             ", "                             ", "                             ", "                             ", "              Q              ", "             QRQ             ", "              Q              ", "                             ", "                             ")
                    .aisle(" KKKSKCKSKKKHHCHHLLLTLCLTLLM ", " XXXXXXXXX  OUOUO  XXXXXXXXX ", "            P   P            ", "           NP V PN           ", "           NP   PN           ", "            P V P            ", "            P   P            ", "            P V P            ", "            O O O            ", "           NO O ON           ", "          E O O O E          ", "           NOOPOON           ", "             OPO             ", "            EOPOE            ", "                             ", "                             ", "             QQQ             ", "            QQQQQ            ", "            QQRQQ            ", "            QQQQQ            ", "             QQQ             ", "                             ")
                    .aisle("KKKKKKKKKKKKKHCHLLLLLLLLLLLLM", " XXXXXXXXX OUOUOUO XXXXXXXXX ", "           P     P           ", "           P     P           ", "           P     P           ", "           O     O           ", "           O     O           ", "           O     O           ", "           P     P           ", "           P     P           ", "          EP     PE          ", "           OO   OO           ", "            O   O            ", "           EO   OE           ", "                             ", "              Q              ", "            QQQQQ            ", "            QQQQQ            ", "           QQQRQQQ           ", "            QQQQQ            ", "            QQQQQ            ", "              Q              ")
                    .aisle("KCKCCKWKCCKCCCCCCCLCCLYLCCLCM", " XXXXXXXXX OOUCUOO XXXXXXXXX ", "          COV C VOC          ", "           O  C  O           ", "          COV C VOC          ", "           P  C  P           ", "           PV C VP           ", "           P  C  P           ", "            O C O            ", "            O C O            ", "          E O C O E          ", "           OP C PO           ", "           DP C PD           ", "           EP C PE           ", "                             ", "             QRQ             ", "            QQRQQ            ", "           QQQRQQQ           ", "           RRRRRRR           ", "           QQQRQQQ           ", "            QQRQQ            ", "             QRQ             ")
                    .aisle("KKKKKKKKKKKKKHCHLLLLLLLLLLLLM", " XXXXXXXXX OUOUOUO XXXXXXXXX ", "           P     P           ", "           P     P           ", "           P     P           ", "           O     O           ", "           O     O           ", "           O     O           ", "           P     P           ", "           P     P           ", "          EP     PE          ", "           OO   OO           ", "            O   O            ", "           EO   OE           ", "                             ", "              Q              ", "            QQQQQ            ", "            QQQQQ            ", "           QQQRQQQ           ", "            QQQQQ            ", "            QQQQQ            ", "              Q              ")
                    .aisle(" KKKSKCKSKKKHHCHHLLLTLCLTLLM ", " XXXXXXXXX  OUOUO  XXXXXXXXX ", "            P   P            ", "           NP V PN           ", "           NP   PN           ", "            P V P            ", "            P   P            ", "            P V P            ", "            O O O            ", "           NO O ON           ", "          E O O O E          ", "           NOOPOON           ", "             OPO             ", "            EOPOE            ", "                             ", "                             ", "             QQQ             ", "            QQQQQ            ", "            QQRQQ            ", "            QQQQQ            ", "             QQQ             ", "                             ")
                    .aisle(" KKKKKCKKKKKHHCHHLLLLLCLLLLM ", " XXXXXXXXX N OOO N XXXXXXXXX ", "           N POP N           ", "            NPOPN            ", "            NPOPN            ", "             OPO             ", "             OPO             ", "             OPO             ", "             P P             ", "            NP PN            ", "           E P P E           ", "            NOOON            ", "              D              ", "             EEE             ", "                             ", "                             ", "                             ", "              Q              ", "             QRQ             ", "              Q              ", "                             ", "                             ")
                    .aisle(" IKKKKKKKKKHHHHHHHLLLLLLLLMJ ", " XXXXXXXXX  E   E  XXXXXXXXX ", "            D   D            ", "                             ", "                             ", "                             ", "            E   E            ", "                             ", "            D   D            ", "                             ", "           E     E           ", "              N              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("   KKKCKKKHHBBCBBHHLLLCLLM   ", "  I       IIE F EJJ       J  ", "   I     I    F    J     J   ", "            D F D            ", "              F              ", "              F  E           ", "            E F E            ", "            D F D            ", "              F              ", "            E F E            ", "            EDFDE            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("   ABKKKHHHBBBCBBBHHHLLLBA   ", "    B      EE   EE      B    ", "    I   I  D     D  J   J    ", "     III             JJJ     ", "      I    ED   DE    J      ", "            E   E            ", "                             ", "            E D E            ", "            E D E            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("    GHHHHBBBBBBBBBBBHHHHG    ", "    BBI   E E F E E   JBB    ", "      I       F       J      ", "          E D F D E          ", "              F              ", "            E F E            ", "            EDFDE            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("     GBBBBBBBBCBBBBBBBBG     ", "     BB   E E   E E   BB     ", "     A    D D   D D    A     ", "          E       E          ", "            E   E            ", "              D              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("      GBBBBBBBBBBBBBBBG      ", "      BB E  E F E  E BB      ", "      A  E    F    E  A      ", "           DD F DD           ", "            EDFDE            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("       GBBBBBBCBBBBBBG       ", "       BBED E   E DEBB       ", "            D   D            ", "            E D E            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("        ABBBBBCBBBBBA        ", "           DE F ED           ", "              @              ", "            EDFDE            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("           BBBBBBB           ", "           ABB BBA           ", "            B   B            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("            AA AA            ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .where(" ", Predicates.any())
                    .where("A", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("trinaquadalloy_frame")))
                    .where("B", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("atomic_casing"))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2)))
                    .where("C", Predicates.blocks(StarTMachineUtils.getKjsBlock("enriched_naquadah_pipe_casing")))
                    .where("D", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("tungsten_carbide_frame")))
                    .where("E", Predicates.blocks(StarTMachineUtils.getKjsBlock("melodium_casing")))
                    .where("F", Predicates.blocks(StarTMachineUtils.getKjsBlock("noble_mixing_casing")))
                    .where("G", Predicates.blocks(StarTMachineUtils.getKjsBlock("enriched_naquadah_firebox_casing")))
                    .where("H", Predicates.blocks(StarTMachineUtils.getKjsBlock("fluix_steel_casing")))
                    .where("I", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("osthendah_frame")))
                    .where("J", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("sterling_silver_frame")))
                    .where("K", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("sturdy_machine_casing")))
                    .where("L", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("computer_casing")))
                    .where("M", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("computer_heat_vent")))
                    .where("N", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("melodium_frame")))
                    .where("O", Predicates.blocks(StarTMachineUtils.getKjsBlock("dragonsteel_casing")))
                    .where("P", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("fusion_glass")))
                    .where("Q", Predicates.blocks(StarTMachineUtils.getKjsBlock("signalum_casing")))
                    .where("R", Predicates.blocks(StarTMachineUtils.getKjsBlock("lumium_casing")))
                    .where("S", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("titanium_pipe_casing")))
                    .where("T", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("advanced_computer_casing")))
                    .where("U", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("high_temperature_smelting_casing")))
                    .where("V", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("superconducting_coil")))
                    .where("W", StarTDysonSwarmPredicates.railgunModulesPredicate)
                    .where("X", Predicates.any()) // bottom of bounding box for modules, extends upwards to top.
                    .where("Y", StarTDysonSwarmPredicates.collectorModulesPredicate)
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            ).workableCasingModel(
                    KubeJS.id("block/casings/abydos_multis/noble_mixing_casing"),
                    StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
            ).register();

    public static void init() {}
}
