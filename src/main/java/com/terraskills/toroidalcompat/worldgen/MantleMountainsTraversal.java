package com.terraskills.toroidalcompat.worldgen;

import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;

import java.lang.reflect.Field;

/** Optional, reflection-only access to Mantle Mountains' seam-sensitive settings and graph. */
public final class MantleMountainsTraversal {
    private static final String CONFIG_CLASS = "com.encanis.tfcmountains.TFCMountainsConfig";

    public static boolean enabled(String setting) {
        return (boolean) value(setting, Boolean.FALSE);
    }

    public static int integer(String setting, int fallback) {
        return ((Number) value(setting, fallback)).intValue();
    }

    public static double decimal(String setting, double fallback) {
        return ((Number) value(setting, fallback)).doubleValue();
    }

    public static Node adjacent(RegionGenerator generator, Node node, int dx, int dz) {
        final int rawX = node.point.x + dx;
        final int rawZ = node.point.z + dz;
        final Region.Point local = node.owner.at(rawX, rawZ);
        if (local != null) return new Node(node.owner, local);

        final int foldedX = TfcTopology.grid(rawX, net.minecraft.core.Direction.Axis.X);
        final int foldedZ = TfcTopology.grid(rawZ, net.minecraft.core.Direction.Axis.Z);
        if (foldedX == rawX && foldedZ == rawZ) return null;

        final Region owner = generator.getOrCreateRegion(foldedX, foldedZ);
        final Region.Point point = owner.at(foldedX, foldedZ);
        return point == null ? null : new Node(owner, point);
    }

    public static long key(Region.Point point) {
        final int x = TfcTopology.grid(point.x, net.minecraft.core.Direction.Axis.X);
        final int z = TfcTopology.grid(point.z, net.minecraft.core.Direction.Axis.Z);
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static Object value(String setting, Object fallback) {
        try {
            final Class<?> config = Class.forName(CONFIG_CLASS);
            final Field field = config.getField(setting);
            final Object configValue = field.get(null);
            return configValue.getClass().getMethod("get").invoke(configValue);
        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }

    public record Node(Region owner, Region.Point point) { }

    private MantleMountainsTraversal() { }
}
