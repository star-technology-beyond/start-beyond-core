package com.startechnology.start_core.item.multitool;

import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.TreeFellingHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolUIBehavior;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
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
import net.minecraftforge.common.ToolAction;
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
            InteractionResult result = behavior.onItemUseFirst(stack, context);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        for (IToolBehavior behavior : StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)) {
            InteractionResult result = behavior.onItemUse(context);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        for (IToolBehavior behavior : StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)) {
            if (behavior instanceof IToolUIBehavior uiBehavior && uiBehavior.openUI(player, usedHand)) {
                if (!level.isClientSide) {
                    HeldItemUIFactory.INSTANCE.openUI((ServerPlayer) player, usedHand);
                }
                return InteractionResultHolder.success(stack);
            }
        }

        for (IToolBehavior behavior : StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)) {
            InteractionResultHolder<ItemStack> result = behavior.onItemRightClick(level, player, usedHand);
            if (result.getResult() != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ModularUI createUI(Player player, HeldItemUIFactory.HeldItemHolder holder) {
        ItemStack stack = holder.getHeld();
        for (IToolBehavior behavior : StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)) {
            if (behavior instanceof IToolUIBehavior uiBehavior && uiBehavior.openUI(player, holder.getHand())) {
                return uiBehavior.createUI(player, holder);
            }
        }
        return new ModularUI(holder, player);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level().isClientSide) return false;

        StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)
                .forEach(behavior -> behavior.onBlockStartBreak(stack, pos, player));

        if (!player.isShiftKeyDown()) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            int result = -1;

            if (ToolHelper.isTool(stack, GTToolType.SHEARS)) {
                result = ToolHelper.shearBlockRoutine(serverPlayer, stack, pos);
            }

            if (result != 0) {
                BlockState state = player.level().getBlockState(pos);

                boolean effective = getToolClasses(stack).stream()
                        .anyMatch(type -> type.harvestTags.stream().anyMatch(state::is));
                effective |= ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));

                if (effective) {
                    if (ToolHelper.areaOfEffectBlockBreakRoutine(stack, serverPlayer, pos)) {
                        if (playSoundOnBlockDestroy()) playSound(player);
                    } else {
                        if (result == -1) {
                            var tag = ToolHelper.getBehaviorsTag(stack);
                            if (tag.getBoolean(ToolHelper.TREE_FELLING_KEY) &&
                                    !tag.getBoolean(ToolHelper.DISABLE_TREE_FELLING_KEY) &&
                                    state.is(BlockTags.LOGS)) {
                                TreeFellingHelper.fellTree(stack, player.level(), state, pos, player);
                            }
                            if (playSoundOnBlockDestroy()) playSound(player);
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide) {
            StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)
                    .forEach(behavior -> behavior.onBlockDestroyed(stack, level, state, pos, miningEntity));
            if (state.getDestroySpeed(level, pos) != 0.0D) {
                ToolHelper.damageItem(stack, miningEntity, getToolStats().getToolDamagePerBlockBreak(stack));
            }
            if (miningEntity instanceof Player player && playSoundOnBlockDestroy()) {
                if (player.isShiftKeyDown()) {
                    playSound(player);
                }
            }
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)
                .forEach(behavior -> behavior.hitEntity(stack, target, attacker));
        ToolHelper.damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return StarTMultitoolDefinition.INSTANCE.getBehaviors(stack).stream()
                .anyMatch(behavior -> behavior.canDisableShield(stack, shield, entity, attacker));
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        StarTMultitoolDefinition.INSTANCE.getBehaviors(stack)
                .forEach(behavior -> behavior.onEntitySwing(entity, stack));
        return false;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        if (active != null && active.toolType().defaultAbilities.contains(action)) return true;
        return StarTMultitoolDefinition.INSTANCE.getBehaviors(stack).stream()
                .anyMatch(behavior -> behavior.canPerformAction(stack, action));
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
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.hint")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.line"));
            tooltipComponents.add(Component.translatable("block.start_core.breaker_line"));
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l1"));
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l2"));
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l3"));
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l4"));
            tooltipComponents.add(Component.translatable("block.start_core.breaker_line"));
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l5"));
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l6"));
            tooltipComponents.add(Component.translatable("block.start_core.breaker_line"));
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l7"));
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l8"));
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.l9"));
            tooltipComponents.add(Component.translatable("block.start_core.breaker_line"));
        }

        List<StarTMultitoolMode> installed = StarTMultitoolMode.getInstalled(stack);
        if (!installed.isEmpty()) {
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.installed")
                    .withStyle(ChatFormatting.GRAY));
            for (StarTMultitoolMode mode : installed) {
                tooltipComponents.add(Component.literal("  ")
                        .append(mode.displayName()))
            }
        }

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}