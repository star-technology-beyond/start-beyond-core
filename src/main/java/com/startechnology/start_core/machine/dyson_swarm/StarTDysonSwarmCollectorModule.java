package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.machine.modular.StarTModularInterfaceHatchPartMachine;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.chat.Component;


public class StarTDysonSwarmCollectorModule extends WorkableElectricMultiblockMachine {

    protected List<ResourceLocation> acceptedMultiblockIds;

    @Getter
    private int euT = 0;

    @Getter
    private final int tier;

    @Getter
    private final int[] maxSwarmCounts = { 250, 500, 1000 }; //placeholder nums, may change when balancing

    @Setter
    private int mirrorCount;

    @Setter
    private int amplifierCount;

    @Setter
    private int railgunTier;

    private StarTModularInterfaceHatchPartMachine node;
    private boolean ready;

    public StarTDysonSwarmCollectorModule(IMachineBlockEntity holder, int tier, ResourceLocation... acceptedMultiblockIds) {
        super(holder);

        this.tier = tier;
        this.acceptedMultiblockIds = Arrays.asList(acceptedMultiblockIds);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        this.ready = false;

        this.setupTerminals();

        this.ready = true;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();

        resetModule();
    }

    protected RecipeLogic createRecipeLogic(Object... args) {
        return new StarTDysonSwarmCollectorLogic(this);
    }

    private void setupTerminals() {
        for (IMultiPart part : getParts()) {
            if (part instanceof  StarTModularInterfaceHatchPartMachine terminal) {
                this.node = terminal;
            }
        }
    }

//  Get mirror, and amplifier counts. Then work out the power output based off those nums. Then output the power.
    private void doLogic() {

        if(!node.checkSupportedModule()) {
            resetModule();
            return;
        }

        double tierMultiplier = (this.railgunTier == GTValues.UHV) ? 1.0075 :
                (this.railgunTier == GTValues.UEV) ? 1.005 : (this.railgunTier == GTValues.UIV) ? 1.0025 : 0;

        euT = (int) Math.floor(this.mirrorCount * Math.pow(tierMultiplier, this.amplifierCount) * GTValues.V[railgunTier]);
    }

    private void resetModule() {
        mirrorCount = 0;
        amplifierCount = 0;
        railgunTier = 0;
        euT = 0;
        ready = false;
    }

    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return false;
    }

    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed()) return;

        if (isActive()) {
            textList.add(Component.translatable("dyson_swarm.start_core.mirror_count", FormattingUtil.formatNumbers(mirrorCount)));
            textList.add(Component.translatable("dyson_swarm.start_core.amplifier_count", FormattingUtil.formatNumbers(amplifierCount)));
            textList.add(Component.translatable("dyson_swarm.start_core.collector_module.generating", FormattingUtil.formatNumbers(euT)));
        }
    }

    public static class StarTDysonSwarmCollectorLogic extends RecipeLogic {
        private static final int UPDATE_INTERVAL = 100;

        public StarTDysonSwarmCollectorLogic(StarTDysonSwarmCollectorModule machine) {
            super(machine);
        }

        @NotNull
        @Override
        public StarTDysonSwarmCollectorModule getMachine() {
            return (StarTDysonSwarmCollectorModule) super.getMachine();
        }

        private void produceEnergy() {
            EnergyContainerList energyContainer = getMachine().energyContainer;

            if (energyContainer == null || getMachine().euT <= 0) return;

            long resultEnergy = energyContainer.getEnergyStored() + getMachine().euT;

            if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
                energyContainer.changeEnergy(getMachine().euT);
            }
        }

        @Override
        public void serverTick() {
            var machine = getMachine();

            if (!machine.isFormed || !isWorkingEnabled() || !machine.ready) {
                machine.euT = 0;
                setStatus(Status.IDLE);
                isActive = false;
                return;
            }

            if (progress == 0) {
                machine.doLogic();
            }

            isActive = machine.euT > 0;
            setStatus(isActive ? Status.WORKING : Status.IDLE);

            progress = (progress + 1) % UPDATE_INTERVAL;

            produceEnergy();
        }

        @Override
        public int getMaxProgress() {
            return UPDATE_INTERVAL;
        }
    }

}
