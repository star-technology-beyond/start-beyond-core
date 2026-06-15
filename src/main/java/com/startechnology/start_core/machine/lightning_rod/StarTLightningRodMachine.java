package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;


import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarTLightningRodMachine extends WorkableElectricMultiblockMachine {

    private static final int STRIKES_PER_STORM = 5;
    private static final int STORM_COOLDOWN = 24000;
    private static final long MAX_UNSTABLE_EU = 1000000;
    private static final double DECAY_PER_TICK = 0.0025;

    @Getter
    private final int tier;
    @Getter
    private int euT = 0;

    @Persisted
    private long unstableEU = 0;

    @Persisted
    private long timeSinceLastStorm = 23800;

    @Getter
    @Setter
    private int strikesThisStorm = 0;

    public StarTLightningRodMachine(IMachineBlockEntity holder, int tier) {
        super(holder);

        this.tier = tier;

    }

    private void LightningStrike() {
        long generatedUnstableEU = 2048;


        unstableEU = Math.min(
            unstableEU + generatedUnstableEU,
            MAX_UNSTABLE_EU
        );

        strikesThisStorm += 1;
        System.out.println("KABOOM!");

    }

    private String getWeather() {
        System.out.println("Weatherman!");
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
            textList.add(Component.translatable("lightning.start_core.lightning_controller.energy"));
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
                System.out.println("Cooldown ticking");

            } else if (machine.timeSinceLastStorm == STORM_COOLDOWN) {
                System.out.println("Cooldown finished");
                if (machine.getWeather().equals("lightning.start_core.lightning_controller.weather_thunder")){
                    System.out.println("Thunder!");
                    if (machine.strikesThisStorm < STRIKES_PER_STORM) {
                        System.out.println("kaboom?");
                        machine.LightningStrike();

                    } else if (machine.strikesThisStorm == STRIKES_PER_STORM) {
                        System.out.println("No more kaboom :(");
                        machine.timeSinceLastStorm = 0;
                        if (!machine.getWeather().equals("lightning.start_core.lightning_controller.weather_thunder")){
                            machine.strikesThisStorm = 0;
                        }

                    }
                }
            }


            if (machine.unstableEU > 0) {
                machine.unstableEU -= Math.max(1L,
                    Math.round(machine.unstableEU * DECAY_PER_TICK));
                System.out.println("Decay Decay Decay");
            }

        }
    }
}

