package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class StarTLightningOutputHatchPartMachine extends TieredIOPartMachine implements IDataInfoProvider {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
        StarTLightningOutputHatchPartMachine.class,
        TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    protected NotifiableEnergyContainer energyContainer;

    @Getter
    private final int amperage;

    public StarTLightningOutputHatchPartMachine(IMachineBlockEntity holder, int tier, int amperage) {
        super(holder, tier, IO.OUT);

        this.amperage = amperage;

        long maxEUt = getMaxOutputEUt(tier, amperage);
        this.energyContainer = NotifiableEnergyContainer.emitterContainer(this, maxEUt, GTValues.V[tier], amperage);
        this.energyContainer.setSideOutputCondition(side -> side == getFrontFacing());
    }

    public static long getMaxOutputEUt(int tier, int amperage) {
        return GTValues.V[tier] * (long) amperage;
    }

    public long getMaxOutputEUt() {
        return getMaxOutputEUt(getTier(), amperage);
    }

    public long getOutputSpace() {
        return energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
    }

    public long acceptLightningEnergy(long amount) {
        long accepted = Math.min(amount, getMaxOutputEUt());
        accepted = Math.min(accepted, getOutputSpace());

        if (accepted <= 0) {
            return 0L;
        }

        return energyContainer.changeEnergy(accepted);
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL
            || mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return List.of(
                Component.translatable("gtceu.universal.tooltip.voltage_out",
                    FormattingUtil.formatNumbers(GTValues.V[getTier()]), GTValues.VNF[getTier()]),
                Component.translatable("gtceu.universal.tooltip.amperage_out", amperage),
                Component.literal("%s/%s EU".formatted(
                    FormattingUtil.formatNumbers(energyContainer.getEnergyStored()),
                    FormattingUtil.formatNumbers(energyContainer.getEnergyCapacity())))
                    .withStyle(ChatFormatting.GRAY));
        }
        return List.of();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }
}
