package com.startechnology.start_core;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.simibubi.create.content.contraptions.bearing.BearingVisual;
import com.startechnology.start_core.machine.komaru.client.KomaruRenderer;
import com.startechnology.start_core.machine.komaru.client.KomaruRendererManager;
import com.startechnology.start_core.machine.wind_turbine.StarTWindTurbineBearingBlockEntity;
import com.startechnology.start_core.machine.wind_turbine.StarTWindTurbineBlocks;

import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class StarTCoreClient {

    public static void init() {
        // always register, so we don't have to change the machine definition
        DynamicRenderManager.register(StarTCore.resourceLocation("komaru_renderer"), KomaruRenderer.TYPE);

        if (StarTConfig.INSTANCE.client.komaruRenderer) {
            KomaruRendererManager.init();
        }        
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {

        // this sets a visualiser for a block entity with flywheel
        VisualizerRegistry.setVisualizer(
            StarTWindTurbineBlocks.WIND_TURBINE_BEARING_BE.get(),
            new BlockEntityVisualizer<StarTWindTurbineBearingBlockEntity>() {
                @Override
                public BlockEntityVisual<? super StarTWindTurbineBearingBlockEntity> createVisual(
                    VisualizationContext context, StarTWindTurbineBearingBlockEntity blockEntity, float partialTick) {
                    // we render the bearing visual for the wind turbine bearing
                    // this is the spinning head
                    return new BearingVisual(context, blockEntity, partialTick);
                }
                
                @Override
                public boolean skipVanillaRender(StarTWindTurbineBearingBlockEntity blockEntity) {
                    // flywheel does the rendering for us
                    return true;
                }
            }
        );
    }

}
