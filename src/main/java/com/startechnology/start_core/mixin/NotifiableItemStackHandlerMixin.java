package com.startechnology.start_core.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = NotifiableItemStackHandler.class, remap = false)
public class NotifiableItemStackHandlerMixin {

    @Inject(method = "handleRecipe", at = @At("RETURN"))
    private static void start_core$mergeInputStacks(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate,
                                                   IO handlerIO, CustomItemStackHandler storage,
                                                   CallbackInfoReturnable<List<Ingredient>> cir) {
        if (simulate || io != IO.IN || handlerIO != IO.IN) {
            return;
        }

        start_core$mergeStacks(storage);
    }

    @Unique
    private static void start_core$mergeStacks(CustomItemStackHandler storage) {
        if (storage.getSlots() < 2) {
            return;
        }

        for (int slot = 0; slot < storage.getSlots(); slot++) {
            var target = storage.getStackInSlot(slot);
            if (target.isEmpty()) {
                continue;
            }

            var limit = Math.min(target.getMaxStackSize(), storage.getSlotLimit(slot));
            if (target.getCount() >= limit) {
                continue;
            }

            var changed = false;
            for (int otherSlot = slot + 1; otherSlot < storage.getSlots(); otherSlot++) {
                var source = storage.getStackInSlot(otherSlot);
                if (source.isEmpty() || !ItemStack.isSameItemSameTags(target, source)) {
                    continue;
                }

                var moved = Math.min(limit - target.getCount(), source.getCount());
                target.grow(moved);
                source.shrink(moved);
                changed = true;
                storage.setStackInSlot(otherSlot, source.isEmpty() ? ItemStack.EMPTY : source);

                if (target.getCount() >= limit) {
                    break;
                }
            }

            if (changed) {
                storage.setStackInSlot(slot, target);
            }
        }
    }
}
