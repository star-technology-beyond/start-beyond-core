package com.startechnology.start_core.machine.dyson_swarm;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.startechnology.start_core.machine.modular.StarTModularPredicates;

public class StarTDysonSwarmPredicates {
    public static String RAILGUN_STORAGE_KEY = "railgunModules";
    public static String COLLECTOR_STORAGE_KEY = "collectorModules";

    public static TraceabilityPredicate railgunModulesPredicate = StarTModularPredicates
            .createKeyedInterfaceTerminalPredicate(RAILGUN_STORAGE_KEY);

    public static TraceabilityPredicate collectorModulesPredicate = StarTModularPredicates
            .createKeyedInterfaceTerminalPredicate(COLLECTOR_STORAGE_KEY);
}
