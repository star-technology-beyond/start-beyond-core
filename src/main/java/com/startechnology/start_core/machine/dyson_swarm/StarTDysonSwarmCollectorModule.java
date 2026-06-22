package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.machine.modular.StarTModularInterfaceHatchPartMachine;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class StarTDysonSwarmCollectorModule extends WorkableElectricMultiblockMachine {

    @Getter
    private int euT = 0;

    @Getter
    private final int tier;

    @Getter
    private final int[] maxSwarmCounts = { 250, 500, 1000 }; //placeholder nums, may change when balancing

    @Persisted
    private int runningTimer = 0;

    protected List<ResourceLocation> acceptedMultiblockIds;
    private boolean readyToUpdate;
    private StarTDysonSwarmMonitor starTDysonSwarmMonitor;

    public StarTDysonSwarmCollectorModule(IMachineBlockEntity holder, int tier, ResourceLocation... acceptedMultiblockIds) {
        super(holder);

        this.tier = tier;
        this.acceptedMultiblockIds = Arrays.asList(acceptedMultiblockIds);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        this.readyToUpdate = false;

        this.setupTerminals();
        starTDysonSwarmMonitor.setCollectorTier(this.tier);

        this.readyToUpdate = true;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();

        // runs checks every 7.2s 500 times = 1hr
        if (runningTimer % 144 == 0) {
            doLogic();
        }

        runningTimer++;
        if (runningTimer > 72000) runningTimer %= 72000; // resets once every hour of running

        return value;
    }

    protected  RecipeLogic createRecipeLogic(Object... args) {
        return new StarTDysonSwarmCollectorLogic(this);
    }

    private void setupTerminals() {
        for (IMultiPart part : getParts()) {
            if (part instanceof  StarTModularInterfaceHatchPartMachine terminal) {
                terminal.setSupportedModules(acceptedMultiblockIds);
                terminal.resetSupportedModule();

                terminal.setSupportedMachineControllerConsumer(dysonMonitorMachine ->
                        this.starTDysonSwarmMonitor = (StarTDysonSwarmMonitor) dysonMonitorMachine);
            }
        }
    }

//  Get mirror, and amplifier counts. Then work out the power output based off those nums. Then output the power.
    private void doLogic() {
        int mirrorCount = starTDysonSwarmMonitor.getMirrorCount();
        int amplifierCount = starTDysonSwarmMonitor.getAmplifierCount();
        int railgunTier = starTDysonSwarmMonitor.getRailgunTier();

        double tierMultiplier = (railgunTier == GTValues.UHV) ? 1.0075 :
                (railgunTier == GTValues.UEV) ? 1.005 : 1.0025;

        euT = (int) Math.floor(mirrorCount * Math.pow(tierMultiplier, amplifierCount) * GTValues.V[railgunTier]);
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
            var monitor = getMachine().starTDysonSwarmMonitor;

            if (!machine.isFormed || !isWorkingEnabled() || monitor.getRailgunTier() == null || monitor.getCollectorTier() == null) {
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
