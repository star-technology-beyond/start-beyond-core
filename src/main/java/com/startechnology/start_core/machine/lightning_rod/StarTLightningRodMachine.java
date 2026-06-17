package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;


import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarTLightningRodMachine extends WorkableElectricMultiblockMachine {

    private static final int STRIKES_PER_STORM = 5; // Maximum strikes that can occur without a weather change
    private static final int STORM_COOLDOWN = 24000; // Amount of time before multi can be struck again
    private static final int LIGHTNING_SURGE_TICKS = 100;
    private static final int STRIKE_ROLL_INTERVAL = 100;
    private static final double DECAY_PER_TICK = 0.0025;
    private static final double FULL_COIL_STABILISATION_RATE = 0.3155;
    private boolean fullDynamo = false; // Controller flag

    @Getter
    private final int tier;
    @Getter
    private int euT = 0;

    @Persisted
    @DescSynced
    private long unstableEU = 0;

    @Persisted
    @DescSynced
    private long timeSinceLastStorm = STORM_COOLDOWN;

    @Persisted
    @DescSynced
    @Getter
    @Setter
    private int strikesThisStorm = 0;

    @Persisted
    @DescSynced
    private int surgeTicksRemaining = 0;

    @Persisted
    @DescSynced
    private long surgeEUt = 0;

    public StarTLightningRodMachine(IMachineBlockEntity holder, int tier) {
        super(holder);

        this.tier = tier;

    }

    private static int getGeneratedUnstableEU(int tier) { // How much uEU is generated per strike (tier dependent)
        return switch (tier) {
            case GTValues.LV -> 11378;
            case GTValues.MV -> 45511;
            case GTValues.HV -> 182044;
            case GTValues.EV -> 728178;
            default -> 11378;
        };
    }

    // Slightly cleaner method of Thunderstorm check
    private boolean isThunderstorm() {
        return getLevel() != null && getLevel().isThundering();
    }

    private boolean shouldStartLightningSurge() {
        return Math.random() < (1.0 / 3.0);
    }

    // Counter for Surge/Power Gen and starts countdown
    private void startLightningSurge() {
        strikesThisStorm++;
        surgeEUt = getGeneratedUnstableEU(tier);
        surgeTicksRemaining = LIGHTNING_SURGE_TICKS;

        if (strikesThisStorm >= STRIKES_PER_STORM) {
            timeSinceLastStorm = 0;
        }
    }

    // Generates Surge Power
    private void generateSurgeTick() {
        if (surgeTicksRemaining <= 0) {
            return;
        }

        unstableEU += surgeEUt;
        surgeTicksRemaining--;
    }

    // Decay centralised into the one method
    private void decayUnstableEU() {
        if (unstableEU <= 0) {
            return;
        }

        long decay = Math.max(1L, Math.round(unstableEU * DECAY_PER_TICK));
        unstableEU = Math.max(0L, unstableEU - decay);
    }

    private String getWeather() { // returns the current weather
        if (isThunderstorm())
            return "lightning.start_core.lightning_controller.weather_thunder";

        if(getLevel().isRaining())
            return "lightning.start_core.lightning_controller.weather_rain";

        return  "lightning.start_core.lightning_controller.weather_clear";
    }

    private String cooldownPeriod() { // converts cooldown into minutes and seconds
        int cooldownTicks = Math.toIntExact(STORM_COOLDOWN - timeSinceLastStorm);
        int totalSeconds = cooldownTicks / 20;
        int cooldownMinutes = totalSeconds / 60;
        int cooldownSeconds = totalSeconds % 60;

        return "%dm %02ds".formatted(cooldownMinutes, cooldownSeconds);

    }

    // Adjusted old EnergyContainer to new LightningHatch (Name Pending still idk)
    private StarTLightningOutputHatchPartMachine getLightningOutputHatch() {
        for (IMultiPart part : getParts()) {
            if (part instanceof StarTLightningOutputHatchPartMachine hatch && hatch.getTier() == tier) {
                return hatch;
            }
        }

        return null;
    }

    protected RecipeLogic createRecipeLogic(Object... args){ return new StarTLightningRodRecipeLogic(this);}

    @Override
    public boolean isGenerator(){ return true;}

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed()) {
            return;
        }

        if (unstableEU == 0) {
            textList.add(Component.translatable("lightning.start_core.lightning_controller.no_energy"));

        } else if (euT == 0) {
            textList.add(Component.translatable("lightning.start_core.lightning_controller.low_energy"));

        } else if (fullDynamo) {
            textList.add(Component.translatable("lightning.start_core.lightning_controller.full_dynamo"));
        }
        else {
            textList.add(Component.translatable("lightning.start_core.lightning_controller.energy",
                FormattingUtil.formatNumbers(euT)));
        }

        textList.add(Component.translatable("lightning.start_core.lightning_controller.weather_indicator",
            Component.translatable(getWeather())));
        textList.add(Component.translatable("lightning.start_core.lightning_controller.unstable_eu_buffer",
            FormattingUtil.formatNumbers(unstableEU)));
        textList.add(Component.translatable("lightning.start_core.lightning_controller.strikes_left",
            STRIKES_PER_STORM - strikesThisStorm));
        textList.add(Component.translatable("lightning.start_core.lightning_controller.cooldown_timer",
            cooldownPeriod()));

    }

    public static class StarTLightningRodRecipeLogic extends RecipeLogic {

        public StarTLightningRodRecipeLogic(StarTLightningRodMachine machine) {
            super(machine);
        }
        private static final int UPDATE_INTERVAL = 100;

        @Override
        public StarTLightningRodMachine getMachine(){return (StarTLightningRodMachine) super.getMachine();}

        public void produceEnergy(){
            StarTLightningRodMachine machine = getMachine();
            StarTLightningOutputHatchPartMachine hatch = machine.getLightningOutputHatch();

            if (hatch == null || machine.unstableEU <= 0) {
                machine.euT = 0;
                machine.fullDynamo = hatch == null;
                return;
            }

            long desiredEUt = Math.max(0L, Math.round(machine.unstableEU * FULL_COIL_STABILISATION_RATE));

            if (desiredEUt <= 0) {
                machine.euT = 0;
                return;
            }

            long accepted = hatch.acceptLightningEnergy(desiredEUt);

            // Check here to ensure that only accepted amount of EU in Hatch is output and if no space then stays unstable to decay
            machine.euT = Math.toIntExact(Math.min(Integer.MAX_VALUE, accepted));
            machine.fullDynamo = accepted < desiredEUt;

            if (accepted > 0) {
                machine.unstableEU = Math.max(0L, machine.unstableEU - accepted);
            }
        }

        public void serverTick() {
            var machine = getMachine();

            if (!machine.isFormed || !machine.isWorkingEnabled()) {
                machine.decayUnstableEU();
                machine.euT = 0;
                setStatus(Status.IDLE);
                isActive = false;
                return;
            }

            if (machine.timeSinceLastStorm < STORM_COOLDOWN) {
                machine.timeSinceLastStorm++;

                if (machine.timeSinceLastStorm >= STORM_COOLDOWN) {
                    machine.strikesThisStorm = 0;
                }
            }

            // Check for whether strike can occur - must meet below requirements
            boolean canRollStrike = machine.isThunderstorm()
                && machine.timeSinceLastStorm >= STORM_COOLDOWN
                && machine.strikesThisStorm < STRIKES_PER_STORM
                && machine.surgeTicksRemaining <= 0
                && machine.getLevel().getGameTime() % STRIKE_ROLL_INTERVAL == 0;

            if (canRollStrike && machine.shouldStartLightningSurge()) {
                machine.startLightningSurge();
            }

            machine.generateSurgeTick();

            if (machine.unstableEU > 0) {
                produceEnergy();
                machine.decayUnstableEU();
            } else {
                machine.euT = 0;
                machine.fullDynamo = false;
            }

            isActive = machine.unstableEU > 0 || machine.surgeTicksRemaining > 0;
            setStatus(isActive ? Status.WORKING : Status.IDLE);

            progress = (progress + 1) % UPDATE_INTERVAL;

        }

        @Override
        public int getMaxProgress() {
            return UPDATE_INTERVAL;
        }
    }
}
