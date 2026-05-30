package com.startechnology.start_core.item.multitool;

import static com.startechnology.start_core.StarTCore.START_REGISTRATE;

import java.util.HashMap;
import java.util.Map;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class StarTMultitoolItems {
    // all generated multitool variants (one for each type)
    public static final Map<GTToolType, ItemEntry<StarTMultitoolItem>> MULTITOOLS = new HashMap<>();

    // empty variant
    public static final ItemEntry<StarTMultitoolItem> MULTITOOL_EMPTY = START_REGISTRATE
            .item("multitool_empty",
                    properties -> new StarTMultitoolItem(GTToolType.KNIFE,
                            GTMaterials.Neutronium.getToolTier(),
                            GTMaterials.Neutronium, properties, GTValues.LuV))
            .properties(properties -> properties.stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .register();

    public static void init() {
        START_REGISTRATE.addDataGenerator(ProviderType.LANG, prov ->
                prov.add("item.start_core.gregtech_multitool", "Symbiotic Swiss Knife"));

        // generate multitool variant for the type with proper
        // tags for crafting purposes
        for (GTToolType type : GTToolType.getTypes().values()) {
            var builder = START_REGISTRATE.item("multitool_" + type.name,
                            properties -> new StarTMultitoolItem(type,
                                    GTMaterials.Neutronium.getToolTier(),
                                    GTMaterials.Neutronium, properties, GTValues.LuV))
                    .properties(properties -> properties.stacksTo(1))
                    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .color(() -> IGTTool::tintColor);

            for (TagKey<Item> tag : type.itemTags) {
                builder = builder.tag(tag);
            }

            MULTITOOLS.put(type, builder.register());
        }
    }
}
