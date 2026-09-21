package com.terraskills.toroidalcompat.worldgen;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldFolds;
import com.toroidalworld.core.WorldLoopBounds;
import com.toroidalworld.core.FlatShape;
import net.dries007.tfc.world.region.Units;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TfcTopologyTest {
    private WorldFold fold;

    @BeforeEach
    void setUp() {
        fold = WorldFolds.of(FlatShape.latticeTorus(WorldLoopBounds.ofWidth(32), 0));
        ActiveTfcFold.set(fold);
    }

    @AfterEach
    void tearDown() {
        ActiveTfcFold.clear();
    }

    @Test
    void foldsBlocksAndChunksIntoTheCanonicalDomain() {
        final var domain = fold.blockDomain(Direction.Axis.X);

        assertTrue(TfcTopology.active());
        assertEquals(domain.lowerBound, TfcTopology.block(domain.lowerBound + domain.domainLength, Direction.Axis.X));
        assertEquals(Math.floorDiv(domain.lowerBound, 16),
                TfcTopology.chunk(Math.floorDiv(domain.lowerBound + domain.domainLength, 16), Direction.Axis.X));
    }

    @Test
    void regionalSamplingRepeatsExactlyOneWorldApart() {
        final double gridPeriod = fold.blockDomain(Direction.Axis.X).domainLength
                / (double) Units.GRID_WIDTH_IN_BLOCK;

        final double first = TfcTopology.samplePeriodicGrid(0.75, -1.25, (x, z) -> Math.sin(x) + Math.cos(z));
        final double repeated = TfcTopology.samplePeriodicGrid(
                0.75 + gridPeriod, -1.25 + gridPeriod, (x, z) -> Math.sin(x) + Math.cos(z));

        assertEquals(first, repeated, 1e-12);
    }

    @Test
    void climateScaleAndPoleOffsetFitTheWrappedCircumference() {
        final var domain = fold.blockDomain(Direction.Axis.Z);
        final int scale = TfcTopology.climateScaleBlocks(Direction.Axis.Z, 20_000);

        assertEquals(domain.domainLength / 2, scale);
        assertEquals(-scale / 2 - domain.lowerBound, TfcTopology.climateZOffsetBlocks(20_000));
        assertTrue(TfcTopology.mirrorsSouthernHemisphere());
    }
}
