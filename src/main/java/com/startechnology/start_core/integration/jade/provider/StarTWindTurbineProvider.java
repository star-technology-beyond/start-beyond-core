package com.startechnology.start_core.integration.jade.provider;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.machine.wind_turbine.StarTWindTurbineMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class StarTWindTurbineProvider extends CapabilityBlockProvider<StarTWindTurbineMachine> {
    public StarTWindTurbineProvider() {
        super(StarTCore.resourceLocation("wind_turbine_info"));
    }

    @Override
    protected @Nullable StarTWindTurbineMachine getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof MetaMachineBlockEntity metaMachineBlockEntity
            && metaMachineBlockEntity.getMetaMachine() instanceof StarTWindTurbineMachine windTurbine) {
            return windTurbine;
        }

        return null;
    }

    @Override
    protected void write(CompoundTag data, StarTWindTurbineMachine capability) {
        data.putBoolean("formed", capability.isFormed());
        data.putInt("euT", capability.getEuT());
        data.putBoolean("usingLubricant", capability.isUsingLubricant());
        data.putBoolean("usingSeedOil", capability.isUsingSeedOil());
        data.putBoolean("isCrowded", capability.isCrowded());
        data.putInt("tier", capability.getTier());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.getBoolean("formed")) return;

        int euT = capData.getInt("euT");
        if (euT <= 0) {
            tooltip.add(Component.translatable(("wind.start_core.wind_controller.waiting_for_fluid")).withStyle(ChatFormatting.YELLOW));
            return;
        }
        tooltip.add(Component.translatable("gtceu.top.energy_production").append(" ").append(getEnergyProductionText(euT, capData.getInt("tier"))));

        if (capData.getBoolean("usingLubricant")) {
            tooltip.add(Component.translatable("wind.start_core.wind_controller.lubricant_boost"));
        } else if (capData.getBoolean("usingSeedOil")) {
            tooltip.add(Component.translatable("wind.start_core.wind_controller.seed_oil"));
        }

        double weatherMultiplier = capData.getDouble("weatherMultiplier");
        if (weatherMultiplier > 1.0) {
            tooltip.add(Component.translatable(
                "wind.start_core.wind_controller.weather_boost",
                FormattingUtil.formatNumbers(weatherMultiplier)
            ));
        }

        if (capData.getBoolean("isCrowded")) {
            tooltip.add(Component.translatable("wind.start_core.wind_controller.crowding_penalty"));
        }
    }

    private MutableComponent getEnergyProductionText(int euT, int tier) {
        float minAmperage = (float) euT / GTValues.V[tier];

        MutableComponent text = Component.translatable(
            "gtceu.recipe.eu.total",
            FormattingUtil.formatNumbers(euT)
        ).withStyle(ChatFormatting.RED);

        MutableComponent voltageTier;
        if (tier < GTValues.TIER_COUNT - 1) {
            voltageTier = Component.literal(GTValues.VNF[tier])
                .withStyle(style -> style.withColor(GTValues.VC[tier]));
        } else {
            int calculatedSpeed = Mth.ceil(Math.log((double) euT / GTValues.V[GTValues.MAX]) / Math.log(4));
            int speed = Mth.clamp(calculatedSpeed, 0, GTValues.TIER_COUNT);

            if (speed == 0) {
                voltageTier = Component.literal(GTValues.VNF[tier])
                    .withStyle(style -> style.withColor(GTValues.VC[tier]));
            } else {
                minAmperage = (float) (minAmperage / Math.pow(4, speed));
                voltageTier = Component.literal("MAX")
                    .withStyle(style -> style.withColor(TooltipHelper.rainbowColor(speed)))
                    .append(Component.literal("+")
                        .withStyle(style -> style.withColor(GTValues.VC[speed]))
                        .append(FormattingUtil.formatNumbers(speed)));
            }
        }

        text.append(Component.translatable(
            "gtceu.universal.padded_parentheses",
            Component.translatable(
                "gtceu.recipe.eu.amp_notation",
                FormattingUtil.formatNumber2Places(minAmperage),
                voltageTier
            ).withStyle(ChatFormatting.WHITE)
        ));

        return text;
    }
}
