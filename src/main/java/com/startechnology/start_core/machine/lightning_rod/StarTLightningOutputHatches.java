package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.startechnology.start_core.machine.StarTMachineUtils;
import com.startechnology.start_core.machine.StarTPartAbility;
import net.minecraft.network.chat.Component;

public class StarTLightningOutputHatches {
    public static final MachineDefinition[] LIGHTNING_OUTPUT_HATCHES = StarTMachineUtils.registerTieredMachines(
        "lightning_output_hatch",
        (holder, tier) -> new StarTLightningOutputHatchPartMachine(holder, tier, getLightningAmperage(tier)),
        StarTLightningOutputHatches::buildLightningOutputHatch,
        GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV
    );

    private static MachineDefinition buildLightningOutputHatch(int tier, MachineBuilder<MachineDefinition> builder) {
        int amperage = getLightningAmperage(tier);
        long maxEUt = StarTLightningOutputHatchPartMachine.getMaxOutputEUt(tier, amperage);

        return builder
            .langValue(GTValues.VNF[tier] + " Lightning Output Hatch")
            .rotationState(RotationState.ALL)
            .abilities(StarTPartAbility.LIGHTNING_OUTPUT_HATCH)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .tooltips(
                Component.translatable("gtceu.universal.tooltip.voltage_out",
                    FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                Component.translatable("gtceu.universal.tooltip.amperage_out", amperage),
                Component.translatable("start_core.machine.lightning_output_hatch.discharge_buffer",
                    FormattingUtil.formatNumbers(maxEUt)))
            .overlayTieredHullModel(GTCEu.id("block/machine/part/energy_output_hatch"))
            .register();
    }

    public static int getLightningAmperage(int tier) {
        return switch (tier) {
            case GTValues.LV -> 64;
            case GTValues.MV, GTValues.HV -> 256;
            case GTValues.EV -> 512;
            default -> 0;
        };
    }

    public static void init() {
    }
}
