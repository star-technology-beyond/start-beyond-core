package com.startechnology.start_core.item.multitool;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum StarTMultitoolMode {
    WRENCH(GTToolType.WRENCH),
    SCREWDRIVER(GTToolType.SCREWDRIVER),
    WIRE_CUTTER(GTToolType.WIRE_CUTTER),
    HARD_HAMMER(GTToolType.HARD_HAMMER),
    SOFT_MALLET(GTToolType.SOFT_MALLET),
    CROWBAR(GTToolType.CROWBAR);


    public static final String TAG_KEY = "StarT.MultitoolMode";
    public static final StarTMultitoolMode[] VALUES = values();

    private final GTToolType toolType;

    StarTMultitoolMode(GTToolType toolType) {
        this.toolType = toolType;
    }

    public GTToolType toolType() {
        return toolType;
    }

    public Component displayName() {
        return Component.translatable("gtceu.tool.class." + toolType.name);
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public StarTMultitoolMode offset(int amount) {
        int index = Math.floorMod(ordinal() + amount, VALUES.length);
        return VALUES[index];
    }

    public static StarTMultitoolMode get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_KEY, Tag.TAG_STRING)) {
            return WRENCH;
        }
        try {
            return valueOf(tag.getString(TAG_KEY));
        } catch (IllegalArgumentException ignored) {
            return WRENCH;
        }
    }

    public static void set(ItemStack stack, StarTMultitoolMode mode) {
        stack.getOrCreateTag().putString(TAG_KEY, mode.name());
    }
}
