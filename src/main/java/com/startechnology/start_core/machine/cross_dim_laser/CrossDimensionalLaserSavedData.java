package com.startechnology.start_core.machine.cross_dim_laser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class CrossDimensionalLaserSavedData extends SavedData {

    public static final String DATA_NAME = "start_core_cross_dimensional_laser";

    /*
     * One entry per singularity pair/network
     * 
     * This holds all the linked pairs of cross dimensional lasers.
     */
    private final Map<Long, LaserEndpointPair> links = new HashMap<>();

    /*
     * A pair of receivers, this is held
     * by the position of the reciever
     */
    static class LaserEndpointPair {
        public GlobalPos sender;
        public GlobalPos receiver;
    }

    public static CrossDimensionalLaserSavedData get(Level level) {
        if (level.isClientSide()) {
            return null;
        }

        return get(((ServerLevel) level).getServer());
    }

    public static CrossDimensionalLaserSavedData get(
            MinecraftServer server) {

        DimensionDataStorage storage = server.overworld().getDataStorage();

        // Load our data from the dimension storage
        return storage.computeIfAbsent(
                CrossDimensionalLaserSavedData::load,
                CrossDimensionalLaserSavedData::new,
                DATA_NAME);
    }

    public void registerSender(Long linkId, GlobalPos pos) {

        LaserEndpointPair pair = links.computeIfAbsent(
                linkId,
                ignored -> new LaserEndpointPair());

        pair.sender = pos;

        setDirty();
    }

    public void registerReceiver(Long linkId, GlobalPos pos) {

        LaserEndpointPair pair = links.computeIfAbsent(
                linkId,
                ignored -> new LaserEndpointPair());

        pair.receiver = pos;

        setDirty();
    }

    /*
     * Unregisters the full pair with one of the pair
     * in this position
     */
    public void unregister(GlobalPos pos) {

        links.values().removeIf(pair -> {

            boolean changed = false;

            if (pos.equals(pair.sender)) {
                pair.sender = null;
                changed = true;
            }

            if (pos.equals(pair.receiver)) {
                pair.receiver = null;
                changed = true;
            }

            return pair.sender == null && pair.receiver == null;
        });

        setDirty();
    }

    public boolean hasPair(Long linkId) {
        return links.containsKey(linkId);
    }

    public Optional<GlobalPos> getReceiver(Long linkId) {

        LaserEndpointPair pair = links.get(linkId);

        if (pair == null || pair.receiver == null) {
            return Optional.empty();
        }

        return Optional.of(pair.receiver);
    }

    public Optional<GlobalPos> getSender(Long linkId) {

        LaserEndpointPair pair = links.get(linkId);

        if (pair == null || pair.sender == null) {
            return Optional.empty();
        }

        return Optional.of(pair.sender);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {

        ListTag linksTag = new ListTag();

        for (Map.Entry<Long, LaserEndpointPair> entry : links.entrySet()) {

            Long linkId = entry.getKey();
            LaserEndpointPair pair = entry.getValue();

            CompoundTag pairTag = new CompoundTag();

            pairTag.putLong("link_id", linkId);

            if (pair.sender != null) {
                pairTag.put(
                        "sender_pos",
                        writeGlobalPos(pair.sender));
            }

            if (pair.receiver != null) {
                pairTag.put(
                        "receiver_pos",
                        writeGlobalPos(pair.receiver));
            }

            linksTag.add(pairTag);
        }

        tag.put("links", linksTag);

        return tag;
    }

    public static CrossDimensionalLaserSavedData load(
            CompoundTag tag) {

        CrossDimensionalLaserSavedData data = new CrossDimensionalLaserSavedData();

        ListTag linksTag = tag.getList("links", Tag.TAG_COMPOUND);

        for (int i = 0; i < linksTag.size(); i++) {

            CompoundTag pairTag = linksTag.getCompound(i);

            Long linkId = pairTag.getLong("link_id");

            LaserEndpointPair pair = new LaserEndpointPair();

            if (pairTag.contains("sender_pos")) {
                pair.sender = readGlobalPos(
                        pairTag.getCompound("sender_pos"));
            }

            if (pairTag.contains("receiver_pos")) {
                pair.receiver = readGlobalPos(
                        pairTag.getCompound("receiver_pos"));
            }

            data.links.put(linkId, pair);
        }

        return data;
    }

    public static CompoundTag writeGlobalPos(
            GlobalPos pos) {

        CompoundTag tag = new CompoundTag();

        tag.put(
                "pos",
                NbtUtils.writeBlockPos(pos.pos()));

        tag.putString(
                "dimension",
                pos.dimension().location().toString());

        return tag;
    }

    public static GlobalPos readGlobalPos(
            CompoundTag tag) {

        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));

        ResourceLocation dimensionId = new ResourceLocation(
                tag.getString("dimension"));

        ResourceKey<Level> dimension = ResourceKey.create(
                Registries.DIMENSION,
                dimensionId);

        return GlobalPos.of(dimension, pos);
    }
}