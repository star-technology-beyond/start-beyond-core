package com.startechnology.start_core.recipe.recipes;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.machine.StarTEnergyDynamos;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.api.GTValues.ULV;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plate;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rod;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtQuadruple;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.spring;
import static com.gregtechceu.gtceu.common.data.GTItems.VOLTAGE_COIL_HV;
import static com.gregtechceu.gtceu.common.data.GTItems.VOLTAGE_COIL_LV;
import static com.gregtechceu.gtceu.common.data.GTItems.VOLTAGE_COIL_MV;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Aluminium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Copper;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Gold;
import static com.gregtechceu.gtceu.common.data.GTMaterials.IronMagnetic;
import static com.gregtechceu.gtceu.common.data.GTMaterials.StainlessSteel;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Steel;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Tin;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Lead;
import static com.gregtechceu.gtceu.common.data.GTMaterials.WroughtIron;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;

public class StarTDynamoHatches {

    public static void init(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTCEu.id("dynamo_hatch_ulv"),
                GTMachines.ENERGY_OUTPUT_HATCH[ULV].asStack(),
                "PRP",
                "WHW",
                "SRS",
                'W', new MaterialEntry(wireGtQuadruple, Lead),
                'H', GTMachines.HULL[ULV].asStack(),
                'P', new MaterialEntry(plate, WroughtIron),
                'S', new MaterialEntry(spring, Lead),
                'R', new MaterialEntry(rod, IronMagnetic));

        VanillaRecipeHelper.addShapedRecipe(provider, true, GTCEu.id("dynamo_hatch_lv"),
                GTMachines.ENERGY_OUTPUT_HATCH[LV].asStack(),
                "PRP",
                "WHW",
                "SRS",
                'W', new MaterialEntry(wireGtQuadruple, Steel),
                'H', GTMachines.HULL[LV].asStack(),
                'P', new MaterialEntry(plate, Steel),
                'S', new MaterialEntry(spring, Tin),
                'R', new MaterialEntry(rod, IronMagnetic));

        ASSEMBLER_RECIPES.recipeBuilder(StarTCore.resourceLocation("lv_energy_output_hatch_4a"))
                .inputItems(GTMachines.TRANSFORMER[LV])
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[LV])
                .inputItems(wireGtQuadruple, Tin, 4)
                .inputItems(VOLTAGE_COIL_LV)
                .inputItems(plate, Steel, 4)
                .outputItems(StarTEnergyDynamos.ENERGY_OUTPUT_HATCH_4A[LV])
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(StarTCore.resourceLocation("mv_energy_output_hatch_4a"))
                .inputItems(GTMachines.TRANSFORMER[MV])
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[MV])
                .inputItems(wireGtQuadruple, Copper, 4)
                .inputItems(VOLTAGE_COIL_MV)
                .inputItems(plate, Aluminium, 4)
                .outputItems(StarTEnergyDynamos.ENERGY_OUTPUT_HATCH_4A[MV])
                .duration(200)
                .EUt(VA[MV])
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(StarTCore.resourceLocation("hv_energy_output_hatch_4a"))
                .inputItems(GTMachines.TRANSFORMER[HV])
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[HV])
                .inputItems(wireGtQuadruple, Gold, 4)
                .inputItems(VOLTAGE_COIL_HV)
                .inputItems(plate, StainlessSteel, 4)
                .outputItems(StarTEnergyDynamos.ENERGY_OUTPUT_HATCH_4A[HV])
                .duration(200)
                .EUt(VA[HV])
                .save(provider);
    }
}
