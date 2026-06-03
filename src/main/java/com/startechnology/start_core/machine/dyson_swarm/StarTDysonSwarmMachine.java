package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;


public class StarTDysonSwarmMachine extends WorkableElectricMultiblockMachine {

    private final int tier;
    @Getter
    private int euT = 0;
    @Getter
    private int[] mirrorCounts = new int[3];
    @Getter
    private int[] shieldCounts = new int[3];
    @Getter
    private int[] amplifierCounts = new int[3];
    @Persisted
    private int runningTimer = 0;
    private final String type;


    public StarTDysonSwarmMachine(IMachineBlockEntity holder, String type, int tier) {
        super(holder);
        this.type = type;
        this.tier = tier;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();

        // runs checks every 7.2s
        if (runningTimer % 144 == 0) {
            switch (type) {
                case "monitor" -> {
                    doMonitorLogic();
                }
                case "receiver" -> {
                    doReceiverLogic();
                }
                case "railgun" -> {
                    doRailgunLogic();
                }
                default -> System.out.println("unknown type:" + type);
            }
        }

        runningTimer++;
        if (runningTimer > 72000) runningTimer %= 72000; // resets once every hour of running

        return value;
    }

    private void doMonitorLogic() {

    }

    private void doReceiverLogic() {

    }

    private void doRailgunLogic() {

    }
}
