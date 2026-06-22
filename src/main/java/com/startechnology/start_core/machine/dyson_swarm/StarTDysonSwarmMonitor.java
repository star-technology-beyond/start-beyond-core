package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.machine.modular.StarTModularInterfaceHatchPartMachine;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class StarTDysonSwarmMonitor extends WorkableElectricMultiblockMachine {


    @Getter
    @Setter
    private int euT = 0; //leaving this here just in case we wanna have the eut displayed on both the collector and the monitor

    @Setter
    private HashMap<String, Integer> durabilities = new HashMap<>();

    // using these 3 to display in controller and to communicate between the 3 locations
    @Getter
    @Setter
    private int mirrorCount = 0;

    @Getter
    private int shieldCount = 0;

    @Getter
    @Setter
    private int amplifierCount = 0;

    @Getter
    @Setter
    private int collectorTier = 0;

    @Getter
    @Setter
    private int railgunTier = 0;

    @Persisted
    private int runningTimer = 0;

    private boolean readyToUpdate;
    private StarTDysonSwarmCollectorModule starTDysonCollectorModule;
    private StarTDysonSwarmRailgunModule starTDysonRailgunModule;

    protected List<ResourceLocation> railgunModuleIds;
    protected List<ResourceLocation> collectorModuleIds;
    protected List<StarTModularInterfaceHatchPartMachine> railgunTerminals = new ArrayList<>();
    protected List<StarTModularInterfaceHatchPartMachine> collectorTerminals = new ArrayList<>();


    public StarTDysonSwarmMonitor(IMachineBlockEntity holder, List<ResourceLocation> railgunModuleIds, List<ResourceLocation> collectorModuleIds) {
        super(holder);

        this.railgunModuleIds = railgunModuleIds;
        this.collectorModuleIds = collectorModuleIds;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        this.readyToUpdate = false;

        // Gather the different terminals for each type
        railgunTerminals = this.getMultiblockState().getMatchContext()
                .getOrDefault(StarTDysonSwarmPredicates.RAILGUN_STORAGE_KEY, new ArrayList<>());
        collectorTerminals = this.getMultiblockState().getMatchContext()
                .getOrDefault(StarTDysonSwarmPredicates.COLLECTOR_STORAGE_KEY, new ArrayList<>());

        this.setupTerminals();

        this.readyToUpdate = true;

    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        collectorTier = 0;
        railgunTier = 0;
    }

    protected RecipeLogic createRecipeLogic(Object... args) {
        return new StarTDysonSwarmMonitorLogic(this);
    }

    private void setupTerminals() {
        for (StarTModularInterfaceHatchPartMachine collectorTerminal : collectorTerminals) {
            collectorTerminal.setSupportedModules(collectorModuleIds);
            collectorTerminal.resetSupportedModule();

            collectorTerminal.setSupportedMachineControllerConsumer(collectorControllerMachine ->
                    this.starTDysonCollectorModule = (StarTDysonSwarmCollectorModule) collectorControllerMachine);

        }

        for (StarTModularInterfaceHatchPartMachine railgunTerminal : railgunTerminals) {
            railgunTerminal.setSupportedModules(railgunModuleIds);
            railgunTerminal.resetSupportedModule();

            railgunTerminal.setSupportedMachineControllerConsumer(railgunControllerMachine ->
                    this.starTDysonRailgunModule = (StarTDysonSwarmRailgunModule) railgunControllerMachine);

        }
    }

    private void updateModules() {
        if (!readyToUpdate) return;

        if (starTDysonRailgunModule != null) {
            railgunTier = starTDysonRailgunModule.getTier();
        }

        if (starTDysonCollectorModule != null) {
            starTDysonCollectorModule.setRailgunTier(railgunTier);
            starTDysonCollectorModule.setMirrorCount(mirrorCount);
            starTDysonCollectorModule.setAmplifierCount(amplifierCount);
        }
    }

    private void doLogic() {
        if (!readyToUpdate) return;
        /* TODO
         * Update controller ui with counts and avg durability %, run through the durabilities hashmap and deal damage to all dependent on shield count (not in this order).
         * If the durability reaches 0, remove from hashmap, get the tier and type from the id and take 1 off the typeCount array in the position of tier.
         */

    }

    private void ejectSwarms(String type, int count, boolean all) {
        /* TODO should be pretty straight forward to understand what I'm planning here
           Note: don't forget to run this whenever railgun unforms
         */
    }

    public static class StarTDysonSwarmMonitorLogic extends RecipeLogic {
        private static final int UPDATE_INTERVAL = 100;

        public StarTDysonSwarmMonitorLogic(StarTDysonSwarmMonitor machine) {
            super(machine);
        }

        @NotNull
        @Override
        public StarTDysonSwarmMonitor getMachine() {
            return (StarTDysonSwarmMonitor) super.getMachine();
        }

        @Override
        public void serverTick() {
            var machine = getMachine();

            if (!machine.isFormed || !isWorkingEnabled()) {
                setStatus(Status.IDLE);
                isActive = false;
                return;
            }

            if (progress == 0 || progress == 50) {
                if (progress == 0) {
                    machine.updateModules();
                    machine.doLogic();
                }
                else {
                    machine.updateModules();
                }
            }

            setStatus(Status.WORKING);
        }
    }


}
