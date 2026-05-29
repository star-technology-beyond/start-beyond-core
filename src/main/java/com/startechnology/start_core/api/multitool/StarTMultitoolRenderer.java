package com.startechnology.start_core.api.multitool;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.startechnology.start_core.item.StarTItems;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StarTMultitoolRenderer {

    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        // register empty tool
        event.register(new ModelResourceLocation(
                new ResourceLocation("start_core", "multitool_mode_empty"), "inventory"));

        // register each tool type
        for (GTToolType type : GTToolType.getTypes().values()) {
            event.register(new ModelResourceLocation(
                    new ResourceLocation("start_core", "multitool_mode_" + type.name), "inventory"));
        }
    }

    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        // get the item since we want to bake a different model depending on the active type
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(StarTItems.GREGTECH_MULTITOOL.get());
        if (itemKey == null) {
            return;
        }

        // all the models are undere the inventory for the item
        ModelResourceLocation modelKey = new ModelResourceLocation(itemKey, "inventory");

        //// get the empty model as a fallback if empty/issue
        BakedModel emptyModel = event.getModels().get(
                new ModelResourceLocation(new ResourceLocation("start_core", "multitool_mode_empty"), "inventory"));

        // fall back to whatever is baked for the item itself if the empty model is invalid
        if (emptyModel == null) emptyModel = event.getModels().get(modelKey);

        // if somehow we still cant load any model then just return
        if (emptyModel == null) {
            return;
        }

        // Collect the baked models for each mode
        Map<String, BakedModel> perTypeModels = new HashMap<>();

        for (GTToolType type : GTToolType.getTypes().values()) {
            ModelResourceLocation modeKey = new ModelResourceLocation(
                    new ResourceLocation("start_core", "multitool_mode_" + type.name), "inventory");
            BakedModel baked = event.getModels().get(modeKey);
            if (baked != null) {
                perTypeModels.put(type.name, baked);
            }
        }

        // we need a final version to pass into the function below
        final BakedModel finalEmptyModel = emptyModel;

        // this puts a function essentially that resolves the model
        // for an item at runtime/dynamically
        //
        // we can basically get the active mode and then the corresponding model
        // for that mode here
        event.getModels().put(modelKey, new BakedModelWrapper<>(emptyModel) {
            @Override
            public ItemOverrides getOverrides() {
                return new ItemOverrides() {
                    @Override
                    public BakedModel resolve(BakedModel model, ItemStack stack,
                                              @Nullable ClientLevel level,
                                              @Nullable LivingEntity entity, int seed) {
                        StarTMultitoolMode active = StarTMultitoolMode.getActive(stack);
                        if (active == null) return finalEmptyModel;
                        return perTypeModels.getOrDefault(active.toolType().name, finalEmptyModel);
                    }
                };
            }
        });
    }
}