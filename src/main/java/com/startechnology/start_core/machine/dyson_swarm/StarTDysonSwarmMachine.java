package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.machine.modular.StarTModularControllerMachine;
import com.startechnology.start_core.machine.modular.StarTModularInterfaceHatchPartMachine;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class StarTDysonSwarmMachine extends StarTModularControllerMachine {

    private static final List<ResourceLocation> MODULE_ID = new ArrayList<>();

    @Getter
    @Setter
    private int euT = 0; //leaving this here just in case we wanna have the eut displayed on both the collector and the monitor

    @Setter
    private HashMap<String, Integer> durabilities = new HashMap<>();

    // using these 3 to display in controller and to communicate between the 3 locations
    @Getter
    @Setter
    private int[] mirrorCounts = new int[3];

    @Getter
    private int[] shieldCounts = new int[3];

    @Getter
    @Setter
    private int[] amplifierCounts = new int[3];

    @Persisted
    private int runningTimer = 0;

    protected List<StarTModularInterfaceHatchPartMachine> terminals = new ArrayList<>();

    public StarTDysonSwarmMachine(IMachineBlockEntity holder, ResourceLocation... acceptedModuleIds) {
        super(holder, acceptedModuleIds);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        this.setupTerminals();
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();

        // runs checks every 7.2s 500 times = 1hr
        if (runningTimer % 144 == 0) {
            doMonitorLogic();
        }

        runningTimer++;
        if (runningTimer > 72000) runningTimer %= 72000; // resets once every hour of running

        return value;
    }

    private void setupTerminals() {
        for (StarTModularInterfaceHatchPartMachine terminal : terminals) {
            terminal.setSupportedModules(MODULE_ID); //not entirely sure about how to add to this, will ask when u awake
        }
    }

    private void doMonitorLogic() {
        /*
         * Update controller ui with counts and avg durability %, run through the durabilities hashmap and deal damage to all dependent on shield count (not in this order).
         * If the durability reaches 0, remove from hashmap, get the tier and type from the id and take 1 off the typeCount array in the position of tier.
         */

    }

    private void ejectSwarms(String type, int count, boolean all) {
        // should be pretty straight forward to understand what I'm planning here
    }


}
