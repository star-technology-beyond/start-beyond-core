package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.machine.StarTMachineUtils;
import com.startechnology.start_core.machine.StarTPartAbility;
import dev.latvian.mods.kubejs.KubeJS;


import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.startechnology.start_core.StarTCore.START_REGISTRATE;


public class StarTDysonSwarmMachines {

    public static final MultiblockMachineDefinition T1_STELLAR_RAILGUN = START_REGISTRATE
            .multiblock("uhv_railgun_module", (holder) -> new StarTDysonSwarmMachine(holder, "railgun", UHV))
            .langValue("Basic Stellar Railgun Module [BSRM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
//            .appearanceBlock()
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA")
                    .aisle("C@B")
                    .aisle("AAA")
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                    )
                    .where("B", Predicates.abilities(StarTPartAbility.MODULAR_TERMINAL))
                    .where("C", Predicates.abilities(PartAbility.INPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
//            .workableCasingModel()
            .register();

    public static final MultiblockMachineDefinition T2_STELLAR_RAILGUN = START_REGISTRATE
            .multiblock("uev_railgun_module", (holder) -> new StarTDysonSwarmMachine(holder, "railgun", UEV))
            .langValue("Advanced Stellar Railgun Module [ASRM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
//            .appearanceBlock()
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA")
                    .aisle("C@B")
                    .aisle("AAA")
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                    )
                    .where("B", Predicates.abilities(StarTPartAbility.MODULAR_TERMINAL))
                    .where("C", Predicates.abilities(PartAbility.INPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
//            .workableCasingModel()
            .register();

    public static final MultiblockMachineDefinition T3_STELLAR_RAILGUN = START_REGISTRATE
            .multiblock("uiv_railgun_module", (holder) -> new StarTDysonSwarmMachine(holder, "railgun", UIV))
            .langValue("Advanced II Stellar Railgun Module [A2SRM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
//            .appearanceBlock()
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA")
                    .aisle("C@B")
                    .aisle("AAA")
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                    )
                    .where("B", Predicates.abilities(StarTPartAbility.MODULAR_TERMINAL))
                    .where("C", Predicates.abilities(PartAbility.INPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
//            .workableCasingModel()
            .register();

    public static final MultiblockMachineDefinition T1_PHOTONIC_ACCUMULATOR = START_REGISTRATE
            .multiblock("uhv_photonic_accumulator", (holder) -> new StarTDysonSwarmMachine(holder, "receiver", UHV))
            .langValue("Basic Photonic Accumulator Module [BPAM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .generator(true)
//            .appearanceBlock()
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA")
                    .aisle("B@C")
                    .aisle("AAA")
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where("B", Predicates.abilities(StarTPartAbility.MODULAR_TERMINAL))
                    .where("C", Predicates.abilities(PartAbility.OUTPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
//            .workableCasingModel()
            .register();

    public static final MultiblockMachineDefinition T2_PHOTONIC_ACCUMULATOR = START_REGISTRATE
            .multiblock("uev_photonic_accumulator", (holder) -> new StarTDysonSwarmMachine(holder, "receiver", UEV))
            .langValue("Advanced Photonic Accumulator Module [APAM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .generator(true)
//            .appearanceBlock()
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA")
                    .aisle("B@C")
                    .aisle("AAA")
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where("B", Predicates.abilities(StarTPartAbility.MODULAR_TERMINAL))
                    .where("C", Predicates.abilities(PartAbility.OUTPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
//            .workableCasingModel()
            .register();

    public static final MultiblockMachineDefinition T3_PHOTONIC_ACCUMULATOR = START_REGISTRATE
            .multiblock("uiv_photonic_accumulator", (holder) -> new StarTDysonSwarmMachine(holder, "receiver", UIV))
            .langValue("Advanced II Photonic Accumulator Module [A2PAM]")
//            .tooltips()
//            .paginatedTooltips()
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .generator(true)
//            .appearanceBlock()
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA")
                    .aisle("B@C")
                    .aisle("AAA")
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where("B", Predicates.abilities(StarTPartAbility.MODULAR_TERMINAL))
                    .where("C", Predicates.abilities(PartAbility.OUTPUT_LASER))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
//            .workableCasingModel()
            .register();

    public static final MultiblockMachineDefinition DYSON_SWARM_MONITOR = START_REGISTRATE
            .multiblock("dyson_swarm_monitor", (holder) -> new StarTDysonSwarmMachine(holder, "monitor", UHV))
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
                    .aisle("KCKCCKWKCCKCCCCCCCLCCLWLCCLCM", " XXXXXXXXX OOUCUOO XXXXXXXXX ", "          COV C VOC          ", "           O  C  O           ", "          COV C VOC          ", "           P  C  P           ", "           PV C VP           ", "           P  C  P           ", "            O C O            ", "            O C O            ", "          E O C O E          ", "           OP C PO           ", "           DP C PD           ", "           EP C PE           ", "                             ", "             QRQ             ", "            QQRQQ            ", "           QQQRQQQ           ", "           RRRRRRR           ", "           QQQRQQQ           ", "            QQRQQ            ", "             QRQ             ")
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
                    .where("W", Predicates.abilities(StarTPartAbility.MODULAR_TERMINAL_INTERFACE))
                    .where("X", Predicates.any()) // bottom of bounding box for modules, extends upwards to top.
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            ).workableCasingModel(
                    KubeJS.id("block/casings/abydos_multis/noble_mixing_casing"),
                    StarTCore.resourceLocation("block/overlay/cross_dimensional_laser")
            )
            .register();

    public static void init() {
    }
}
