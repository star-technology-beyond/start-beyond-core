package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.machine.modular.StarTModularInterfaceHatchPartMachine;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StarTDysonCollectorModule extends WorkableElectricMultiblockMachine {

    private static final List<ResourceLocation> MODULE_ID = new ArrayList<>();
    private final List<ResourceLocation> acceptedMultiblockIds;
    private final int tier;

    @Getter
    private final int[] maxSwarmCounts = { 250, 500, 1000 }; //placeholder nums, may change when balancing

    @Persisted
    private int runningTimer = 0;

    protected List<StarTModularInterfaceHatchPartMachine> terminals;

    public StarTDysonCollectorModule(IMachineBlockEntity holder, int tier, ResourceLocation... acceptedMultiblockIds) {
        super(holder);

        this.tier = tier;
        this.acceptedMultiblockIds = List.copyOf((Arrays.asList(acceptedMultiblockIds)));
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        this.setupTerminals();
//        StarTDysonSwarmMonitor.setCollectorTier(this.tier);
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

    private void setupTerminals() {
        for (StarTModularInterfaceHatchPartMachine terminal : terminals) {
            terminal.setSupportedModules(MODULE_ID); //not entirely sure about how to add to this, will ask when u awake
        }
    }

    private void doLogic() {
        int euT = 0;
        // get mirror, and amplifier counts. Then work out the power output based off those nums. Then output the power.
    }


}
