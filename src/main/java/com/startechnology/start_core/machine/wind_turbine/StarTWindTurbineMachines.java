package com.startechnology.start_core.machine.wind_turbine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.machine.StarTMachineUtils;
import dev.latvian.mods.kubejs.KubeJS;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.GTValues.HV;

public class StarTWindTurbineMachines {
    public static final MultiblockMachineDefinition[] WIND_TURBINES = StarTMachineUtils.registerTieredMultis(
        "wind_turbine",
        StarTWindTurbineMachine::new,
        StarTWindTurbineMachines::buildWindTurbine,
        LV, MV, HV
    );

    private static MultiblockMachineDefinition buildWindTurbine(
        int tier,
        MultiblockMachineBuilder builder
    ) {
        return builder
            .langValue("%s Wind Turbine".formatted(GTValues.VNF[tier] + "§r"))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(() -> getCasingBlock(tier))
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
            .pattern(definition -> switch (tier) {
                case MV -> FactoryBlockPattern.start()
                    .aisle("#######", "#######", "#######", "#######", "#######", "###M###", "###M###", "###M###", "###N###", "###M###", "###M###", "###M###")
                    .aisle("##C#C##", "#######", "#######", "#######", "#######", "#######", "###O###", "#######", "#######", "#######", "###O###", "#######")
                    .aisle("#CDEDC#", "##AFA##", "###B###", "#######", "#######", "#######", "###O###", "#######", "#######", "#######", "###O###", "#######")
                    .aisle("#CDDDC#", "##AAA##", "##BGB##", "###J###", "###K###", "###J###", "###J###", "###J###", "###L###", "###J###", "###J###", "###J###")
                    .aisle("#CDDDC#", "##A@A##", "###B###", "#######", "#######", "#######", "#OO#OO#", "#######", "#######", "#######", "#OO#OO#", "#######")
                    .aisle("##CCC##", "#######", "#######", "#######", "#######", "M#####M", "M#####M", "M#####M", "N#####N", "M#####M", "M#####M", "M#####M")
                    .where("#", Predicates.any())
                    .where("A", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where("B", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("aluminium_frame")))
                    .where("C", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("steel_frame")))
                    .where("D", Predicates.blocks(GTBlocks.STEEL_BRICKS_HULL.get()))
                    .where("E", Predicates.blocks(GTMachines.FLUID_IMPORT_HATCH[LV].getBlock()))
                    .where("F", Predicates.blocks(GTMachines.ENERGY_OUTPUT_HATCH[MV].getBlock()))
                    .where("G", StarTWindTurbinePredicates.windTurbineBearing())
                    .where("H", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("steel_gearbox")))
                    .where("I", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("cupronickel_coil_block")))
                    .where("J", StarTWindTurbinePredicates.windTurbineBlade(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where("K", StarTWindTurbinePredicates.windTurbineBlade(
                        StarTMachineUtils.getGTCEuBlock("steel_gearbox")))
                    .where("L", StarTWindTurbinePredicates.windTurbineBlade(
                        StarTMachineUtils.getGTCEuBlock("cupronickel_coil_block")))
                    .where("M", StarTWindTurbinePredicates.windTurbineBlade(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where("N", StarTWindTurbinePredicates.windTurbineBlade(
                        StarTMachineUtils.getGTCEuBlock("aluminium_frame")))
                    .where("O", StarTWindTurbinePredicates.windTurbineBlade(
                        StarTMachineUtils.getGTCEuBlock("steel_frame")))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build();

                case HV -> FactoryBlockPattern.start()
                    .aisle("         ", "         ", "         ", "         ", "         ", "         ", "         ", "    AA   ", "   A     ", "         ", "         ", "         ", "         ", "    AA   ", "   A     ", "         ", "         ", "         ") 
                    .aisle("         ", "         ", "         ", "         ", "         ", "         ", "      A  ", "    B    ", "  A      ", "         ", "         ", "         ", "      A  ", "    B    ", "  A      ", "         ", "         ", "         ") 
                    .aisle("   B B   ", "         ", "         ", "         ", "         ", "       A ", "         ", "    B    ", "         ", " A       ", "         ", "       A ", "         ", "    B    ", "         ", " A       ", "         ", "         ") 
                    .aisle("  BCDCB  ", "   EFE   ", "   GEG   ", "    B    ", "A   E    ", "        A", "         ", "    B    ", "         ", "         ", "A        ", "        A", "         ", "    B    ", "         ", "         ", "A        ", "         ") 
                    .aisle("  HCCCH  ", "  BEEEB  ", "   EEE   ", "   BIB   ", "A  EJE  A", "    I    ", "    K    ", "    K    ", "    K    ", "    K    ", "ABBBKBBBA", "    K    ", "    K    ", "    K    ", "    K    ", "    K    ", "A   K   A", "    L    ") 
                    .aisle("  BCCCB  ", "   EEE   ", "   G@G   ", "    B    ", "    E   A", "A        ", "         ", "    B    ", "         ", "         ", "        A", "A        ", "         ", "    B    ", "         ", "         ", "        A", "         ") 
                    .aisle("   BHB   ", "    B    ", "         ", "         ", "         ", " A       ", "         ", "    B    ", "         ", "       A ", "         ", " A       ", "         ", "    B    ", "         ", "       A ", "         ", "         ") 
                    .aisle("         ", "         ", "         ", "         ", "         ", "         ", "  A      ", "    B    ", "      A  ", "         ", "         ", "         ", "  A      ", "    B    ", "      A  ", "         ", "         ", "         ") 
                    .aisle("         ", "         ", "         ", "         ", "         ", "         ", "         ", "   AA    ", "     A   ", "         ", "         ", "         ", "         ", "   AA    ", "     A   ", "         ", "         ", "         ") 
                    .where(" ", Predicates.any())
                    .where("A", StarTWindTurbinePredicates.windTurbineBlade(
                        StarTMachineUtils.getKjsBlock("polyethylene_wind_turbine")))
                    .where("B", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("stainless_steel_frame")))
                    .where("C", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("steel_turbine_casing")))
                    .where("D", Predicates.blocks(GTMachines.FLUID_IMPORT_HATCH[LV].getBlock()))
                    .where("E", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("clean_machine_casing")))
                    .where("F", Predicates.blocks(GTMachines.ENERGY_OUTPUT_HATCH[HV].getBlock()))
                    .where("G", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("aluminium_frame")))
                    .where("H", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("black_steel_frame")))
                    .where("I", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("stainless_steel_gearbox")))
                    .where("J", StarTWindTurbinePredicates.windTurbineBearing())
                    .where("K", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("stainless_steel_turbine_casing")))
                    .where("L", Predicates.blocks(StarTMachineUtils.getGTCEuBlock("kanthal_coil_block")))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .build();

                default -> FactoryBlockPattern.start()
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
                        StarTMachineUtils.getKjsBlock("high_steam_machine_casing")))
                    .build();
            })
            .workableCasingModel(
                getCasingTexture(tier),
                StarTCore.resourceLocation("block/overlay/wind_turbine")
            )
            .register();
    }

    private static Block getCasingBlock(int tier) {
        return switch (tier) {
            case MV -> GTBlocks.CASING_STEEL_SOLID.get();
            case HV -> StarTMachineUtils.getGTCEuBlock("stainless_steel_turbine_casing");
            default -> StarTMachineUtils.getKjsBlock("high_steam_machine_casing");
        };
    }

    private static ResourceLocation getCasingTexture(int tier) {
        return switch (tier) {
            case MV -> GTCEu.id("block/casings/solid/machine_casing_solid_steel");
            case HV -> GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel");
            default -> KubeJS.id("block/casings/basic/high_steam_machine_casing");
        };
    }

    public static void init() {
    }
}
