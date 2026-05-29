package com.startechnology.start_core.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(value = PipeBlock.class, priority = 900)
public abstract class PipeBlockMultitoolShapeMixin {

    @Inject(method = "getShape", at = @At("RETURN"), cancellable = true)
    private void onGetShapeReturn(BlockState pState, BlockGetter pLevel, BlockPos pPos,
                                              CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        // in the original method here
        // this is where the "hitbox" is expanded
        // when you say hover over a pipe with a wrench
        //
        // we want this to happen for the multitool too!
        if (!(context instanceof EntityCollisionContext entityCtx)) return;
        if (!(entityCtx.getEntity() instanceof Player player)) return;
        
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof StarTMultitoolItem)) return;
        
        // already returning full block shape, nothing to do
        if (cir.getReturnValue() == Shapes.block()) return;
        
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (!(tile instanceof PipeBlockEntity<?, ?> pipeTile)) return;
        
        // expand for multitool type if its a pipe tune tool
        GTToolType pipeTuneTool = pipeTile.getPipeTuneTool();
        Set<GTToolType> multitoolTypes = ((StarTMultitoolItem) held.getItem()).getToolClasses(held);
        
        if (multitoolTypes.contains(pipeTuneTool)) {
            cir.setReturnValue(Shapes.block());
        }
    }
}