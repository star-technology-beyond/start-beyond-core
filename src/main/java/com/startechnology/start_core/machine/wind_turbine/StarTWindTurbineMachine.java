package com.startechnology.start_core.machine.wind_turbine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarTWindTurbineMachine extends WorkableElectricMultiblockMachine {

    public static final int FLUID_PER_CYCLE = 4;
    private final int tier;
    private int euT = 0;
    private boolean usingLubricant = false;
    private boolean usingSeedOil = false;
    private boolean isCrowded = false;
    private double weatherMultiplier = 1.0;

    private final GTRecipe lubricantRecipe;
    private final GTRecipe seedOilRecipe;

    public StarTWindTurbineMachine(IMachineBlockEntity holder, int tier) {
        super(holder);

        this.tier = tier;

        this.lubricantRecipe = GTRecipeBuilder.ofRaw()
            .inputFluids(GTMaterials.Lubricant.getFluid(FLUID_PER_CYCLE))
            .buildRawRecipe();

        this.seedOilRecipe = GTRecipeBuilder.ofRaw()
            .inputFluids(GTMaterials.SeedOil.getFluid(FLUID_PER_CYCLE))
            .buildRawRecipe();

    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new StarTWindTurbineRecipeLogic(this);
    }

    public void doLogic() {
        weatherMultiplier = getWeatherMultiplier();
        isCrowded = hasNearbyWindTurbine();

        usingLubricant = RecipeHelper.matchRecipe(this, lubricantRecipe).isSuccess()
            && RecipeHelper.handleRecipeIO(this, lubricantRecipe, IO.IN, recipeLogic.getChanceCaches()).isSuccess();
        usingSeedOil = false;
        
        if (!usingLubricant) {
            usingSeedOil = RecipeHelper.matchRecipe(this, seedOilRecipe).isSuccess()
                && RecipeHelper.handleRecipeIO(this, seedOilRecipe, IO.IN, recipeLogic.getChanceCaches()).isSuccess();

            if (!usingSeedOil) {
                euT = 0;
                return;
            }
        }

        double lubricantMultiplier = usingLubricant ? 1.2 : 1.0;
        double crowdingMultiplier = isCrowded ? 0.5 : 1.0;

        euT = Math.max(1, (int) (getBaseEuT() * lubricantMultiplier * weatherMultiplier * crowdingMultiplier));
    }

    public int getBaseEuT() {
        return switch (tier) {
            case GTValues.LV -> 32;
            case GTValues.MV -> 128;
            case GTValues.HV -> 512;
            default -> (int) GTValues.V[tier];
        };
    }

    public int getCrowdingRadius() {
        return getCrowdingRadius(tier);
    }

    public static int getCrowdingRadius(int tier) {
        return switch (tier) {
            case GTValues.LV -> 7;
            case GTValues.MV -> 10;
            case GTValues.HV -> 13;
            default -> 7;
        };
    }

    private double getWeatherMultiplier() {
        var level = getLevel();

        if (level.isThundering()) return 2.0;
        if (level.isRaining()) return 1.5;

        return 1.0;
    }

    private boolean hasNearbyWindTurbine() {
        var level = getLevel();
        BlockPos center = getPos();
        int radius = getCrowdingRadius();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (pos.equals(center)) continue;

            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof IMachineBlockEntity machineBlockEntity
                && machineBlockEntity.getMetaMachine() instanceof StarTWindTurbineMachine) {
                return true;
            }
        }

        return false;
    }

    public boolean regressWhenWaiting() {
        return false;
    }

    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return false;
    }

    @Override
    public boolean isGenerator() {
        return true;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed()) return;

        if (isActive()) {
            textList.add(Component.literal("Generating: ")
                .append(Component.literal("%s EU/t".formatted(FormattingUtil.formatNumbers(euT)))
                    .withStyle(ChatFormatting.GREEN)));
        } else {
            textList.add(Component.literal("Waiting for Lubricant or Seed Oil").withStyle(ChatFormatting.YELLOW));
        }

        String fluidName = usingLubricant ? "Lubricant" : usingSeedOil ? "Seed Oil" : "None";
        textList.add(Component.literal("Fluid: ")
            .append(Component.literal(fluidName).withStyle(usingLubricant || usingSeedOil ? ChatFormatting.AQUA : ChatFormatting.GRAY)));
        textList.add(Component.literal("Dynamo Tier: ")
            .append(Component.literal(GTValues.VNF[getTier()]).withStyle(ChatFormatting.GOLD)));
        textList.add(Component.literal("Weather Boost: ")
            .append(Component.literal("%.0f%%".formatted(weatherMultiplier * 100)).withStyle(ChatFormatting.BLUE)));
        textList.add(Component.literal("Nearby Airspace: ")
            .append(Component.literal(isCrowded ? "Crowded" : "Clear").withStyle(isCrowded ? ChatFormatting.RED : ChatFormatting.GREEN)));
    }


    public static class StarTWindTurbineRecipeLogic extends RecipeLogic {
        private static final int UPDATE_INTERVAL = 75;

        public StarTWindTurbineRecipeLogic(StarTWindTurbineMachine machine) {
            super(machine);
        }

        @NotNull
        @Override
        public StarTWindTurbineMachine getMachine() {
            return (StarTWindTurbineMachine) super.getMachine();
        }

        private void produceEnergy() {
            EnergyContainerList energyContainer = getMachine().energyContainer;

            if (energyContainer == null || getMachine().euT <= 0) return;

            long resultEnergy = energyContainer.getEnergyStored() + getMachine().euT;

            if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
                energyContainer.changeEnergy(getMachine().euT);
            }
        }

        @Override
        public void serverTick() {
            var machine = getMachine();

            if (!machine.isFormed || !isWorkingEnabled()) {
                machine.euT = 0;
                machine.usingLubricant = false;
                machine.usingSeedOil = false;
                setStatus(Status.IDLE);
                isActive = false;
                return;
            }

            if (progress == 0) {
                machine.doLogic();
            }

            isActive = machine.euT > 0;
            setStatus(isActive ? Status.WORKING : Status.IDLE);

            progress = (progress + 1) % UPDATE_INTERVAL;

            produceEnergy();
        }

        @Override
        public int getMaxProgress() {
            return UPDATE_INTERVAL;
        }

    }

}
