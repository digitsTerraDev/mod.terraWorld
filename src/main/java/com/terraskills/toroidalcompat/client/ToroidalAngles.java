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

    /** Identifies which half of the Z loop contains the latitude arc. */
    public static char latitudeBranch(ToroidalShape shape, double z) {
        if (!shape.loops(Direction.Axis.Z)) return '\0';
        final double folded = shape.foldCoord(Direction.Axis.Z, z);
        return latitudeBranch(folded, shape.minBlock(Direction.Axis.Z), shape.widthBlocks(Direction.Axis.Z));
    }

    static double toroidal(double foldedCoordinate, double minimum, double width) {
        return fraction(foldedCoordinate, minimum, width) * 360.0;
    }

    static double poloidal(double foldedCoordinate, double minimum, double width) {
        // The seam and opposite side are the pole lines. Latitude changes at
        // a constant rate between each pole and its neighboring equators.
        final double turn = fraction(foldedCoordinate, minimum, width);
        return turn <= 0.5 ? 90.0 - 360.0 * turn : 360.0 * turn - 270.0;
    }

    static char latitudeBranch(double foldedCoordinate, double minimum, double width) {
        return fraction(foldedCoordinate, minimum, width) < 0.5d ? '+' : '-';
    }

    private static double fraction(double coordinate, double minimum, double width) {
        if (!(width > 0.0)) throw new IllegalArgumentException("Angle width must be positive");
        return (coordinate - minimum) / width;
    }

    private ToroidalAngles() {}
}
