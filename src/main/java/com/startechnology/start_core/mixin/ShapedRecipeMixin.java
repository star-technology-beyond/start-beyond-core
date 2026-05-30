package com.startechnology.start_core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.startechnology.start_core.item.multitool.StarTMultitoolItem;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void onMatches(CraftingContainer inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            // prevent crafting when multitool is out of energy
            if (stack.getItem() instanceof StarTMultitoolItem tool && tool.isOutOfEnergy(stack)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}