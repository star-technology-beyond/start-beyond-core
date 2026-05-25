package com.startechnology.start_core.machine.wind_turbine;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class StarTWindTurbineManager {

    private final HashMap<ResourceKey<Level>, RTree<StarTWindTurbineMachine, Geometry>> turbineTrees = new HashMap<>();
    private final HashSet<TurbineKey> insertedTurbines = new HashSet<>();
    private final HashMap<StarTWindTurbineMachine, TurbineKey> machineKeys = new HashMap<>();

    private static final StarTWindTurbineManager MANAGER = new StarTWindTurbineManager();

    private StarTWindTurbineManager() {
    }

    public static void addTurbine(StarTWindTurbineMachine machine) {
        if (machine.isRemote()) return;

        BlockPos position = machine.getPos();
        ResourceKey<Level> dimension = machine.getLevel().dimension();
        TurbineKey key = new TurbineKey(dimension, position);

        if (!MANAGER.insertedTurbines.add(key)) {
            MANAGER.removeTurbineAt(key);
            MANAGER.insertedTurbines.add(key);
        }

        MANAGER.turbineTrees.putIfAbsent(dimension, RTree.create());
        MANAGER.machineKeys.put(machine, key);
        MANAGER.turbineTrees.compute(dimension, (dimensionKey, tree) ->
            tree.add(machine, Geometries.point(position.getX(), position.getZ()))
        );
    }

    public static void removeTurbine(StarTWindTurbineMachine machine) {
        if (machine.isRemote()) return;

        TurbineKey key = MANAGER.machineKeys.remove(machine);
        if (key == null) {
            key = new TurbineKey(machine.getLevel().dimension(), machine.getPos());
        }

        MANAGER.removeTurbineAt(key);
    }

    public static boolean hasNearbyTurbine(StarTWindTurbineMachine machine, int radius) {
        if (machine.isRemote()) return false;

        BlockPos center = machine.getPos();
        int radiusSquared = radius * radius;
        ResourceKey<Level> dimension = machine.getLevel().dimension();

        MANAGER.turbineTrees.putIfAbsent(dimension, RTree.create());

        var nearbyEntry = MANAGER.turbineTrees.get(dimension)
            .search(Geometries.rectangle(
                center.getX() - radius,
                center.getZ() - radius,
                center.getX() + radius,
                center.getZ() + radius
            ))
            .filter(entry -> {
                StarTWindTurbineMachine other = entry.value();
                if (other == machine || !MANAGER.isValidEntry(other)) return false;

                BlockPos otherPos = other.getPos();
                long dx = center.getX() - otherPos.getX();
                long dy = center.getY() - otherPos.getY();
                long dz = center.getZ() - otherPos.getZ();

                return dx * dx + dy * dy + dz * dz <= radiusSquared;
            })
            .toBlocking()
            .firstOrDefault(null);

        return nearbyEntry != null;
    }

    private void removeTurbineAt(TurbineKey key) {
        insertedTurbines.remove(key);
        turbineTrees.putIfAbsent(key.dimension(), RTree.create());

        var entriesToDelete = turbineTrees.get(key.dimension())
            .search(Geometries.point(key.position().getX(), key.position().getZ()).mbr())
            .filter(entry -> entry.value().getPos().equals(key.position()))
            .toList()
            .toBlocking()
            .single();

        for (var entry : entriesToDelete) {
            machineKeys.remove(entry.value());
            turbineTrees.compute(key.dimension(), (dimension, tree) ->
                tree.delete(entry.value(), entry.geometry())
            );
        }
    }

    private boolean isValidEntry(StarTWindTurbineMachine machine) {
        if (!machine.isFormed()) return false;

        TurbineKey key = machineKeys.get(machine);
        if (key == null || !insertedTurbines.contains(key)) return false;

        BlockEntity blockEntity = machine.getLevel().getBlockEntity(machine.getPos());
        if (!(blockEntity instanceof IMachineBlockEntity machineBlockEntity)) return false;

        return machineBlockEntity.getMetaMachine() == machine;
    }

    private record TurbineKey(ResourceKey<Level> dimension, BlockPos position) {
    }
}
