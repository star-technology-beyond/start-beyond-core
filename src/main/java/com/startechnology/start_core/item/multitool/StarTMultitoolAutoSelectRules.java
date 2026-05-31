package com.startechnology.start_core.item.multitool;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StarTMultitoolAutoSelectRules {

    public static final String TAG_RULES = "autoSelectRules";
    public static final String TAG_REGEX = "regex";
    public static final String TAG_TOOL_TYPE = "toolType";

    // defaults for a multitool
    public static final List<Rule> DEFAULTS = List.of(
            new Rule(".*log.*|.*wood.*|.*planks.*|.*leaves.*", GTToolType.AXE.name),
            new Rule(
                    ".*ore.*|.*stone.*|.*cobblestone.*|.*deepslate.*|.*basalt.*|.*granite.*|.*diorite.*|.*andesite.*|.*tuff.*|.*calcite.*|.*obsidian.*|.*netherrack.*|.*blackstone.*|.*end_stone.*",
                    GTToolType.PICKAXE.name),
            new Rule(
                    ".*dirt.*|.*grass.*|.*gravel.*|.*sand.*|.*clay.*|.*mud.*|.*soul_sand.*|.*soul_soil.*|.*farmland.*|.*podzol.*|.*mycelium.*",
                    GTToolType.SHOVEL.name),
            new Rule(".*grass.*|.*fern.*|.*vine.*|.*cobweb.*|.*leaves.*|.*wool.*", GTToolType.SHEARS.name),
            new Rule(
                    ".*crop.*|.*wheat.*|.*carrot.*|.*potato.*|.*beetroot.*|.*melon.*|.*pumpkin.*|.*sugar_cane.*|.*bamboo.*|.*nether_wart.*",
                    GTToolType.HOE.name),
            new Rule(".*", GTToolType.PICKAXE.name) // catch-all fallback
    );

    // a single rule record belonging to a regex and a type name
    public record Rule(String regex, String toolTypeName) {

        // returns true when a block matches this regex
        public boolean matches(String blockId) {
            try {
                return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(blockId).matches();
            } catch (PatternSyntaxException e) {
                return false;
            }
        }

        // returns true when the rule's regex compiles without fialure
        public boolean isValid() {
            try {
                Pattern.compile(regex);
                return true;
            } catch (PatternSyntaxException e) {
                return false;
            }
        }
    }

    public static List<Rule> getRules(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        // if there is no tag then return the defaults
        List<Rule> result = new ArrayList<>();
        if (tag == null || !tag.contains(TAG_RULES, Tag.TAG_LIST)) {
            return new ArrayList<>(DEFAULTS);
        }

        // else get all the regex rules from the tags
        ListTag list = tag.getList(TAG_RULES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String regex = entry.getString(TAG_REGEX);
            String toolType = entry.getString(TAG_TOOL_TYPE);
            if (!regex.isEmpty() && !toolType.isEmpty()) {
                result.add(new Rule(regex, toolType));
            }
        }
        return result;
    }

    public static void setRules(ItemStack stack, List<Rule> rules) {
        // set all the rules in the tag
        ListTag list = new ListTag();
        for (Rule rule : rules) {
            CompoundTag entry = new CompoundTag();
            entry.putString(TAG_REGEX, rule.regex());
            entry.putString(TAG_TOOL_TYPE, rule.toolTypeName());
            list.add(entry);
        }
        stack.getOrCreateTag().put(TAG_RULES, list);
    }

    // writes all the defaults to a stack's tags to ensure
    // that the tag is present
    public static void ensureDefaults(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_RULES, Tag.TAG_LIST)) {
            setRules(stack, new ArrayList<>(DEFAULTS));
        }
    }


    // finds the first/best mode for which matches this block id to the multitoolo stack
    public static StarTMultitoolMode findBestMode(ItemStack stack, String blockId) {
        List<Rule> rules = getRules(stack);

        // we need to ensure the mode falls into the installed tools
        List<StarTMultitoolMode> installed = StarTMultitoolMode.getInstalled(stack);
        if (installed.isEmpty())
            return null;

        // match against each rule
        for (Rule rule : rules) {
            if (!rule.matches(blockId))
                continue;

            // this rool matches, we get the tol otype from the tool type name
            GTToolType desired = GTToolType.getTypes().get(rule.toolTypeName());
            if (desired == null)
                continue;

            // if the mode is installed, we can return it
            for (StarTMultitoolMode mode : installed) {
                if (mode.toolType() == desired)
                    return mode;
            }
        }
        return null;
    }
}