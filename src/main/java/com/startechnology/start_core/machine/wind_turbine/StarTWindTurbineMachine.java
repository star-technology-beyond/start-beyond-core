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
import com.simibubi.create.content.contraptions.bearing.BearingBlock;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarTWindTurbineMachine extends WorkableElectricMultiblockMachine {

    public static final int FLUID_PER_CYCLE = 4;
    @Getter
    private final int tier;
    @Getter
    private int euT = 0;

    @Getter
    private boolean usingLubricant = false;

    @Getter
    private boolean usingSeedOil = false;

    @Getter
    private boolean isCrowded = false;

    // Store bearing that we use for the spinny of doom
    private StarTWindTurbineBearingBlockEntity cachedBearing = null;

    // We dont want to deform the multiblock if its just the contraption
    // being assembled
    @Getter
    @Setter
    private boolean contraptionAssembled = false;

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

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        StarTWindTurbineManager.addTurbine(this);
        findAndCacheBearing();
        forceBearingDirection();
    }

    @Override
    public void onStructureInvalid() {
        if (contraptionAssembled && cachedBearing != null && cachedBearing.isAssembling()) {
            return;
        }
        
        this.contraptionAssembled = false;
        stopBearing();
        cachedBearing = null;
        StarTWindTurbineManager.removeTurbine(this);
        super.onStructureInvalid();
    }

    @Override
    public void onUnload() {
        stopBearing();
        cachedBearing = null;
        StarTWindTurbineManager.removeTurbine(this);
        super.onUnload();
    }

    public void doLogic() {
        weatherMultiplier = getWeatherMultiplier();
        isCrowded = StarTWindTurbineManager.hasNearbyTurbine(this, getCrowdingRadius());

        usingLubricant = RecipeHelper.matchRecipe(this, lubricantRecipe).isSuccess()
                && RecipeHelper.handleRecipeIO(this, lubricantRecipe, IO.IN, recipeLogic.getChanceCaches()).isSuccess();
        usingSeedOil = false;

        if (!usingLubricant) {
            usingSeedOil = RecipeHelper.matchRecipe(this, seedOilRecipe).isSuccess()
                    && RecipeHelper.handleRecipeIO(this, seedOilRecipe, IO.IN, recipeLogic.getChanceCaches())
                            .isSuccess();

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

        if (level.isThundering())
            return 2.0;
        if (level.isRaining())
            return 1.5;

        return 1.0;
    }

    public double getCurrentWeatherMultiplier() {
        return weatherMultiplier;
    }

    private String getWeatherType() {
        var level = getLevel();

        if (level.isThundering())
            return "Thunder";
        if (level.isRaining())
            return "Rain";

        return "Clear";
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

        if (!isFormed())
            return;

        if (isActive()) {
            textList.add(Component.literal("Generating: ")
                    .append(Component.literal("%s EU/t".formatted(FormattingUtil.formatNumbers(euT)))
                            .withStyle(ChatFormatting.GREEN)));
        } else {
            textList.add(Component.literal("Waiting for Lubricant or Seed Oil").withStyle(ChatFormatting.YELLOW));
        }

        String fluidName = usingLubricant ? "Lubricant" : usingSeedOil ? "Seed Oil" : "None";
        textList.add(Component.literal("Fluid: ")
                .append(Component.literal(fluidName)
                        .withStyle(usingLubricant || usingSeedOil ? ChatFormatting.AQUA : ChatFormatting.GRAY)));
        textList.add(Component.literal("Dynamo Tier: ")
                .append(Component.literal(GTValues.VNF[getTier()]).withStyle(ChatFormatting.GOLD)));
        textList.add(Component.literal("Weather Boost: ")
                .append(Component.literal("%.0f%%".formatted(weatherMultiplier * 100)).withStyle(ChatFormatting.BLUE))
                .append(Component.literal(" ("))
                .append(Component.literal(getWeatherType()).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(")")));
        textList.add(Component.literal("Nearby Airspace: ")
                .append(Component.literal(isCrowded ? "Crowded" : "Clear")
                        .withStyle(isCrowded ? ChatFormatting.RED : ChatFormatting.GREEN)));
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

            if (energyContainer == null || getMachine().euT <= 0)
                return;

            long resultEnergy = energyContainer.getEnergyStored() + getMachine().euT;

            if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
                energyContainer.changeEnergy(getMachine().euT);
            }
        }

        @Override
        public void serverTick() {
            var machine = getMachine();

            if (!machine.isFormed || !isWorkingEnabled()) {
                StarTWindTurbineManager.removeTurbine(machine);
                machine.euT = 0;
                machine.usingLubricant = false;
                machine.usingSeedOil = false;
                machine.isCrowded = false;
                if (machine.cachedBearing != null) {
                    machine.cachedBearing.stopAssembly();
                }
                setStatus(Status.IDLE);
                isActive = false;
                return;
            }

            if (machine.cachedBearing != null && !machine.cachedBearing.isRunning() && machine.contraptionAssembled) {
                machine.setContraptionAssembled(false);
            }

            if (progress == 0) {
                machine.doLogic();
                
                if (machine.euT > 0 && machine.cachedBearing != null) {
                    machine.setContraptionAssembled(true);
                    machine.updateBearingSpeed();
                    if (!machine.cachedBearing.isRunning()) {
                        machine.cachedBearing.startAssembly();
                    }
                } else if (machine.euT <= 0 && machine.cachedBearing != null) {
                    machine.setContraptionAssembled(false);
                    machine.cachedBearing.stopAssembly();
                }
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

    // set target RPM to scale based on eu/t and tier.
    public float getTargetRPM() {
        if (euT <= 0)
            return 0f;
        float baseRPM = switch (tier) {
            case GTValues.LV -> 8f;
            case GTValues.MV -> 14f;
            case GTValues.HV -> 20f;
            default -> 8f;
        };
        return (float) (baseRPM * Math.sqrt((double) euT / getBaseEuT()));
    }

    // try find the bearing based on the traceability predicate
    // like how we do komaru basic and advanced modules
    private void findAndCacheBearing() {
        cachedBearing = null;
        if (!isFormed() || getLevel() == null)
            return;

        BlockPos bearingPos = getMultiblockState().getMatchContext()
                .getOrDefault(StarTWindTurbinePredicates.BEARING_KEY, null);

        if (bearingPos == null)
            return;

        if (getLevel().getBlockEntity(bearingPos) instanceof StarTWindTurbineBearingBlockEntity bearing) {
            cachedBearing = bearing;
        }
    }

    // bearing should face in the direction we want it to >:)
    // else bad things will happen.
    private void forceBearingDirection() {
        if (cachedBearing == null || getLevel() == null) return;
        
        BlockPos bearingPos = cachedBearing.getBlockPos();
        BlockState bearingState = getLevel().getBlockState(bearingPos);
        
        Direction frontFace = getFrontFacing(); 
        
        if (bearingState.hasProperty(BearingBlock.FACING) && 
            bearingState.getValue(BearingBlock.FACING) != frontFace) {
            getLevel().setBlock(
                bearingPos,
                bearingState.setValue(BearingBlock.FACING, frontFace),
                3
            );
            findAndCacheBearing();
        }
    }

    // updates the rpm spinny speed of the bearing
    // to the target rpm of the machine
    public void updateBearingSpeed() {
        if (cachedBearing == null)
            return;
        cachedBearing.setTargetSpeed(getTargetRPM());
    }

    // stops the bearing
    private void stopBearing() {
        if (cachedBearing == null)
            return;
        cachedBearing.stopAssembly();
    }
}
