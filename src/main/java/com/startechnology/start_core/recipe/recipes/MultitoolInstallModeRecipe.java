package com.startechnology.start_core.recipe.recipes;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import com.startechnology.start_core.item.multitool.StarTMultitoolMode;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class MultitoolInstallModeRecipe extends CustomRecipe {

    public static final RecipeSerializer<MultitoolInstallModeRecipe> SERIALIZER =
            new MultitoolInstallModeRecipe.Serializer();

    public MultitoolInstallModeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        // try check if the recipe matches a multitool install mode recipe
        // we get the multitool and tool.
        ItemStack multitool = ItemStack.EMPTY;
        ItemStack tool = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof StarTMultitoolItem) {
                 // two multitools
                if (!multitool.isEmpty()) return false;
                multitool = stack;
            } else if (stack.getItem() instanceof IGTTool) {
                 // two tools
                if (!tool.isEmpty()) return false;
                tool = stack;
            } else {
                // unknown item
                return false;
            }
        }

        if (multitool.isEmpty() || tool.isEmpty()) return false;

        // check the tool type isn't already installed
        GTToolType toolType = ((IGTTool) tool.getItem()).getToolType();
        return !StarTMultitoolMode.isInstalled(multitool, toolType);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, net.minecraft.core.RegistryAccess registryAccess) {
        // find the multitool and tool since we need to combine
        // them for the assemble result
        ItemStack multitool = ItemStack.EMPTY;
        ItemStack tool = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof StarTMultitoolItem) {
                multitool = stack;
            } else if (stack.getItem() instanceof IGTTool) {
                tool = stack;
            }
        }

        if (multitool.isEmpty() || tool.isEmpty()) return ItemStack.EMPTY;

        // get the tool type and material so we can add it to the multitool as a mode
        IGTTool gtTool = (IGTTool) tool.getItem();
        GTToolType toolType = gtTool.getToolType();
        var material = gtTool.getMaterial();

        // copy the existing multitool NBT so prior modes are preserved
        ItemStack result = multitool.copy();
        result.setCount(1);
        StarTMultitoolMode.install(result, toolType, material);
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        // we cant really express the ingredients statically
        return NonNullList.create();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        // nothing should remain after we craft, consume the multitool/gt tool essentially
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        return remaining;
    }

    public static class Serializer implements RecipeSerializer<MultitoolInstallModeRecipe> {
        @Override
        public MultitoolInstallModeRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new MultitoolInstallModeRecipe(id, CraftingBookCategory.MISC);
        }

        @Override
        public MultitoolInstallModeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new MultitoolInstallModeRecipe(id, CraftingBookCategory.MISC);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MultitoolInstallModeRecipe recipe) {
            // nothing to write
        }
    }
}