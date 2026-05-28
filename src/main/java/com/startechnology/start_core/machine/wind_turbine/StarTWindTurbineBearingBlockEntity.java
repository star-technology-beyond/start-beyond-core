package com.startechnology.start_core.machine.wind_turbine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.common.base.Supplier;
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

public class StarTWindTurbineBearingBlockEntity extends WindmillBearingBlockEntity {

    private float targetSpeed = 0f;

    @Getter
    private boolean isAssembling = false;

    // List of allowed blocks to be grabbed as part of the
    // actual turbine blade
    private static final List<Supplier<Block>> TURBINE_BLADE_BLOCKS = new ArrayList<>();


    public StarTWindTurbineBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setTargetSpeed(float rpm) {
        this.targetSpeed = rpm;
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

    public static void registerBladeBlock(Supplier<Block> block) {
        TURBINE_BLADE_BLOCKS.add(block);
    }

    private static boolean isBladesBlock(Block block) {
        for (Supplier<Block> supplier : TURBINE_BLADE_BLOCKS) {
            if (supplier.get() == block) return true;
        }
        return false;
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

    @Override
    public void assemble() {
        if (!(level.getBlockState(worldPosition).getBlock() instanceof BearingBlock))
            return;

        Direction direction = getBlockState().getValue(BearingBlock.FACING);

        // grab all the turbine blade blocks we can visit
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        BlockPos start = worldPosition.relative(direction);
        queue.add(start);
        visited.add(start);

        List<BlockPos> bladePositions = new ArrayList<>();

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState state = level.getBlockState(current);
            
            if (!isBladesBlock(state.getBlock())) continue;
            
            bladePositions.add(current);

            for (Direction d : Direction.values()) {
                BlockPos neighbor = current.relative(d);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        if (bladePositions.isEmpty()) return;

        BearingContraption contraption = new BearingContraption(false, direction);
        
        // set the anchor manually to the block
        contraption.anchor = worldPosition.relative(direction);
        contraption.bounds = new AABB(BlockPos.ZERO);

        // add all the blocks in the turbine blades to the contraption
        for (BlockPos pos : bladePositions) {
            contraption.addBlock(level, pos, ((CreateContraptionAccessor)contraption).start_core$capture(level, pos));
        }

        // make the contraption from the blocks and begin updating it
        contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
        movedContraption = ControlledContraptionEntity.create(level, this, contraption);
        BlockPos anchor = worldPosition.relative(direction);
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        movedContraption.setRotationAxis(direction.getAxis());
        level.addFreshEntity(movedContraption);
        running = true;
        angle = 0;
        sendData();
        updateGeneratedRotation();
        isAssembling = false;
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
        super.disassemble();
    }

    @Override
    protected boolean isWindmill() {
        return false;
    }
}