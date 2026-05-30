package com.startechnology.start_core.recipe.recipes;

import java.util.function.Consumer;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.startechnology.start_core.item.StarTItems;
import com.startechnology.start_core.item.multitool.StarTMultitoolItems;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;

public class MultitoolRecipe {

    public static final void init(Consumer<FinishedRecipe> provider) {
        multitoolRecipe(provider);
    }

    public static void multitoolRecipe(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedEnergyTransferRecipe(
                provider,
                true,
                false,
                true,
                "multitool_empty",

                Ingredient.of(StarTItems.POWER_UNIT_LuV.asStack()),
                StarTMultitoolItems.MULTITOOL_EMPTY.asStack(),

                "wdx",
                "PUP",
                "NMN",

                'W', GTToolType.WRENCH,
                'S', GTToolType.SCREWDRIVER,
                'C', GTToolType.WIRE_CUTTER,
                'P', GTItems.ELECTRIC_PISTON_LuV.asStack(),
                'U', StarTItems.POWER_UNIT_LuV.asStack(),
                'M', GTItems.ELECTRIC_MOTOR_LuV.asStack(),
                'N', new MaterialEntry(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium));
    }

}
