package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.machine.modular.StarTModularInterfaceHatchPartMachine;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StarTDysonRailgunModule extends WorkableElectricMultiblockMachine {

    protected List<ResourceLocation> acceptedMultiblockIds;
    private final int tier;

    @Persisted
    private int runningTimer = 0;

    protected List<StarTModularInterfaceHatchPartMachine> terminals = new ArrayList<>();
    private boolean readyToUpdate;

    public StarTDysonRailgunModule(IMachineBlockEntity holder, int tier, ResourceLocation... acceptedMultiblockIds) {
        super(holder);

        this.tier = tier;
        this.acceptedMultiblockIds = Arrays.asList(acceptedMultiblockIds);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        this.readyToUpdate = false;

        this.setupTerminals();
        this.readyToUpdate = true;
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
        for (IMultiPart part : getParts()) {
            if (part instanceof  StarTModularInterfaceHatchPartMachine terminal) {
                terminal.setSupportedModules(acceptedMultiblockIds);
            }
        }
    }

    private void doLogic() {
        /*
         * I think I need a new recipe type here? But logic being when input, check if has starting power to fire in its energy hatch. Then check swarm type and tier.
         * Then add 1 to the type of module in the position of the tiers part of typeCounts array of StarTDysonSwarmMonitor (the monitor), and add
         * the durability to the hashmap storing durabilities with a unique id of tier_type_count. REMEMBER TO CHECK IF AIRSPACE IS FULL BEFORE STARTING THE RECIPE.
         * */
    }

}
