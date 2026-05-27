package com.startechnology.start_core.mixin;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PipeBlock.class)
public class PipeBlockMultitoolShapeMixin {

    @Inject(method = "getShape", at = @At("RETURN"), cancellable = true)
    private void startCore$expandPipeShapeForSelectedMultitool(BlockState state, BlockGetter level, BlockPos pos,
                                                              CollisionContext context,
                                                              CallbackInfoReturnable<VoxelShape> cir) {
        if (!(context instanceof EntityCollisionContext entityContext) ||
                !(entityContext.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof StarTMultitoolItem) ||
                !(level.getBlockEntity(pos) instanceof PipeBlockEntity<?, ?> pipe)) {
            return;
        }
        if (StarTMultitoolMode.get(held).toolType() == pipe.getPipeTuneTool()) {
            cir.setReturnValue(Shapes.block());
        }
    }
}
