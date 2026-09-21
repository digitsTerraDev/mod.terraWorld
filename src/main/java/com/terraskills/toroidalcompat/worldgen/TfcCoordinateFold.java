package com.terraskills.toroidalcompat.worldgen;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;

public final class TfcCoordinateFold {
    private static final int BLOCKS_PER_GRID = 128;
    private static final int BLOCKS_PER_CHUNK = 16;

    public static int quart(int coordinate, Direction.Axis axis) {
        final WorldFold fold = activeFold();
        if (fold == null || !fold.bounds().loops(axis)) {
            return coordinate;
        }
        return QuartPos.fromBlock(fold.blockDomain(axis).wrap(QuartPos.toBlock(coordinate)));
    }

    public static int block(int coordinate, Direction.Axis axis) {
        final WorldFold fold = activeFold();
        if (fold == null || !fold.bounds().loops(axis)) {
            return coordinate;
        }
        return fold.blockDomain(axis).wrap(coordinate);
    }

    public static int grid(int coordinate, Direction.Axis axis) {
        return scaled(coordinate, BLOCKS_PER_GRID, axis);
    }

    public static int chunk(int coordinate, Direction.Axis axis) {
        return scaled(coordinate, BLOCKS_PER_CHUNK, axis);
    }

    private static int scaled(int coordinate, int blocksPerUnit, Direction.Axis axis) {
        final WorldFold fold = activeFold();
        if (fold == null || !fold.bounds().loops(axis)) {
            return coordinate;
        }
        return Math.floorDiv(fold.blockDomain(axis).wrap(coordinate * blocksPerUnit), blocksPerUnit);
    }

    private static WorldFold activeFold() {
        final WorldFold scoped = GenerationTransformerContext.context().wrappedTransformer();
        return scoped != null ? scoped : ActiveTfcFold.get();
    }

    private TfcCoordinateFold() { }
}
