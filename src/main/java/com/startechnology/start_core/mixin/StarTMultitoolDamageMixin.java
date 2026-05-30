package com.startechnology.start_core.mixin;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ToolHelper.class, remap = false)
public class StarTMultitoolDamageMixin {

    /**
     * intercept the damaging gt tools for
     * multitool damage which should only
     * deal damage by reducing energy.
     */
    @Inject(
        method = "damageItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void startcore$interceptMultitoolDamage(
            ItemStack stack,
            @Nullable LivingEntity user,
            int damage,
            CallbackInfo ci) {

        // must be a multitoolo
        if (!(stack.getItem() instanceof StarTMultitoolItem multitool)) return;
        ci.cancel();

        // dont consume durability in creative
        if (user instanceof Player player && player.isCreative()) {
            return;
        }

        // check if current active is unbreakable
        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        if (active != null) {
            ToolProperty toolProperty = active.material().getProperty(PropertyKey.TOOL);
            if (toolProperty != null && toolProperty.isUnbreakable()) return;
        }

        // get the electric item from the multitool and deal damage to that.
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return;

        int euCost = damage * ConfigHolder.INSTANCE.machines.energyUsageMultiplier;
        electricItem.discharge(euCost, multitool.getElectricTier(), true, false, false);
    }
}