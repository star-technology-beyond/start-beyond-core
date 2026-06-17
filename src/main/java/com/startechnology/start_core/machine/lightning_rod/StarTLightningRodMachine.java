package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.startechnology.start_core.machine.modular.StarTModularConduitHatchPartMachine;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;


import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarTLightningRodMachine extends WorkableElectricMultiblockMachine {

    private static final int STRIKES_PER_STORM = 5; // Maximum strikes that can occur without a weather change
    private static final int STORM_COOLDOWN = 24000; // Amount of time before multi can be struck again
    private static final double DECAY_PER_TICK = 0.0025;
    private boolean fullDynamo = false; // Controller flag
    private boolean doLightningStrike = true;

    @Getter
    private final int tier;
    @Getter
    private int euT = 0;

    @Persisted
    @DescSynced
    private long unstableEU = 0;

    @Persisted
    @DescSynced
    private long timeSinceLastStorm = 23800;

    @Persisted
    @DescSynced
    @Getter
    @Setter
    private int strikesThisStorm = 0;

    public StarTLightningRodMachine(IMachineBlockEntity holder, int tier) {
        super(holder);

        this.tier = tier;

    }

    //@Override
    //public void onStructureFormed() {
    //    for (IMultiPart part : getParts()) {
    //        if (part instanceof StarTModularConduitHatchPartMachine coil) {
    //              { coil.getPartPositions();
    //    }
    //}


    private static int getGeneratedUnstableEU(int tier) { // How much uEU is generated per strike (tier dependent)
        return switch (tier) {
            case GTValues.LV -> 11378;
            case GTValues.MV -> 45511;
            case GTValues.HV -> 182044;
            case GTValues.EV -> 728178;
            default -> 11378;
        };
    }

    private void lightningStrikeCheck() {
        int strikeChance = (int)(Math.random() * 6); // Controls chance of thunder

        if (strikeChance == 1) {
            strikesThisStorm += 1;
            doLightningStrike = true;
        } else {
            doLightningStrike = false;
        }
    }

    private void LightningStrike() {
        long generatedUnstableEU = getGeneratedUnstableEU(tier); // gets related uEU amount

        unstableEU =  unstableEU + generatedUnstableEU;// Adds to the multis internal buffer, but will not cross the limit
    }


    private String getWeather() { // returns the current weather
        if (getLevel().isThundering())
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
            textList.add(Component.translatable("lightning.start_core.lightning_controller.energy")
                .append(Component.literal("%s EU/t".formatted(FormattingUtil.formatNumbers(euT)))
                    .withStyle(ChatFormatting.GREEN)));
        }

        textList.add(Component.translatable("lightning.start_core.lightning_controller.weather_indicator")
            .append(Component.translatable(getWeather())));
        textList.add(Component.translatable("lightning.start_core.lightning_controller.unstable_eu_buffer")
            .append(Component.literal("" + unstableEU)
                .withStyle(ChatFormatting.BLUE)));
        textList.add(Component.translatable("lightning.start_core.lightning_controller.strikes_left")
            .append(Component.literal("" + (STRIKES_PER_STORM - strikesThisStorm))
                .withStyle(ChatFormatting.GOLD)));
        textList.add(Component.translatable("lightning.start_core.lightning_controller.cooldown_timer")
            .append(Component.literal(cooldownPeriod())
                .withStyle(ChatFormatting.BLUE)));

    }

    public static class StarTLightningRodRecipeLogic extends RecipeLogic {

        public StarTLightningRodRecipeLogic(StarTLightningRodMachine machine) {
            super(machine);
        }
        private static final int UPDATE_INTERVAL = 100;

        @Override
        public StarTLightningRodMachine getMachine(){return (StarTLightningRodMachine) super.getMachine();}

        public void produceEnergy(){
            EnergyContainerList energyContainer = getMachine().energyContainer;
            getMachine().euT = Math.max(0, (int) (getMachine().unstableEU * 0.3155));

            if (energyContainer == null || getMachine().euT <= 0) {
                return;
            }

            long resultEnergy = energyContainer.getEnergyStored() + getMachine().euT;

            if (resultEnergy >= 0L && 0 == energyContainer.getEnergyCapacity()) {
                getMachine().fullDynamo = false;
                energyContainer.changeEnergy(getMachine().euT);
                getMachine().unstableEU = Math.max(0, getMachine().unstableEU - getMachine().euT);

            } else {
                getMachine().fullDynamo = true;
            }
        }

        public void serverTick() {
            var machine = getMachine();

            if (!machine.isFormed || !isWorkingEnabled()) {
                machine.euT = 0;
                setStatus(Status.IDLE);
                isActive = false;
                return;
            }

            if (machine.getLevel().getGameTime() % 100 == 0) {
                if (machine.strikesThisStorm < STRIKES_PER_STORM) {
                    machine.lightningStrikeCheck();
                }
            }

            if (machine.timeSinceLastStorm < STORM_COOLDOWN) {
                machine.timeSinceLastStorm += 1;
            } else if (machine.timeSinceLastStorm == STORM_COOLDOWN) {
                if (machine.getWeather().equals("lightning.start_core.lightning_controller.weather_thunder")){
                    if (machine.strikesThisStorm < STRIKES_PER_STORM) {
                        if (machine.doLightningStrike) {
                            machine.LightningStrike();
                        }
                    } else if (machine.strikesThisStorm == STRIKES_PER_STORM) {
                        machine.timeSinceLastStorm = 0;
                    }
                }
            }

            if ((machine.getWeather().equals("lightning.start_core.lightning_controller.weather_clear" ) ||
                machine.getWeather().equals("lightning.start_core.lightning_controller.weather_rain"))
                && machine.strikesThisStorm == STRIKES_PER_STORM) {
                machine.strikesThisStorm = 0;
            }

            if (machine.unstableEU > 0) {
                produceEnergy();

                machine.unstableEU -= Math.max(1L,
                    Math.round(machine.unstableEU * DECAY_PER_TICK));
            }

            isActive = machine.unstableEU > 0;
            setStatus(isActive ? Status.WORKING : Status.IDLE);

            progress = (progress + 1) % UPDATE_INTERVAL;

        }

        @Override
        public int getMaxProgress() {
            return UPDATE_INTERVAL;
        }
    }
}

