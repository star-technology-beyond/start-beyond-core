package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;

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
    @Getter
    private int euT = 0;

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

    protected RecipeLogic createRecipeLogic(Object... args){ return new StarTLightningRodRecipeLogic(this);}

    @Override
    public boolean isGenerator(){ return true;}


    public static class StarTLightningRodRecipeLogic extends RecipeLogic {
        private static final int UPDATE_INTERVAL = 0;

        public StarTLightningRodRecipeLogic(StarTLightningRodMachine machine) {
            super(machine);
        }

        @Override
        public StarTLightningRodMachine getMachine(){return (StarTLightningRodMachine) super.getMachine();}

        public void serverTick() {
            var machine = getMachine();

            if (!machine.isFormed || !isWorkingEnabled()) {
                machine.euT = 0;
                setStatus(Status.IDLE);
                isActive = false;
                return;
            }

            if (machine.timeSinceLastStorm < STORM_COOLDOWN) {
                machine.timeSinceLastStorm += 1;
            } else if (machine.timeSinceLastStorm == STORM_COOLDOWN) {
                if (machine.getWeather().equals("Thundering")){
                    if (machine.strikesThisStorm < STRIKES_PER_STORM) {
                        machine.LightingStrike();
                    } else if (machine.strikesThisStorm == STRIKES_PER_STORM) {
                        machine.timeSinceLastStorm = 0;
                        machine.strikesThisStorm = 0;
                    }
                }
            }

            if (machine.unstableEU > 0) {
                machine.unstableEU -= (long) Math.max(1, machine.unstableEU * DECAY_PER_TICK);
            }
        }
    }
}

