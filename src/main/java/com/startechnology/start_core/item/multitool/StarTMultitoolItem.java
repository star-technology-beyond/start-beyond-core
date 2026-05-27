package com.startechnology.start_core.item.multitool;

import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StarTMultitoolItem extends GTToolItem {

    public StarTMultitoolItem(MaterialToolTier tier, Material material, Properties properties) {
        super(GTToolType.WRENCH, tier, material, StarTMultitoolDefinition.INSTANCE, properties);
    }

    @Override
    public Component getDescription() {
        return Component.translatable(getDescriptionId());
    }

    @Override
    public String getDescriptionId() {
        return "item.start_core.gregtech_multitool";
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription().copy()
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(StarTMultitoolMode.get(stack).displayName().copy().withStyle(ChatFormatting.AQUA));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = get();
        StarTMultitoolMode.set(stack, StarTMultitoolMode.WRENCH);
        return stack;
    }

    @Override
    public Set<GTToolType> getToolClasses(ItemStack stack) {
        Set<GTToolType> classes = new HashSet<>(StarTMultitoolMode.get(stack).toolType().toolClasses);
        classes.add(StarTMultitoolMode.get(stack).toolType());
        return classes;
    }

    @Override
    public GTToolType getToolType() {
        return GTToolType.WRENCH;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        for (IToolBehavior behavior : StarTMultitoolMode.get(stack).toolType().toolDefinition.getBehaviors()) {
            if (behavior.onItemUseFirst(stack, context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        for (IToolBehavior behavior : StarTMultitoolMode.get(stack).toolType().toolDefinition.getBehaviors()) {
            if (behavior.onItemUse(context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        for (IToolBehavior behavior : StarTMultitoolMode.get(stack).toolType().toolDefinition.getBehaviors()) {
            if (behavior.onItemRightClick(level, player, usedHand).getResult() == InteractionResult.SUCCESS) {
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        StarTMultitoolMode.get(stack).toolType().toolDefinition.getBehaviors()
                .forEach(behavior -> behavior.hitEntity(stack, target, attacker));
        ToolHelper.damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide) {
            StarTMultitoolMode.get(stack).toolType().toolDefinition.getBehaviors()
                    .forEach(behavior -> behavior.onBlockDestroyed(stack, level, state, pos, miningEntity));
            if (state.getDestroySpeed(level, pos) != 0.0D) {
                ToolHelper.damageItem(stack, miningEntity, getToolStats().getToolDamagePerBlockBreak(stack));
            }
        }
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return StarTMultitoolMode.get(stack).toolType().toolDefinition.canApplyEnchantment(stack, enchantment);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return definition$getDefaultAttributeModifiers(slot, stack);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.mode",
                StarTMultitoolMode.get(stack).displayName()).withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.hint")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    public static void setMode(ItemStack stack, StarTMultitoolMode mode) {
        StarTMultitoolMode.set(stack, mode);
        CompoundTag toolTag = ToolHelper.getToolTag(stack);
        toolTag.remove(ToolHelper.TOOL_SPEED_KEY);
        toolTag.remove(ToolHelper.ATTACK_DAMAGE_KEY);
        toolTag.remove(ToolHelper.ATTACK_SPEED_KEY);
        toolTag.remove(ToolHelper.HARVEST_LEVEL_KEY);
    }
}
