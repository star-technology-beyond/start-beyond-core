package com.startechnology.start_core.mixin;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.startechnology.start_core.machine.parallel.IStarTMinimumParallelBlockCache;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(value = RecipeLogic.class, remap = false)
public class RecipeLogicMinimumParallelMixin {
    @Shadow
    public IRecipeLogicMachine machine;

    @Shadow
    public List<GTRecipe> lastFailedMatches;

    @Inject(method = "handleSearchingRecipes", at = @At("HEAD"))
    private void start_core$clearMinimumParallelBlocks(@NotNull Iterator<GTRecipe> matches, CallbackInfo ci) {
        if (machine instanceof IStarTMinimumParallelBlockCache cache) {
            cache.start_core$clearMinimumParallelBlocks();
        }
    }

    @Inject(method = "handleSearchingRecipes", at = @At("RETURN"))
    private void start_core$forgetMinimumParallelOnlyFailures(@NotNull Iterator<GTRecipe> matches, CallbackInfo ci) {
        if (lastFailedMatches == null || lastFailedMatches.isEmpty()) {
            return;
        }

        if (machine instanceof IStarTMinimumParallelBlockCache cache &&
                lastFailedMatches.stream().allMatch(cache::start_core$wasMinimumParallelBlocked)) {
            lastFailedMatches = null;
        }
    }
}
