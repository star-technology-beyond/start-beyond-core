package com.startechnology.start_core.item.multitool;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class StarTMultitoolDefinition implements IGTToolDefinition {
    public static final StarTMultitoolDefinition INSTANCE = new StarTMultitoolDefinition();

    private StarTMultitoolDefinition() {}

    // return the currently active multitool's internal gt definition
    private IGTToolDefinition selected(ItemStack stack) {
        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        return active == null ? null : active.toolType().toolDefinition;
    }

    // returns true if the multitool has no charge remaining
    private boolean isOutOfEnergy(ItemStack stack) {
        IElectricItem cap = GTCapabilityHelper.getElectricItem(stack);
        return cap != null && cap.getCharge() == 0;
    }

    @Override
    public List<IToolBehavior> getBehaviors() {
        return List.of();
    }

    public List<IToolBehavior> getBehaviors(ItemStack stack) {
        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        return active == null ? List.of() : active.toolType().toolDefinition.getBehaviors();
    }

    @Override
    public boolean isToolEffective(BlockState state) {
        return false;
    }

    @Override
    public int getDamagePerAction(ItemStack stack) {
        IGTToolDefinition def = selected(stack);
        return def == null ? 1 : def.getDamagePerAction(stack);
    }

    @Override
    public int getDamagePerCraftingAction(ItemStack stack) {
        IGTToolDefinition def = selected(stack);
        return def == null ? 1 : def.getDamagePerCraftingAction(stack);
    }

    @Override
    public boolean isSuitableForBlockBreak(ItemStack stack) {
        if (isOutOfEnergy(stack)) return false;
        IGTToolDefinition def = selected(stack);
        return def != null && def.isSuitableForBlockBreak(stack);
    }

    @Override
    public boolean isSuitableForAttacking(ItemStack stack) {
        if (isOutOfEnergy(stack)) return false;
        IGTToolDefinition def = selected(stack);
        return def != null && def.isSuitableForAttacking(stack);
    }

    @Override
    public boolean isSuitableForCrafting(ItemStack stack) {
        if (isOutOfEnergy(stack)) return false;
        IGTToolDefinition def = selected(stack);
        return def != null && def.isSuitableForCrafting(stack);
    }

    @Override
    public int getBaseDurability(ItemStack stack) {
        return 0;
    }

    @Override
    public float getDurabilityMultiplier(ItemStack stack) {
        IGTToolDefinition def = selected(stack);
        return def == null ? 1.0F : Math.max(1.0F, def.getDurabilityMultiplier(stack));
    }

    @Override
    public int getBaseQuality(ItemStack stack) {
        IGTToolDefinition def = selected(stack);
        return def == null ? 0 : def.getBaseQuality(stack);
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        if (isOutOfEnergy(stack)) return 0.0F;
        IGTToolDefinition def = selected(stack);
        return def == null ? 1.0F : def.getBaseDamage(stack);
    }

    @Override
    public float getBaseEfficiency(ItemStack stack) {
        if (isOutOfEnergy(stack)) return 1.0F;
        IGTToolDefinition def = selected(stack);
        return def == null ? 1.0F : def.getBaseEfficiency(stack);
    }

    @Override
    public float getEfficiencyMultiplier(ItemStack stack) {
        if (isOutOfEnergy(stack)) return 1.0F;
        IGTToolDefinition def = selected(stack);
        return def == null ? 1.0F : def.getEfficiencyMultiplier(stack);
    }

    @Override
    public float getAttackSpeed(ItemStack stack) {
        if (isOutOfEnergy(stack)) return 0.0F;
        IGTToolDefinition def = selected(stack);
        return def == null ? 0.0F : def.getAttackSpeed(stack);
    }

    @Override
    public AoESymmetrical getAoEDefinition(ItemStack stack) {
        if (isOutOfEnergy(stack)) return AoESymmetrical.ZERO;
        IGTToolDefinition def = selected(stack);
        return def == null ? AoESymmetrical.ZERO : def.getAoEDefinition(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        IGTToolDefinition def = selected(stack);
        return def != null && def.isEnchantable(stack);
    }

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        IGTToolDefinition def = selected(stack);
        return def != null && def.canApplyEnchantment(stack, enchantment);
    }

    @Override
    public Object2IntMap<Enchantment> getDefaultEnchantments(ItemStack stack) {
        IGTToolDefinition def = selected(stack);
        return def == null ? Object2IntMaps.emptyMap() : def.getDefaultEnchantments(stack);
    }

    @Override
    public boolean doesSneakBypassUse() {
        return true;
    }
}