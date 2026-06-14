package com.startechnology.start_core.machine.lightning_rod;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;
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

    private long unstableEU = 0;
    private long timeSinceLastStorm = 24000;

    @Getter
    @Setter
    private int strikesThisStorm = 0;

    public StarTLightningRodMachine(IMachineBlockEntity holder, int tier) {
        super(holder);

        this.tier = tier;

    }

    public void LightingStrike() {
        long generatedUnstableEU = 1000;

        int lightingChance = ThreadLocalRandom.current().nextInt(6000, 6768);

        if (lightingChance == 6767) {
            unstableEU = Math.min(
                unstableEU + generatedUnstableEU,
                MAX_UNSTABLE_EU
            );

            strikesThisStorm += 1;
        }
    }

    private String getWeather() {
        if (getLevel().isThundering())
            return "Thundering";
        if(getLevel().isRaining())
            return "Raining";

        return  "Clear";
    }

    public String cooldownPeriod() {
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
            textList.add(Component.literal("lightning.start_core.lighting_controller.energy"));
        } else {
            textList.add(Component.literal("lightning.start_core.lighting_controller.no_energy"));
        }

        textList.add(Component.literal("Weather: ")
            .append(Component.literal(getWeather())
                .withStyle(ChatFormatting.GRAY)));
        textList.add(Component.literal("Unstable EU: ")
            .append(Component.literal("" + unstableEU)
                .withStyle(ChatFormatting.BLUE)));
        textList.add(Component.literal("Strikes left: ")
            .append(Component.literal("" + (STRIKES_PER_STORM - strikesThisStorm))
                .withStyle(ChatFormatting.GOLD)));
        textList.add(Component.literal("Cooldown Remaining: ")
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
                machine.unstableEU -= Math.max(1L,
                    Math.round(machine.unstableEU * DECAY_PER_TICK));
            }

        }
    }
}

