package com.startechnology.start_core.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.startechnology.start_core.machine.parallel.IStarTMinimumParallelBlockCache;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = WorkableMultiblockMachine.class, remap = false)
public class WorkableMultiblockMachineMinimumParallelMixin implements IStarTMinimumParallelBlockCache {
    @Unique
    private final Set<ResourceLocation> start_core$minimumParallelBlockedRecipes = new HashSet<>();

    @Override
    public void start_core$markMinimumParallelBlocked(GTRecipe recipe) {
        ResourceLocation recipeId = start_core$getRecipeId(recipe);
        if (recipeId != null) {
            start_core$minimumParallelBlockedRecipes.add(recipeId);
        }
    }

    @Override
    public boolean start_core$wasMinimumParallelBlocked(GTRecipe recipe) {
        ResourceLocation recipeId = start_core$getRecipeId(recipe);
        return recipeId != null && start_core$minimumParallelBlockedRecipes.contains(recipeId);
    }

    @Override
    public void start_core$clearMinimumParallelBlocks() {
        start_core$minimumParallelBlockedRecipes.clear();
    }

    @Unique
    private static @Nullable ResourceLocation start_core$getRecipeId(GTRecipe recipe) {
        return recipe == null ? null : recipe.getId();
    }
}
