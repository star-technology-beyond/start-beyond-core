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

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarTWindTurbineMachine extends WorkableElectricMultiblockMachine {

    private static final int LV_FLUID_PER_CYCLE = 4;
    private static final int MV_FLUID_PER_CYCLE = 12;
    private static final int HV_FLUID_PER_CYCLE = 36;
    @Getter
    private final int tier;
    @Getter
    private int euT = 0;

    @Getter
    private boolean usingLubricant = false;

    @Getter
    private boolean usingSeedOil = false;

    @Getter
    private boolean usingFishOil = false;

    @Getter
    private boolean isCrowded = false;

    // Store bearing that we use for the spinny of doom
    private StarTWindTurbineBearingBlockEntity cachedBearing = null;

    // We dont want to deform the multiblock if its just the contraption
    // being assembled
    @Getter
    @Setter
    private boolean contraptionAssembled = false;

    private final GTRecipe lubricantRecipe;
    private final GTRecipe seedOilRecipe;
    private final GTRecipe fishOilRecipe;

    public StarTWindTurbineMachine(IMachineBlockEntity holder, int tier) {
        super(holder);

        this.tier = tier;

        int fluidPerCycle = getFluidPerCycle(tier);

        this.lubricantRecipe = GTRecipeBuilder.ofRaw()
                .inputFluids(GTMaterials.Lubricant.getFluid(fluidPerCycle))
                .buildRawRecipe();

        this.seedOilRecipe = GTRecipeBuilder.ofRaw()
                .inputFluids(GTMaterials.SeedOil.getFluid(fluidPerCycle))
                .buildRawRecipe();

        this.fishOilRecipe = GTRecipeBuilder.ofRaw()
                .inputFluids(GTMaterials.FishOil.getFluid(fluidPerCycle))
                .buildRawRecipe();

    }

    public static int getFluidPerCycle(int tier) {
        return switch (tier) {
            case GTValues.MV -> MV_FLUID_PER_CYCLE;
            case GTValues.HV -> HV_FLUID_PER_CYCLE;
            default -> LV_FLUID_PER_CYCLE;
        };
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new StarTWindTurbineRecipeLogic(this);
    }

    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                              BlockHitResult hitResult) {
        playerIn.swing(hand);
        return InteractionResult.CONSUME;
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
    public void onLoad() {
        super.onLoad();
        if (isFormed()) {
            findAndCacheBearing();
            forceBearingDirection();
        }
    }

    @Override
    public void onUnload() {
        if (cachedBearing != null && getLevel() != null && !getLevel().isClientSide()) {
            if (getLevel().isLoaded(getPos())) {
                cachedBearing.stopAssembly();
            }
        }
        cachedBearing = null;
        StarTWindTurbineManager.removeTurbine(this);
        super.onUnload();
    }

    public void doLogic() {
        isCrowded = StarTWindTurbineManager.hasNearbyTurbine(this, getCrowdingRadius());
        usingLubricant = RecipeHelper.matchRecipe(this, lubricantRecipe).isSuccess()
                && RecipeHelper.handleRecipeIO(this, lubricantRecipe, IO.IN, recipeLogic.getChanceCaches()).isSuccess();
        usingSeedOil = false;
        usingFishOil = false;

        if (!usingLubricant) {
            usingSeedOil = RecipeHelper.matchRecipe(this, seedOilRecipe).isSuccess()
                    && RecipeHelper.handleRecipeIO(this, seedOilRecipe, IO.IN, recipeLogic.getChanceCaches()).isSuccess();

            if (!usingSeedOil) {
                usingFishOil = RecipeHelper.matchRecipe(this, fishOilRecipe).isSuccess()
                        && RecipeHelper.handleRecipeIO(this, fishOilRecipe, IO.IN, recipeLogic.getChanceCaches()).isSuccess();

                if (!usingFishOil) {
                    euT = 0;
                    return;
                }
            }
        }

        double crowdingMultiplier = isCrowded ? 0.5 : 1.0;
        double fluidMultiplier = usingLubricant ? 1.0 : 0.85;
        euT = Math.max(1, (int) (GTValues.V[tier] * getOutputAmps() * fluidMultiplier * crowdingMultiplier));

    }

    public int getBaseEuT() {
        return switch (tier) {
            case GTValues.LV -> 32;
            case GTValues.MV -> 128;
            case GTValues.HV -> 512;
            default -> (int) GTValues.V[tier];
        };
    }

    private double getOutputCurvePhase() {
        long cycle = getLevel().getGameTime() / 75L;
        double period = 60.0 * Math.PI;
        long positionHash = getPos().asLong();

        double offset = Math.floorMod(positionHash, 10_000L) / 10_000 * period;

        return (cycle + offset) % period;
    }

    private double getOutputAmps() {
        double cycles = getOutputCurvePhase();

        double amps = 2.0 + Math.pow(Math.sin(cycles / 12.0), 2) + Math.abs(Math.cos(2.0 * cycles / 5.0));

        amps *= switch (tier) {
            case GTValues.MV -> 0.9;
            case GTValues.HV -> 0.8;
            default -> 1.0;
        };

        if (getLevel().isThundering()) {
            return (amps + 4.0) / 2.0;
        }

        if (getLevel().isRaining()) {
            return (amps + 2.0) / 1.5;
        }

        return amps;
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

    private Component getWeatherType() {
        var level = getLevel();

        if (level.isThundering())
            return Component.translatable("wind.start_core.wind_controller.weather.thunder");
        if (level.isRaining())
            return Component.translatable("wind.start_core.wind_controller.weather.rain");

        return Component.translatable("wind.start_core.wind_controller.weather.clear");
    }

    private Component getFluidName() {
        if (usingLubricant)
            return Component.translatable("wind.start_core.wind_controller.lubricant");
        if (usingSeedOil)
            return Component.translatable("wind.start_core.wind_controller.seed_oil");
        if (usingFishOil)
            return Component.translatable("wind.start_core.wind_controller.fish_oil");

        return Component.translatable("wind.start_core.wind_controller.none");
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
            textList.add(Component.translatable("wind.start_core.wind_controller.generating",
                    FormattingUtil.formatNumbers(euT)));
        } else {
            textList.add(Component.translatable("wind.start_core.wind_controller.waiting_for_fluid"));
        }

        textList.add(Component.translatable("wind.start_core.wind_controller.fluid", getFluidName()));
        textList.add(Component.translatable("wind.start_core.wind_controller.fluid_usage",
                FormattingUtil.formatNumbers(getFluidPerCycle(tier) * 20L)));
        textList.add(Component.translatable("wind.start_core.wind_controller.dynamo_tier",
                GTValues.VNF[getTier()]));
        textList.add(Component.translatable("wind.start_core.wind_controller.weather",
                getWeatherType()));
        textList.add(Component.translatable("wind.start_core.wind_controller.nearby_airspace",
                Component.translatable(isCrowded ? "wind.start_core.wind_controller.airspace.crowded" :
                        "wind.start_core.wind_controller.airspace.clear")));
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
                machine.usingFishOil = false;
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

        return (float) (baseRPM * (double) euT / getBaseEuT());
    }

    // try find the bearing based on the traceability predicate
    // like how we do komaru basic and advanced modules
    //
    // we also pass the actual blade position to it
    private void findAndCacheBearing() {
        cachedBearing = null;
        if (!isFormed() || getLevel() == null)
            return;

        BlockPos bearingPos = getMultiblockState().getMatchContext()
                .getOrDefault(StarTWindTurbinePredicates.BEARING_KEY, null);

        List<BlockPos> bladePositions = getMultiblockState().getMatchContext()
                .getOrDefault(StarTWindTurbinePredicates.BLADE_POSITIONS_KEY, new ArrayList<>());

        if (bearingPos == null)
            return;

        if (getLevel().getBlockEntity(bearingPos) instanceof StarTWindTurbineBearingBlockEntity bearing) {
            cachedBearing = bearing;
            cachedBearing.setBladePositions(bladePositions);
        }
    }

    // bearing should face in the direction we want it to >:)
    // else bad things will happen.
    private void forceBearingDirection() {
        if (cachedBearing == null || getLevel() == null) return;

        BlockPos bearingPos = cachedBearing.getBlockPos();
        BlockState bearingState = getLevel().getBlockState(bearingPos);

        Direction targetFace = switch (tier) {
            case GTValues.MV, GTValues.HV -> Direction.UP;
            default -> getFrontFacing();
        };

        if (bearingState.hasProperty(BearingBlock.FACING) &&
            bearingState.getValue(BearingBlock.FACING) != targetFace) {
            getLevel().setBlock(
                bearingPos,
                bearingState.setValue(BearingBlock.FACING, targetFace),
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

    public boolean isContraptionAssemblingOrRunning() {
        return cachedBearing != null && (cachedBearing.isAssembling() || cachedBearing.isRunning());
    }

    // stops the bearing
    private void stopBearing() {
        if (cachedBearing == null)
            return;
        cachedBearing.stopAssembly();
    }
}
