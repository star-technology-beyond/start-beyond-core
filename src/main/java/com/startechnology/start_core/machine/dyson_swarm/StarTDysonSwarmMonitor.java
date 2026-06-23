package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.startechnology.start_core.machine.modular.StarTModularInterfaceHatchPartMachine;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class StarTDysonSwarmMonitor extends WorkableElectricMultiblockMachine {


    @Getter
    @Setter
    private long CollectorEUT = 0; //leaving this here just in case we wanna have the eut displayed on both the collector and the monitor

    // using these 4 to display in controller and to communicate between the 3 locations
    @Getter
    private int totalSwarmCount = 0;
    @Getter
    @Setter
    private int mirrorCount = 0;

    @Getter
    @Setter
    private int shieldCount = 0;

    @Getter
    @Setter
    private int amplifierCount = 0;

    private int collectorTier = 0;
    private int railgunTier = 0;
    private int maxSwarmCount;

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
        // check if unlinked
        for (StarTModularInterfaceHatchPartMachine railgunTerminal : railgunTerminals) {
            if (!railgunTerminal.isCurrentlyLinked()) {
                railgunTier = 0;
                starTDysonRailgunModule = null;
                //TODO eject all swarms
                break;
            }
        }

        for (StarTModularInterfaceHatchPartMachine collectorTerminal : collectorTerminals) {
            if (!collectorTerminal.isCurrentlyLinked()) {
                collectorTier = 0;
                starTDysonCollectorModule = null;
                break;
            }
        }

        if (!readyToUpdate) return;

        this.totalSwarmCount = mirrorCount + amplifierCount + shieldCount;

        if (starTDysonRailgunModule != null) {
            this.railgunTier = starTDysonRailgunModule.getTier();
        }

        if (starTDysonCollectorModule != null) {
            this.collectorTier = starTDysonCollectorModule.getTier();
            starTDysonCollectorModule.setRailgunTier(railgunTier);
            starTDysonCollectorModule.setMirrorCount(mirrorCount);
            starTDysonCollectorModule.setAmplifierCount(amplifierCount);
            this.CollectorEUT = starTDysonCollectorModule.getEuT();
        }
    }

    private void runBreakage() {
        if (!readyToUpdate) return;
        /* TODO
         * Update controller ui with counts and avg durability %, run through the durabilities hashmap and deal damage to all dependent on shield count (not in this order).
         * If the durability reaches 0, remove from hashmap, get the tier and type from the id and take 1 off the typeCount array in the position of tier.
         */

        double breakChanceMultiplier = (railgunTier == GTValues.UIV) ? 0.25 :
                (railgunTier == GTValues.UEV) ? 0.5 : 1;
        double mirrorBreakChance = breakChanceMultiplier * (30 * (Math.pow(((double) (this.shieldCount + 50) / 50), -0.6)) +
                Math.pow(0.0175 * this.amplifierCount, 1.2));
        double amplifierBreakChance = breakChanceMultiplier * (40 * (Math.pow(((double) (this.shieldCount + 50) / 50), -0.6)));
        double shieldBreakChance = breakChanceMultiplier * (20 + (Math.pow(0.0175 * this.amplifierCount, 1.2)));

//        System.out.printf("Mirror Break Chance: %.2f \n Amplifier Break Chance: %.2f \n Shield Break Chance: %.2f \n Break Chance Multiplier: %.2f \n", mirrorBreakChance, amplifierBreakChance, shieldBreakChance, breakChanceMultiplier);

        // Calculates how many swarms are broken based off the percentages above, then updates the values
        this.mirrorCount -= (int) Math.ceil((this.mirrorCount * mirrorBreakChance) / 100);
        this.amplifierCount -= (int) Math.ceil((this.amplifierCount * amplifierBreakChance) / 100);
        this.shieldCount -= (int) Math.ceil((this.shieldCount * shieldBreakChance) / 100);

    }

    private void ejectSwarms(String type, int count, boolean all) {
        /* TODO should be pretty straight forward to understand what I'm planning here
           Note: don't forget to run this whenever railgun unforms
           Note: run breakage before ejecting
         */
    }

    public static class StarTDysonSwarmMonitorLogic extends RecipeLogic {
        private static final int UPDATE_INTERVAL = 12000;

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

            //update modules every 50t (2.5s) and break swarms every 10 mins
            if (progress % 50 == 0) {
                if (progress == 0) {
                    machine.runBreakage();
                    machine.updateModules();
                }
                else {
                    machine.updateModules();
                }
            }

            if (machine.railgunTier == 0 && machine.collectorTier == 0) {
                setWaiting(Component.translatable("dyson_swarm.start_core.monitor.module_waiting_reason"));
                isActive = false;
                return;
            } else if (machine.railgunTier == 0) {
                setWaiting(Component.translatable("dyson_swarm.start_core.monitor.railgun_waiting_reason"));
                isActive = false;
                return;
            } else if (machine.collectorTier == 0) {
                setWaiting(Component.translatable("dyson_swarm.start_core.monitor.collector_waiting_reason"));
                isActive = false;
                return;
            }

            isActive = true;
            setStatus(Status.WORKING);

            progress = (progress + 1) % UPDATE_INTERVAL;
        }
    }


}
