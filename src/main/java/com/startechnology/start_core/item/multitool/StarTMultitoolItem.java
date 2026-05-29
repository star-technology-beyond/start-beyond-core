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
        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        Component base = getDescription().copy();
        if (active == null) {
            return base.copy()
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("item.start_core.gregtech_multitool.empty")
                            .withStyle(ChatFormatting.RED));
        }
        return base.copy()
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(active.displayName().copy().withStyle(ChatFormatting.AQUA));
    }

    @Override
    public ItemStack getDefaultInstance() {
        // nothing installed for a default multitool
        // item/empty item
        return get();
    }

    @Override
    public Set<GTToolType> getToolClasses(ItemStack stack) {
        Set<GTToolType> classes = new HashSet<>();
        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        if (active != null) {
            classes.addAll(active.toolType().toolClasses);
            classes.add(active.toolType());
        }
        return classes;
    }

    @Override
    public GTToolType getToolType() {
        return GTToolType.WRENCH;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        for (IToolBehavior behavior : StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)) {
            if (behavior.onItemUseFirst(stack, context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        for (IToolBehavior behavior : StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)) {
            if (behavior.onItemUse(context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        for (IToolBehavior behavior : StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)) {
            if (behavior.onItemRightClick(level, player, usedHand).getResult() == InteractionResult.SUCCESS) {
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)
                .forEach(behavior -> behavior.hitEntity(stack, target, attacker));
        ToolHelper.damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide) {
            StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)
                    .forEach(behavior -> behavior.onBlockDestroyed(stack, level, state, pos, miningEntity));
            if (state.getDestroySpeed(level, pos) != 0.0D) {
                ToolHelper.damageItem(stack, miningEntity, getToolStats().getToolDamagePerBlockBreak(stack));
            }
        }
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return StarTMultitoolDefinition.INSTANCE.canApplyEnchantment(stack, enchantment);
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
        // WORK IN PROGRESS
        // TODO: FIX EVERYTHING GRRR IHATE LANG
        
        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        if (active != null) {
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.mode",
                    active.displayName()).withStyle(ChatFormatting.AQUA));
        } else {
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.empty")
                    .withStyle(ChatFormatting.RED));
        }

        List<StarTMultitoolMode> installed = StarTMultitoolMode.getInstalled(stack);
        if (!installed.isEmpty()) {
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.installed")
                    .withStyle(ChatFormatting.GRAY));
            for (StarTMultitoolMode mode : installed) {
                tooltipComponents.add(Component.literal("  ")
                        .append(mode.displayName().copy().withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" (" + mode.material().getLocalizedName() + ")")
                                .withStyle(ChatFormatting.DARK_GRAY)));
            }
        }

        tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.hint")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}