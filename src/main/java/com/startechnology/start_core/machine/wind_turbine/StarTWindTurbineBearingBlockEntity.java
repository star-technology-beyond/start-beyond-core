package com.startechnology.start_core.machine.wind_turbine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.base.Supplier;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.startechnology.start_core.mixin.CreateContraptionAccessor;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StarTWindTurbineBearingBlockEntity extends WindmillBearingBlockEntity {

    private float targetSpeed = 0f;

    @Getter
    private boolean isAssembling = false;

    // List of blade block positions for this wind turbine, we
    // get it from the machine which is from GT :)
    private List<BlockPos> cachedBladePositions = new ArrayList<>();


    public StarTWindTurbineBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setTargetSpeed(float rpm) {
        if (this.targetSpeed != rpm) {
            this.targetSpeed = rpm;
            updateGeneratedRotation();
        }
    }

    public float getTargetSpeed() {
        return targetSpeed;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!running) return 0;
        return targetSpeed * getAngleSpeedDirection();
    }

    public void startAssembly() {
        if (!running) {
            /* go forth, my create ! assemble ! */ 
            isAssembling = true;
            assembleNextTick = true;
        }
    }

    public void stopAssembly() {
        if (running) {
            disassemble();
        }
        isAssembling = false;
        targetSpeed = 0f;
        assembleNextTick = false;
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return true;
    }

    @Override
    public void tick() {
        // because we are not a windmill, we have to manually
        // force assemble after the superclass tick if we 
        // should assemble
        boolean shouldAssemble = assembleNextTick && !running;
        assembleNextTick = false;
        super.tick();
        if (!level.isClientSide && shouldAssemble) {
            assemble();
        }
    }

    public void setBladePositions(List<BlockPos> positions) {
        this.cachedBladePositions = positions;
    }

    private void setContraptionCoilsActive(boolean active) {
        if (movedContraption == null)
            return;

        var blocks = movedContraption.getContraption().getBlocks();
        for (var entry : new ArrayList<>(blocks.entrySet())) {
            var info = entry.getValue();
            BlockState state = info.state();
            if (!state.hasProperty(GTBlockStateProperties.ACTIVE)
                    || state.getValue(GTBlockStateProperties.ACTIVE) == active)
                continue;

            movedContraption.setBlock(
                    entry.getKey(),
                    new StructureTemplate.StructureBlockInfo(
                            info.pos(),
                            state.setValue(GTBlockStateProperties.ACTIVE, active),
                            info.nbt()));
        }
    }

    @Override
    public void assemble() {
        try {
            if (!(level.getBlockState(worldPosition).getBlock() instanceof BearingBlock))
                return;

            if (cachedBladePositions.isEmpty()) return;

            Direction direction = getBlockState().getValue(BearingBlock.FACING);
            BearingContraption contraption = new BearingContraption(false, direction);
            
            // set the anchor manually to the block
            contraption.anchor = worldPosition.relative(direction);
            contraption.bounds = new AABB(BlockPos.ZERO);

            // add all the blocks in the turbine blades to the contraption
            for (BlockPos pos : cachedBladePositions) {
                contraption.addBlock(level, pos,
                        ((CreateContraptionAccessor)contraption).start_core$capture(level, pos));
            }

             // make the contraption from the blocks and begin updating it
            contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
            movedContraption = ControlledContraptionEntity.create(level, this, contraption);
            BlockPos anchor = worldPosition.relative(direction);
            movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
            movedContraption.setRotationAxis(direction.getAxis());
            level.addFreshEntity(movedContraption);
            setContraptionCoilsActive(true);
            running = true;
            angle = 0;
            sendData();
            updateGeneratedRotation();
        } finally {
            isAssembling = false;
        }
    }


    @Override
    public void disassemble() {
        // IMPORTANT: 
        // move contraption to angle 0 before placing blocks back
        //
        // ELSE DA MULTIBLOCK CAN DISASSEMBLE IN A WEIRD PLACE AND 
        // EVERYONE WILL BE SAD :( !!!
        angle = 0;
        applyRotation();
        if (movedContraption != null) {
            movedContraption.getContraption().getBlocks().replaceAll((pos, info) -> {
                BlockState state = info.state();
                if (!state.hasProperty(GTBlockStateProperties.ACTIVE))
                    return info;

                return new StructureTemplate.StructureBlockInfo(
                        info.pos(),
                        state.setValue(GTBlockStateProperties.ACTIVE, false),
                        info.nbt());
            });
        }
        super.disassemble();
    }

    @Override
    protected boolean isWindmill() {
        return false;
    }
}
