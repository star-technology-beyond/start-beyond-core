package com.startechnology.start_core.machine.wind_turbine.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class StarTWindTurbineCoilRenderer {

    private static final ResourceLocation CUPRONICKEL_COIL = GTCEu.id("cupronickel_coil_block");
    private static final ResourceLocation KANTHAL_COIL = GTCEu.id("kanthal_coil_block");
    private static final ResourceLocation CUPRONICKEL_BLOOM = GTCEu.id(
            "block/casings/coils/machine_coil_cupronickel_bloom");
    private static final ResourceLocation KANTHAL_BLOOM = GTCEu.id(
            "block/casings/coils/machine_coil_kanthal_bloom");

    private StarTWindTurbineCoilRenderer() {
    }

    public static void render(AbstractContraptionEntity entity, float partialTicks, PoseStack poseStack,
                              MultiBufferSource buffers) {
        var contraption = entity.getContraption();
        if (contraption == null)
            return;

        poseStack.pushPose();
        entity.applyLocalTransforms(poseStack, partialTicks);

        VertexConsumer buffer = buffers.getBuffer(RenderType.cutoutMipped());
        for (var entry : contraption.getBlocks().entrySet()) {
            ResourceLocation bloomTexture = getBloomTexture(entry.getValue().state());
            if (bloomTexture == null)
                continue;

            poseStack.pushPose();
            poseStack.translate(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
            renderBloomCube(poseStack, buffer, getSprite(bloomTexture));
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static ResourceLocation getBloomTexture(BlockState state) {
        if (!state.hasProperty(GTBlockStateProperties.ACTIVE)
                || !state.getValue(GTBlockStateProperties.ACTIVE))
            return null;

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (CUPRONICKEL_COIL.equals(blockId))
            return CUPRONICKEL_BLOOM;
        if (KANTHAL_COIL.equals(blockId))
            return KANTHAL_BLOOM;
        return null;
    }

    private static TextureAtlasSprite getSprite(ResourceLocation texture) {
        return Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(texture);
    }

    private static void renderBloomCube(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite) {
        float min = -0.002f;
        float max = 1.002f;

        face(poseStack, buffer, sprite, 0, 0, -1,
                min, max, min, max, max, min, max, min, min, min, min, min);
        face(poseStack, buffer, sprite, 0, 0, 1,
                min, min, max, max, min, max, max, max, max, min, max, max);
        face(poseStack, buffer, sprite, -1, 0, 0,
                min, min, min, min, min, max, min, max, max, min, max, min);
        face(poseStack, buffer, sprite, 1, 0, 0,
                max, min, max, max, min, min, max, max, min, max, max, max);
        face(poseStack, buffer, sprite, 0, -1, 0,
                min, min, max, min, min, min, max, min, min, max, min, max);
        face(poseStack, buffer, sprite, 0, 1, 0,
                min, max, min, min, max, max, max, max, max, max, max, min);
    }

    private static void face(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite,
                             float normalX, float normalY, float normalZ,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4) {
        vertex(poseStack, buffer, sprite, x1, y1, z1, sprite.getU0(), sprite.getV0(), normalX, normalY, normalZ);
        vertex(poseStack, buffer, sprite, x2, y2, z2, sprite.getU1(), sprite.getV0(), normalX, normalY, normalZ);
        vertex(poseStack, buffer, sprite, x3, y3, z3, sprite.getU1(), sprite.getV1(), normalX, normalY, normalZ);
        vertex(poseStack, buffer, sprite, x4, y4, z4, sprite.getU0(), sprite.getV1(), normalX, normalY, normalZ);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite,
                               float x, float y, float z, float u, float v,
                               float normalX, float normalY, float normalZ) {
        var pose = poseStack.last();
        buffer.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }
}
