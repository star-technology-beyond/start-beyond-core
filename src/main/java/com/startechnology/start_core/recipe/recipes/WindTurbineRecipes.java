package com.startechnology.start_core.recipe.recipes;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.simibubi.create.AllBlocks;
import com.startechnology.start_core.machine.StarTMachineUtils;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.cableGtSingle;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.frameGt;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.gear;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plate;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rod;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rotor;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireFine;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Aluminium;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Bronze;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Copper;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Gold;
import static com.gregtechceu.gtceu.common.data.GTMaterials.IronMagnetic;
import static com.gregtechceu.gtceu.common.data.GTMaterials.StainlessSteel;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Steel;
import static com.gregtechceu.gtceu.common.data.GTMaterials.SteelMagnetic;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Tin;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Polyethylene;
import static com.gregtechceu.gtceu.common.data.GTMaterials.BorosilicateGlass;
import static com.gregtechceu.gtceu.common.data.GTMaterials.WroughtIron;
import static com.gregtechceu.gtceu.common.data.GTItems.CARBON_FIBERS;
import static com.startechnology.start_core.machine.wind_turbine.StarTWindTurbineBlocks.WIND_TURBINE_BEARING;
import static com.startechnology.start_core.machine.wind_turbine.StarTWindTurbineMachines.WIND_TURBINES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;

public class WindTurbineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, "lv_wind_turbine", WIND_TURBINES[LV].asStack(),
                "MRM",
                "PCP",
                "WQW",
                'P', new MaterialEntry(plate, Steel),
                'R', new MaterialEntry(rotor, Tin),
                'M', new MaterialEntry(rod, IronMagnetic),
                'C', StarTMachineUtils.getKjsBlock("high_steam_machine_casing"),
                'W', new MaterialEntry(cableGtSingle, Tin),
                'Q', CustomTags.LV_CIRCUITS);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "mv_wind_turbine", WIND_TURBINES[MV].asStack(),
                "MRM",
                "PCP",
                "WQW",
                'P', new MaterialEntry(plate, Aluminium),
                'R', new MaterialEntry(rotor, Bronze),
                'M', new MaterialEntry(rod, SteelMagnetic),
                'C', GTBlocks.CASING_STEEL_SOLID.get(),
                'W', new MaterialEntry(cableGtSingle, Copper),
                'Q', CustomTags.MV_CIRCUITS);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "hv_wind_turbine", WIND_TURBINES[HV].asStack(),
                "MRM",
                "PCP",
                "WQW",
                'P', new MaterialEntry(plate, StainlessSteel),
                'R', new MaterialEntry(rotor, Steel),
                'M', new MaterialEntry(rod, SteelMagnetic),
                'C', GTBlocks.CASING_STAINLESS_CLEAN.get(),
                'W', new MaterialEntry(cableGtSingle, Gold),
                'Q', CustomTags.HV_CIRCUITS);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "wind_turbine_bearing", WIND_TURBINE_BEARING.asStack(),
                " S ",
                "PBP",
                "GFG",
                'P', new MaterialEntry(plate, Steel),
                'S', AllBlocks.SHAFT.get(),
                'G', new MaterialEntry(gear, Bronze),
                'B', AllBlocks.WINDMILL_BEARING.get(),
                'F', new MaterialEntry(frameGt, WroughtIron));

        ASSEMBLER_RECIPES.recipeBuilder("polyethylene_wind_turbine")
                .inputItems(plate, Polyethylene, 8)
                .inputItems(wireFine, BorosilicateGlass, 4)
                .inputItems(CARBON_FIBERS, 2)
                .outputItems(new ItemStack(StarTMachineUtils.getKjsBlock("polyethylene_wind_turbine"), 4))
                .duration(100)
                .EUt(64)
                .circuitMeta(6)
                .save(provider);
    }
}
