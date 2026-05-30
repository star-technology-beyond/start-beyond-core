package com.startechnology.start_core.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class StarTItemModelDatagenProvider {
    // datagen all the multitool mode models
    // this saves us from manually doing each one
    // and automatically tells us if we're missing textures
    private static void registerMultitoolModeModels(ItemModelProvider provider) {
        for (GTToolType type : GTToolType.getTypes().values()) {
            provider.getBuilder("multitool_" + type.name)
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", "start_core:item/tools/" + type.name)
                .texture("layer1", "start_core:item/tools/active_tool");
        }
        
        // empty multitool model
        provider.getBuilder("multitool_empty")
            .parent(new ModelFile.UncheckedModelFile("item/handheld"))
            .texture("layer0", "start_core:item/tools/multitool_empty");
    }

    public static void init(ItemModelProvider provider) {
        registerMultitoolModeModels(provider);
    } 
}
