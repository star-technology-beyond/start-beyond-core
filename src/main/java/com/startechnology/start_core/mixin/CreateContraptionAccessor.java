package com.startechnology.start_core.mixin;
import com.simibubi.create.content.contraptions.Contraption;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;


@Mixin(value = Contraption.class, remap = false)
public interface CreateContraptionAccessor {
    @Invoker("capture")
    Pair<StructureTemplate.StructureBlockInfo, BlockEntity> start_core$capture(Level world, BlockPos pos);
}
