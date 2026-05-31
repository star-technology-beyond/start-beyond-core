package com.startechnology.start_core.network.packets;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import com.startechnology.start_core.item.multitool.StarTMultitoolAutoSelectRules;
import com.startechnology.start_core.item.multitool.StarTMultitoolItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CPacketSaveAutoSelectRules implements IPacket {

    private static final int MAX_RULES = 64;
    private static final int MAX_STR_LENGTH = 512;

    private int handOrdinal;
    private List<StarTMultitoolAutoSelectRules.Rule> rules;

    public CPacketSaveAutoSelectRules() {
    }

    public CPacketSaveAutoSelectRules(InteractionHand hand,
            List<StarTMultitoolAutoSelectRules.Rule> rules) {
        this.handOrdinal = hand.ordinal();
        this.rules = rules;
    }
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(handOrdinal);
        buf.writeVarInt(rules.size());

        // encode all the rules
        for (StarTMultitoolAutoSelectRules.Rule rule : rules) {
            buf.writeUtf(rule.regex(), MAX_STR_LENGTH);
            buf.writeUtf(rule.toolTypeName(), 128);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        handOrdinal = buf.readVarInt();
        int count = Math.min(buf.readVarInt(), MAX_RULES);

        // decode rules back
        rules = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String regex = buf.readUtf(MAX_STR_LENGTH);
            String toolType = buf.readUtf(128);
            rules.add(new StarTMultitoolAutoSelectRules.Rule(regex, toolType));
        }
    }

    @Override
    public void execute(IHandlerContext handler) {
        Player player = handler.getPlayer();
        if (player == null)
            return;

        InteractionHand hand = InteractionHand.values()[handOrdinal];
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof StarTMultitoolItem))
            return;

        // drop any rules with an invalid regex
        List<StarTMultitoolAutoSelectRules.Rule> clean = new ArrayList<>();
        for (StarTMultitoolAutoSelectRules.Rule rule : rules) {
            if (rule.isValid())
                clean.add(rule);
        }

        // set the rules for this stack to be the new clean rules
        StarTMultitoolAutoSelectRules.setRules(stack, clean);
    }
}