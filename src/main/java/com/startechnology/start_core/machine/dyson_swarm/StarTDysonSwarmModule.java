package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.machine.modular.StarTModularInterfaceHatchPartMachine;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StarTDysonSwarmModule extends WorkableElectricMultiblockMachine {

    private static final List<ResourceLocation> MODULE_ID = new ArrayList<>();
    private final List<ResourceLocation> acceptedMultiblockIds;
    private final int tier;
    private final String type;

    @Persisted
    private int runningTimer = 0;

    protected List<StarTModularInterfaceHatchPartMachine> terminals = new ArrayList<>();

    public StarTDysonSwarmModule(IMachineBlockEntity holder, String type, int tier, ResourceLocation... acceptedMultiblockIds) {
        super(holder);
        this.tier = tier;
        this.type = type;
        this.acceptedMultiblockIds = List.copyOf((Arrays.asList(acceptedMultiblockIds)));
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
            switch (type) {
                case "receiver" -> doReceiverLogic();
                case "railgun" -> doRailgunLogic();
                default -> System.out.println("unknown module type:" + type);
            }
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

    private void doReceiverLogic() {
        int euT = 0;
        // get mirror, and amplifier counts. Then work out the power output based off those nums. Then output the power.
    }

    private void doRailgunLogic() {
        /*
        * I think I need a new recipe type here? But logic being when input, check if has starting power to fire in its energy hatch. Then check swarm type and tier.
        * Then add 1 to the type of module in the position of the tiers part of typeCounts array of StarTDysonSwarmMachine (the monitor), and add
        * the durability to the hashmap storing durabilities with a unique id of tier_type_count. REMEMBER TO CHECK IF AIRSPACE IS FULL BEFORE STARTING THE RECIPE.
        * */
    }
}
