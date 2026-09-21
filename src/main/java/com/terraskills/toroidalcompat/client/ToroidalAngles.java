package com.terraskills.toroidalcompat.client;

import com.toroidalworld.api.v1.ToroidalShape;
import net.minecraft.core.Direction;

/** Converts folded world coordinates into the torus angles shown to players. */
public final class ToroidalAngles {
    public static double longitude(ToroidalShape shape, double x) {
        if (!shape.loops(Direction.Axis.X)) return x;
        return toroidal(shape.foldCoord(Direction.Axis.X, x),
                shape.minBlock(Direction.Axis.X), shape.widthBlocks(Direction.Axis.X));
    }

    public static double latitude(ToroidalShape shape, double z) {
        if (!shape.loops(Direction.Axis.Z)) return z;
        return poloidal(shape.foldCoord(Direction.Axis.Z, z),
                shape.minBlock(Direction.Axis.Z), shape.widthBlocks(Direction.Axis.Z));
    }

    static double toroidal(double foldedCoordinate, double minimum, double width) {
        return fraction(foldedCoordinate, minimum, width) * 360.0;
    }

    static double poloidal(double foldedCoordinate, double minimum, double width) {
        // The equator is halfway down the map. The two signed 180-degree
        // endpoints are the same physical pole line, so crossing it swaps sign.
        return 180.0 - fraction(foldedCoordinate, minimum, width) * 360.0;
    }

    private static double fraction(double coordinate, double minimum, double width) {
        if (!(width > 0.0)) throw new IllegalArgumentException("Angle width must be positive");
        return (coordinate - minimum) / width;
    }

    private ToroidalAngles() {}
}
