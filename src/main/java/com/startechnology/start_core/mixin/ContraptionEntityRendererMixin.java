package com.startechnology.start_core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import com.startechnology.start_core.machine.wind_turbine.client.StarTWindTurbineCoilRenderer;

import net.minecraft.client.renderer.MultiBufferSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContraptionEntityRenderer.class, remap = false)
public class ContraptionEntityRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void start_core$renderWindTurbineCoils(AbstractContraptionEntity entity, float yaw, float partialTicks,
                                                   PoseStack poseStack, MultiBufferSource buffers, int overlay,
                                                   CallbackInfo ci) {
        StarTWindTurbineCoilRenderer.render(entity, partialTicks, poseStack, buffers);
    }
}
