package com.startechnology.start_core.item.multitool;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StarTMultitoolMode {
    public static final String TAG_KEY = "multitoolMode";
    public static final String TAG_INSTALLED = "installedModes";
    public static final String TAG_MATERIAL = "modeMaterial";

    // store the tool type and material of this mode
    private final GTToolType toolType;
    private final Material material;

    public StarTMultitoolMode(GTToolType toolType, Material material) {
        this.toolType = toolType;
        this.material = material;
    }

    public GTToolType toolType() {
        return toolType;
    }

    public Material material() {
        return material;
    }

    public MutableComponent displayName() {
        return Component.translatable("item.gtceu.tool." + toolType.name, material.getLocalizedName());
    }

    public String id() {
        return toolType.name;
    }

    public static void syncBehaviorsTag(ItemStack multiStack, GTToolType type, Material material) {
        var entry = GTMaterialItems.TOOL_ITEMS.get(material, type);
        if (entry == null) return;
        ItemStack reference = entry.get().get();
        if (reference.isEmpty()) return;

        // we ripoff the reference behaviours from the behaviours tag of this reference item
        CompoundTag refBehaviors = reference.getTagElement(ToolHelper.BEHAVIOURS_TAG_KEY);
        CompoundTag multiTag = multiStack.getOrCreateTag();

        if (refBehaviors != null && !refBehaviors.isEmpty()) {
            // copy over the behaviours to the multitool item stack
            multiTag.put(ToolHelper.BEHAVIOURS_TAG_KEY, refBehaviors.copy());
        } else {
            // no behaviours for this tool, clear the one for the multitool
            multiTag.remove(ToolHelper.BEHAVIOURS_TAG_KEY);
        }
    }

    public static List<StarTMultitoolMode> getInstalled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        List<StarTMultitoolMode> result = new ArrayList<>();
        if (tag == null || !tag.contains(TAG_INSTALLED, Tag.TAG_LIST)) return result;

        // Get the list of all the installed types
        ListTag list = tag.getList(TAG_INSTALLED, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {

            // Get the name of the tool type and use that to get the actual
            // tool type
            String typeName = list.getString(i);
            GTToolType type = GTToolType.getTypes().get(typeName);
            if (type == null) continue;

            // Get the material name
            String matName = tag.getString(TAG_MATERIAL + typeName);
            Material mat = GTCEuAPI.materialManager.getMaterial(matName);
            if (mat == null) continue;

            result.add(new StarTMultitoolMode(type, mat));
        }
        return result;
    }

    public static boolean isInstalled(ItemStack stack, GTToolType type) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_INSTALLED, Tag.TAG_LIST)) return false;

        // Check if installed list contains the types name
        ListTag list = tag.getList(TAG_INSTALLED, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(type.name)) return true;
        }
        return false;
    }

    public static void install(ItemStack stack, GTToolType type, Material material) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.contains(TAG_INSTALLED, Tag.TAG_LIST)
                ? tag.getList(TAG_INSTALLED, Tag.TAG_STRING)
                : new ListTag();

        // Avoid duplicates
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(type.name)) return;
        }
        list.add(StringTag.valueOf(type.name));
        tag.put(TAG_INSTALLED, list);
        tag.putString(TAG_MATERIAL + type.name, material.getName());

        // if no active mode yet then set this as active
        if (!tag.contains(TAG_KEY, Tag.TAG_STRING)) {
            tag.putString(TAG_KEY, type.name);
            syncBehaviorsTag(stack, type, material);
        }
    }

    public static void uninstall(ItemStack stack, GTToolType type) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        // Create a new list with this type missing
        ListTag list = tag.getList(TAG_INSTALLED, Tag.TAG_STRING);
        ListTag newList = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            if (!list.getString(i).equals(type.name)) {
                newList.add(list.get(i));
            }
        }

        tag.put(TAG_INSTALLED, newList);
        tag.remove(TAG_MATERIAL + type.name);

        // if active mode was this one then switch to first available
        String active = tag.getString(TAG_KEY);
        if (active.equals(type.name)) {
            if (!newList.isEmpty()) {
                // put the name of the next mode
                String nextName = newList.getString(0);
                tag.putString(TAG_KEY, nextName);

                // get the type of the next mode and material for behaviour syncing
                GTToolType nextType = GTToolType.getTypes().get(nextName);
                Material nextMat = GTCEuAPI.materialManager.getMaterial(tag.getString(TAG_MATERIAL + nextName));
                if (nextType != null && nextMat != null) {
                    syncBehaviorsTag(stack, nextType, nextMat);
                }
            } else {
                tag.remove(TAG_KEY);
            }
        }

        // clear cached stats so GTCEu recalculates
        tag.remove(ToolHelper.TOOL_SPEED_KEY);
        tag.remove(ToolHelper.ATTACK_DAMAGE_KEY);
        tag.remove(ToolHelper.ATTACK_SPEED_KEY);
        tag.remove(ToolHelper.HARVEST_LEVEL_KEY);
    }

    public static Material getMaterialForType(ItemStack stack, GTToolType type) {
        // Get the material for the tool type in this stack
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        String matName = tag.getString(TAG_MATERIAL + type.name);
        return GTCEuAPI.materialManager.getMaterial(matName);
    }

    public static StarTMultitoolMode getActive(ItemStack stack) {
        List<StarTMultitoolMode> installed = getInstalled(stack);
        if (installed.isEmpty()) return null;

        CompoundTag tag = stack.getTag();
        if (tag == null) return installed.get(0);
        
        // The current active mode is stored under TAG_KEY
        // so filter until we find the mode that equals the active
        // mode
        String activeName = tag.getString(TAG_KEY);
        return installed.stream()
                .filter(mode -> mode.id().equals(activeName))
                .findFirst()
                .orElse(installed.get(0));
    }

    public static void setActive(ItemStack stack, StarTMultitoolMode mode) {
        // set the active mode
        stack.getOrCreateTag().putString(TAG_KEY, mode.id());
        CompoundTag toolTag = ToolHelper.getToolTag(stack);

        // let gtceu recalculate these stats
        toolTag.remove(ToolHelper.TOOL_SPEED_KEY);
        toolTag.remove(ToolHelper.ATTACK_DAMAGE_KEY);
        toolTag.remove(ToolHelper.ATTACK_SPEED_KEY);
        toolTag.remove(ToolHelper.HARVEST_LEVEL_KEY);

        syncBehaviorsTag(stack, mode.toolType(), mode.material());
    }

    public static StarTMultitoolMode offset(ItemStack stack, int amount) {
        // return an offset into the current mode of the
        // stack based on the amount
        List<StarTMultitoolMode> installed = getInstalled(stack);
        if (installed.isEmpty()) return null;

        StarTMultitoolMode active = getActive(stack);
        int idx = active == null ? 0 : installed.indexOf(active);

        return installed.get(Math.floorMod(idx + amount, installed.size()));
    }

    // check if a GT tool stack is not damaged
    public static boolean isFullyRepaired(ItemStack toolStack) {
        CompoundTag toolTag = ToolHelper.getToolTag(toolStack);
        return toolTag.getInt(ToolHelper.DURABILITY_KEY) == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StarTMultitoolMode that = (StarTMultitoolMode) obj;

        // Compare based on the tool types name/id
        return this.id().equals(that.id()); 
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }
}