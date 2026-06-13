package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.startechnology.start_core.machine.wind_turbine.StarTWindTurbineManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarTLightningRodMachine extends WorkableElectricMultiblockMachine {

    private static final int STRIKES_PER_STORM = 5;
    private static final int STORM_COOLDOWN = 24000;
    private static final long MAX_UNSTABLE_EU = 1000000;
    private static final double DECAY_PER_TICK = 0.05;

    @Getter
    private final int tier;

    private long unstableEU = 0;
    private long timeSinceLastStorm = 0;

    @Getter
    @Setter
    private int strikesThisStorm = 0;

    public StarTLightningRodMachine(IMachineBlockEntity holder, int tier) {
        super(holder);

        this.tier = tier;

    }

    public void LightingStrike() {
        long generatedUnstableEU = 1000;

        unstableEU = Math.min(
            unstableEU + generatedUnstableEU,
            MAX_UNSTABLE_EU
        );

        strikesThisStorm += 1;
    }

    public String getWeather() {
        if (getLevel().isThundering())
            return "Thundering";

        return  "Clear";
    }

    public static class StarTLightningRodRecipeLogic extends RecipeLogic {

        public StarTLightningRodRecipeLogic(StarTLightningRodMachine machine) {
            super(machine);
        }

        @NotNull
        @Override
        public StarTLightningRodMachine getMachine(){return (StarTLightningRodMachine) super.getMachine();}

        public void serverTick() {
            var machine = getMachine();

            if (machine.timeSinceLastStorm < STORM_COOLDOWN) {
                machine.timeSinceLastStorm += 1;
            } else if (machine.timeSinceLastStorm == STORM_COOLDOWN) {
                if (machine.getWeather().equals("Thundering")){
                    if (machine.strikesThisStorm < STRIKES_PER_STORM) {
                        machine.LightingStrike();
                    }
                }
            }

            if (machine.unstableEU > 0) {
                machine.unstableEU -= (long) Math.max(1, machine.unstableEU * DECAY_PER_TICK);
            }



        }
    }


}

