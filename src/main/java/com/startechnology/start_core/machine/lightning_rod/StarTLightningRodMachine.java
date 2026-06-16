package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
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

    private static final int STRIKES_PER_STORM = 5;
    private static final int STORM_COOLDOWN = 24000;
    private static final long MAX_UNSTABLE_EU = 100000000;
    private static final double DECAY_PER_TICK = 0.0025;

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

    @Getter
    @Setter
    private int strikesThisStorm = 0;

    public StarTLightningRodMachine(IMachineBlockEntity holder, int tier) {
        super(holder);

        this.tier = tier;

    }

    private static int getGeneratedUnstableEU(int tier) {
        return switch (tier) {
            case GTValues.LV -> 1137778;
            case GTValues.MV -> 4551111;
            case GTValues.HV -> 18204444;
            case GTValues.EV -> 72817778;
            default -> 1137778;
        };
    }

    private void LightningStrike() {
        long generatedUnstableEU = getGeneratedUnstableEU(tier);

        int strikeChance = (int)(Math.random() * 101);

        if (strikeChance == 1) {
            unstableEU = Math.min(
                unstableEU + generatedUnstableEU,
                MAX_UNSTABLE_EU
            );

            strikesThisStorm += 1;
        }
    }

    private String getWeather() {
        if (getLevel().isThundering())
            return "lightning.start_core.lightning_controller.weather_thunder";

        if(getLevel().isRaining())
            return "lightning.start_core.lightning_controller.weather_rain";

        return  "lightning.start_core.lightning_controller.weather_clear";
    }

    private String cooldownPeriod() {
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

        if (!isFormed())
            return;

        if (isActive()) {
            textList.add(Component.translatable("lightning.start_core.lightning_controller.energy")
                .append(Component.literal("%s EU/t".formatted(FormattingUtil.formatNumbers(euT)))
                    .withStyle(ChatFormatting.GREEN)));
        } else {
            textList.add(Component.translatable("lightning.start_core.lightning_controller.no_energy"));
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

        @Override
        public StarTLightningRodMachine getMachine(){return (StarTLightningRodMachine) super.getMachine();}

        public void produceEnergy(){
            EnergyContainerList energyContainer = getMachine().energyContainer;
            getMachine().euT = Math.max(0, (int) (getMachine().unstableEU * 0.81));

            if (energyContainer == null || getMachine().euT <= 0)
                return;

            long resultEnergy = energyContainer.getEnergyStored() + getMachine().euT;
            System.out.println("ResultEnergy: " + resultEnergy);
            if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
                energyContainer.changeEnergy(getMachine().euT);
                getMachine().unstableEU = Math.max(0, getMachine().unstableEU - getMachine().euT);
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

            if (machine.timeSinceLastStorm < STORM_COOLDOWN) {
                machine.timeSinceLastStorm += 1;

            } else if (machine.timeSinceLastStorm == STORM_COOLDOWN) {
                if (machine.getWeather().equals("lightning.start_core.lightning_controller.weather_thunder")){
                    if (machine.strikesThisStorm < STRIKES_PER_STORM) {
                        machine.LightningStrike();

                    } else if (machine.strikesThisStorm == STRIKES_PER_STORM) {
                        machine.timeSinceLastStorm = 0;
                        machine.strikesThisStorm = 0;


                    }
                }

                isActive = machine.euT > 0;
                System.out.println("EuT: " + machine.euT);
                setStatus(isActive ? Status.WORKING : Status.IDLE);
                produceEnergy();
            }


            if (machine.unstableEU > 0) {
                machine.unstableEU -= Math.max(1L,
                    Math.round(machine.unstableEU * DECAY_PER_TICK));
            }

        }
    }
}

