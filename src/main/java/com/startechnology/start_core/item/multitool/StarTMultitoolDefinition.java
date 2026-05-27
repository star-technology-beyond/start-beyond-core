package com.startechnology.start_core.item.multitool;

import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

public class StarTMultitoolDefinition implements IGTToolDefinition {
    public static final StarTMultitoolDefinition INSTANCE = new StarTMultitoolDefinition();

    private final List<IToolBehavior> allBehaviors = Arrays.stream(StarTMultitoolMode.VALUES)
            .flatMap(mode -> mode.toolType().toolDefinition.getBehaviors().stream())
            .distinct()
            .toList();

    private StarTMultitoolDefinition() {
    }

    private IGTToolDefinition selected(ItemStack stack) {
        return StarTMultitoolMode.get(stack).toolType().toolDefinition;
    }

    @Override
    public List<IToolBehavior> getBehaviors() {
        return allBehaviors;
    }

    @Override
    public boolean isToolEffective(BlockState state) {
        return false;
    }

    @Override
    public int getDamagePerAction(ItemStack stack) {
        return selected(stack).getDamagePerAction(stack);
    }

    @Override
    public int getDamagePerCraftingAction(ItemStack stack) {
        return selected(stack).getDamagePerCraftingAction(stack);
    }

    @Override
    public boolean isSuitableForBlockBreak(ItemStack stack) {
        return selected(stack).isSuitableForBlockBreak(stack);
    }

    @Override
    public boolean isSuitableForAttacking(ItemStack stack) {
        return selected(stack).isSuitableForAttacking(stack);
    }

    @Override
    public boolean isSuitableForCrafting(ItemStack stack) {
        return selected(stack).isSuitableForCrafting(stack);
    }

    @Override
    public int getBaseDurability(ItemStack stack) {
        return 8192;
    }

    @Override
    public float getDurabilityMultiplier(ItemStack stack) {
        return Math.max(1.0F, selected(stack).getDurabilityMultiplier(stack));
    }

    @Override
    public int getBaseQuality(ItemStack stack) {
        return Math.max(2, selected(stack).getBaseQuality(stack));
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return selected(stack).getBaseDamage(stack);
    }

    @Override
    public float getBaseEfficiency(ItemStack stack) {
        return selected(stack).getBaseEfficiency(stack);
    }

    @Override
    public float getEfficiencyMultiplier(ItemStack stack) {
        return selected(stack).getEfficiencyMultiplier(stack);
    }

    @Override
    public float getAttackSpeed(ItemStack stack) {
        return selected(stack).getAttackSpeed(stack);
    }

    @Override
    public AoESymmetrical getAoEDefinition(ItemStack stack) {
        return selected(stack).getAoEDefinition(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return selected(stack).isEnchantable(stack);
    }

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return selected(stack).canApplyEnchantment(stack, enchantment);
    }

    @Override
    public Object2IntMap<Enchantment> getDefaultEnchantments(ItemStack stack) {
        return selected(stack).getDefaultEnchantments(stack);
    }

    @Override
    public boolean doesSneakBypassUse() {
        return true;
    }
}
