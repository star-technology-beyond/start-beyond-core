package com.startechnology.start_core.item.multitool;

import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import net.minecraft.locale.Language;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.TreeFellingHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolUIBehavior;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.startechnology.start_core.StarTCore;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
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
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StarTMultitoolItem extends GTToolItem {
    
    // The electric tier of the multitool is its capacity like IV or not
    private final int electricTier;

    public StarTMultitoolItem(MaterialToolTier tier, Material material, Properties properties, int electricTier) {
        super(GTToolType.WRENCH, tier, material, StarTMultitoolDefinition.INSTANCE, properties);
        this.electricTier = electricTier;
    }

    @Override
    public boolean isElectric() {
        return true;
    }

    @Override
    public int getElectricTier() {
        return electricTier;
    }

    private long getMaxEnergy() {
        return GTValues.V[electricTier] * 3200L;
    }

    @Override
    public ItemStack get() {
        ItemStack stack = super.get();

        ElectricItem electricItem = (ElectricItem) GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            long max = getMaxEnergy();
            electricItem.setMaxChargeOverride(max);
            electricItem.setCharge(max);
        }

        // the tool should be unbreakable since all durability
        // damage goes to the eelectricity part
        CompoundTag toolTag = ToolHelper.getToolTag(stack);
        toolTag.putInt(ToolHelper.MAX_DURABILITY_KEY, 1);
        clearStatCache(toolTag);

        return stack;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return get();
    }

    // remove cached values written by gt shennanigans
    // so we can force our tool one
    private static void clearStatCache(CompoundTag toolTag) {
        toolTag.remove(ToolHelper.TOOL_SPEED_KEY);
        toolTag.remove(ToolHelper.ATTACK_DAMAGE_KEY);
        toolTag.remove(ToolHelper.ATTACK_SPEED_KEY);
        toolTag.remove(ToolHelper.HARVEST_LEVEL_KEY);
    }

    
    // returns whether or not the tool is out of energy
    private boolean isOutOfEnergy(ItemStack stack) {
        var cap = GTCapabilityHelper.getElectricItem(stack);
        return cap != null && cap.getCharge() == 0;
    }

    // no durability for our tool, everything is part of
    // elektrizity
    @Override
    public int getTotalMaxDurability(ItemStack stack) {
        CompoundTag toolTag = ToolHelper.getToolTag(stack);
        toolTag.putInt(ToolHelper.MAX_DURABILITY_KEY, 1);
        return 1;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (!hasCraftingRemainingItem(stack)) return ItemStack.EMPTY;
        ToolHelper.damageItem(stack, null, getToolStats().getToolDamagePerCraft(stack));
        playCraftingSound(null, stack);
        return stack.copy();
    }

    private Material getActiveMaterial(ItemStack stack) {
        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        return active != null ? active.material() : null;
    }

    private ToolProperty getActiveToolProperty(ItemStack stack) {
        Material activeMat = getActiveMaterial(stack);
        return activeMat != null ? activeMat.getProperty(PropertyKey.TOOL) : null;
    }

    @Override
    public int getTotalHarvestLevel(ItemStack stack) {
        if (isOutOfEnergy(stack)) return 0;

        CompoundTag toolTag = ToolHelper.getToolTag(stack);

        // return the instance's cached/modified level if it exists
        if (toolTag.contains(ToolHelper.HARVEST_LEVEL_KEY)) {
            return toolTag.getInt(ToolHelper.HARVEST_LEVEL_KEY);
        }

        ToolProperty prop = getActiveToolProperty(stack);
        if (prop == null) return 0; 

        int level = prop.getHarvestLevel() + getToolStats().getBaseQuality(stack);
        toolTag.putInt(ToolHelper.HARVEST_LEVEL_KEY, level);
        return level;
    }

    @Override
    public float getTotalToolSpeed(ItemStack stack) {
        if (isOutOfEnergy(stack)) return 1.0F;

        CompoundTag toolTag = ToolHelper.getToolTag(stack);
        if (toolTag.contains(ToolHelper.TOOL_SPEED_KEY)) {
            return toolTag.getFloat(ToolHelper.TOOL_SPEED_KEY);
        }

        ToolProperty prop = getActiveToolProperty(stack);
        if (prop == null) return 1.0F;

        float speed = (getToolStats().getEfficiencyMultiplier(stack) * prop.getHarvestSpeed())
                + getToolStats().getBaseEfficiency(stack);

        toolTag.putFloat(ToolHelper.TOOL_SPEED_KEY, speed);
        return speed;
    }

    @Override
    public float getTotalAttackDamage(ItemStack stack) {
        if (isOutOfEnergy(stack)) return 0.0F;

        CompoundTag toolTag = ToolHelper.getToolTag(stack);
        if (toolTag.contains(ToolHelper.ATTACK_DAMAGE_KEY)) {
            return toolTag.getFloat(ToolHelper.ATTACK_DAMAGE_KEY);
        }

        ToolProperty prop = getActiveToolProperty(stack);
        if (prop == null) return 0.0F;

        float baseDamage = getToolStats().getBaseDamage(stack);
        float damage = baseDamage == Float.MIN_VALUE ? 0F : prop.getAttackDamage() + baseDamage;

        toolTag.putFloat(ToolHelper.ATTACK_DAMAGE_KEY, damage);
        return damage;
    }

    @Override
    public float getTotalAttackSpeed(ItemStack stack) {
        if (isOutOfEnergy(stack)) return 0.0F;

        CompoundTag toolTag = ToolHelper.getToolTag(stack);
        if (toolTag.contains(ToolHelper.ATTACK_SPEED_KEY)) {
            return toolTag.getFloat(ToolHelper.ATTACK_SPEED_KEY);
        }

        ToolProperty prop = getActiveToolProperty(stack);
        if (prop == null) return 0.0F;

        float speed = prop.getAttackSpeed() + getToolStats().getAttackSpeed(stack);
        toolTag.putFloat(ToolHelper.ATTACK_SPEED_KEY, speed);
        return speed;
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
            // mining stuff can trigger behaviours on block destroyed
            // make sure we run those here else tools wont work so well
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
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (isOutOfEnergy(stack)) return false;
        if (StarTMultitoolMode.getActive(stack) == null) return false;

        // correct tool for drops should use our own harvest level
        return ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isOutOfEnergy(stack)) return 1.0F;
        if (StarTMultitoolMode.getActive(stack) == null) return 1.0F;

        // if it's an effective tool for the block then reteurn the tool speed
        if (ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack))) {
            return getTotalToolSpeed(stack);
        }
        
        return 1.0F;
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

        @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {

        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
        if (active != null) {
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.hint")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            // show fancy description for empty tool
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
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("item.start_core.gregtech_multitool.installed")
                    .withStyle(ChatFormatting.GREEN));
            for (StarTMultitoolMode mode : installed) {
                tooltipComponents.add(Component.literal("  ").append(mode.displayName()));
            }
            tooltipComponents.add(Component.empty());
        }

        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null) return;

        // this is all taken from definition$appendHoverText
        // but we only wanna show some stuff and from the INSTANCE
        // so yea

        // show eu charge (same regardless of tool)
        ElectricStats.addCurrentChargeTooltip(tooltipComponents, getCharge(stack), getMaxCharge(stack), getElectricTier(), false);

        // dont show anything else if no active
        if (active == null) return;

        IGTToolDefinition proxyStats = StarTMultitoolDefinition.INSTANCE;

        // attack Info
        if (proxyStats.isSuitableForAttacking(stack)) {
            tooltipComponents.add(Component.translatable("item.gtceu.tool.tooltip.attack_damage",
                    FormattingUtil.formatNumbers(2 + this.getTotalAttackDamage(stack))));
            tooltipComponents.add(Component.translatable("item.gtceu.tool.tooltip.attack_speed",
                    FormattingUtil.formatNumbers(4 + this.getTotalAttackSpeed(stack))));
        }

        // mining Info
        if (proxyStats.isSuitableForBlockBreak(stack)) {
            tooltipComponents.add(Component.translatable("item.gtceu.tool.tooltip.mining_speed",
                    FormattingUtil.formatNumbers(this.getTotalToolSpeed(stack))));

            int harvestLevel = this.getTotalHarvestLevel(stack);
            String harvestName = "item.gtceu.tool.harvest_level." + harvestLevel;
            if (Language.getInstance().has(harvestName)) { // Requires Language import
                tooltipComponents.add(Component.translatable("item.gtceu.tool.tooltip.harvest_level_extra", harvestLevel,
                        Component.translatable(harvestName)));
            } else {
                tooltipComponents.add(Component.translatable("item.gtceu.tool.tooltip.harvest_level", harvestLevel));
            }
        }
        tooltipComponents.add(CommonComponents.EMPTY);

        // behaviors & AOE
        AoESymmetrical aoeDefinition = proxyStats.getAoEDefinition(stack);
        boolean addedMagneticOrAOELine = false;
        if (!aoeDefinition.isZero()) {
            tooltipComponents.add(Component.translatable("item.gtceu.tool.behavior.aoe_mining",
                    aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
            addedMagneticOrAOELine = true;
        }

        CompoundTag behaviorsTag = ToolHelper.getBehaviorsTag(stack);
        if (behaviorsTag.getBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY)) {
            tooltipComponents.add(Component.translatable("item.gtceu.tool.behavior.relocate_mining"));
            addedMagneticOrAOELine = true;
        }

        // put all the behaviour toolotips in
        int length = tooltipComponents.size();
        StarTMultitoolDefinition.INSTANCE.getBehaviors(stack).forEach(behavior -> 
                behavior.addInformation(stack, level, tooltipComponents, isAdvanced));

        if (tooltipComponents.size() != length || addedMagneticOrAOELine) {
            tooltipComponents.add(CommonComponents.EMPTY);
        }

        // default enchatments for the stack
        var defaultEnchants = this.getDefaultEnchantments(stack);
        if (!defaultEnchants.isEmpty()) {
            tooltipComponents.add(Component.translatable("item.gtceu.tool.tooltip.default_enchantments"));
            for (var entry : defaultEnchants.entrySet()) {
                Enchantment enchant = entry.getKey();
                if (enchant == null) continue;
                tooltipComponents.add(enchant.getFullname(entry.getValue()));
            }
            tooltipComponents.add(CommonComponents.EMPTY);
        }

        // the "usable as stuff"
        tooltipComponents.add(Component.translatable("item.gtceu.tool.usable_as",
                this.getToolClassNames(stack).stream()
                        .filter(s -> Language.getInstance().has("gtceu.tool.class." + s))
                        .map(s -> Component.translatable("gtceu.tool.class." + s))
                        .collect(Component::empty, FormattingUtil::combineComponents,
                                FormattingUtil::combineComponents)));
    }
}