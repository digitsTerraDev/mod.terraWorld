package com.terraskills.toroidalcompat.worldgen;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WrapDomain;
import net.dries007.tfc.world.region.Units;
import net.minecraft.core.Direction;

import java.util.function.DoubleBinaryOperator;

/** Topology operations formerly supplied by the patched TFC classes. */
public final class TfcTopology {
    public static boolean active() {
        return ActiveTfcFold.get() != null;
    }

    public static boolean loops(Direction.Axis axis) {
        final WorldFold fold = ActiveTfcFold.get();
        return fold != null && fold.bounds().loops(axis);
    }

    public static int block(int value, Direction.Axis axis) {
        final WorldFold fold = ActiveTfcFold.get();
        return fold == null || !fold.bounds().loops(axis) ? value : fold.blockDomain(axis).wrap(value);
    }

    public static int chunk(int value, Direction.Axis axis) {
        return scaled(value, 16, axis);
    }

    public static int grid(int value, Direction.Axis axis) {
        return (int) Math.floor(grid((double) value, axis));
    }

    public static double grid(double value, Direction.Axis axis) {
        final Domain domain = gridDomain(axis);
        return domain == null ? value : wrap(value, domain.min, domain.size);
    }

    public static int quart(int value, Direction.Axis axis) {
        final WorldFold fold = ActiveTfcFold.get();
        if (fold == null || !fold.bounds().loops(axis)) return value;
        final WrapDomain domain = fold.blockDomain(axis);
        final int minQuart = Math.floorDiv(domain.lowerBound, 4);
        final int sizeQuart = Math.floorDiv(domain.domainLength, 4);
        final int canonical = minQuart + Math.floorMod(value - minQuart, sizeQuart);
        return canonical == minQuart + sizeQuart - 1 ? minQuart : canonical;
    }

    public static double shortestGridDelta(double delta, Direction.Axis axis) {
        final Domain domain = gridDomain(axis);
        return domain == null ? delta : delta - Math.floor(delta / domain.size + 0.5d) * domain.size;
    }

    public static double samplePeriodicGrid(double x, double z, DoubleBinaryOperator field) {
        final Blend bx = blendGrid(x, Direction.Axis.X);
        final Blend bz = blendGrid(z, Direction.Axis.Z);
        final double nn = field.applyAsDouble(bx.near, bz.near);
        final double fn = bx.weight == 0 ? nn : field.applyAsDouble(bx.far, bz.near);
        final double x0 = lerp(bx.weight, nn, fn);
        if (bz.weight == 0) return x0;
        final double nf = field.applyAsDouble(bx.near, bz.far);
        final double ff = bx.weight == 0 ? nf : field.applyAsDouble(bx.far, bz.far);
        return lerp(bz.weight, x0, lerp(bx.weight, nf, ff));
    }

    public static int climateScaleBlocks(Direction.Axis axis, int requestedScale) {
        final WorldFold fold = ActiveTfcFold.get();
        if (requestedScale == 0 || fold == null || !fold.bounds().loops(axis)) return requestedScale;
        final int circumference = fold.blockDomain(axis).domainLength;
        final int cycles = Math.max(1, Math.round(circumference / (2f * requestedScale)));
        return Math.max(1, Math.round(circumference / (2f * cycles)));
    }

    public static int climateZOffsetBlocks(int requestedScale) {
        final WorldFold fold = ActiveTfcFold.get();
        if (fold == null || !fold.bounds().loops(Direction.Axis.Z)) return 0;
        final int scale = climateScaleBlocks(Direction.Axis.Z, requestedScale);
        return scale == 0 ? 0 : -scale / 2 - fold.blockDomain(Direction.Axis.Z).lowerBound;
    }

    public static boolean mirrorsSouthernHemisphere() {
        return loops(Direction.Axis.Z);
    }

    /**
     * Maps a looped Z axis onto a climate globe. The seam and opposite side are
     * the north and south pole lines; quarter turns are the equators.
     */
    public static float latitudeRadians(int z) {
        final WorldFold fold = ActiveTfcFold.get();
        if (fold == null || !fold.bounds().loops(Direction.Axis.Z)) return Float.NaN;
        final WrapDomain domain = fold.blockDomain(Direction.Axis.Z);
        final double fraction = (z - domain.lowerBound) / (double) domain.domainLength;
        return latitudeRadians(fraction);
    }

    public static boolean isNorthernHemisphere(int z) {
        final float latitude = latitudeRadians(z);
        return Float.isNaN(latitude) || latitude >= 0f;
    }

    public static float polarClimateFactor(int z) {
        final float latitude = latitudeRadians(z);
        return Float.isNaN(latitude) ? Float.NaN : Math.abs(latitude) / ((float) Math.PI / 2f);
    }

    /** Returns TFC's -1 (pole) to +1 (equator) temperature-noise baseline. */
    public static double polarClimateNoiseGrid(double z) {
        final Domain domain = gridDomain(Direction.Axis.Z);
        if (domain == null) return Double.NaN;
        final float latitude = latitudeRadians((z - domain.min) / domain.size);
        return 1d - 2d * Math.abs(latitude / ((float) Math.PI / 2f));
    }

    public static int minChunk(Direction.Axis axis) {
        final WorldFold fold = ActiveTfcFold.get();
        return fold == null ? 0 : Math.floorDiv(fold.blockDomain(axis).lowerBound, 16);
    }

    public static int sizeChunks(Direction.Axis axis) {
        final WorldFold fold = ActiveTfcFold.get();
        return fold == null ? 0 : fold.blockDomain(axis).domainLength / 16;
    }

    private static int scaled(int value, int blocksPerUnit, Direction.Axis axis) {
        if (!loops(axis)) return value;
        return Math.floorDiv(block(value * blocksPerUnit, axis), blocksPerUnit);
    }

    private static Domain gridDomain(Direction.Axis axis) {
        final WorldFold fold = ActiveTfcFold.get();
        if (fold == null || !fold.bounds().loops(axis)) return null;
        final WrapDomain domain = fold.blockDomain(axis);
        return new Domain(domain.lowerBound / (double) Units.GRID_WIDTH_IN_BLOCK,
                domain.domainLength / (double) Units.GRID_WIDTH_IN_BLOCK);
    }

    private static Blend blendGrid(double value, Direction.Axis axis) {
        final Domain domain = gridDomain(axis);
        if (domain == null) return new Blend(value, value, 0);
        final double canonical = wrap(value, domain.min, domain.size);
        final double blendWidth = domain.size / 2d;
        final double start = domain.min + domain.size - blendWidth;
        if (canonical <= start) return new Blend(canonical, canonical - domain.size, 0);
        final double t = (canonical - start) / blendWidth;
        return new Blend(canonical, canonical - domain.size, t * t * (3d - 2d * t));
    }

    private static double wrap(double value, double min, double size) {
        return min + (value - min - Math.floor((value - min) / size) * size);
    }

    private static float latitudeRadians(double fraction) {
        final double turn = fraction - Math.floor(fraction);
        return (float) ((turn <= 0.5d ? 1d - 4d * turn : 4d * turn - 3d) * (Math.PI / 2d));
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private record Domain(double min, double size) {}
    private record Blend(double near, double far, double weight) {}

    private TfcTopology() {}
}
