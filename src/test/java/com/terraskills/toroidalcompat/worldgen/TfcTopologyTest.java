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
    void climateScaleAndTwoPoleLatitudeFitTheWrappedCircumference() {
        final var domain = fold.blockDomain(Direction.Axis.Z);
        final int scale = TfcTopology.climateScaleBlocks(Direction.Axis.Z, 20_000);

        assertEquals(domain.domainLength / 2, scale);
        assertEquals((float) (Math.PI / 2d), TfcTopology.latitudeRadians(domain.lowerBound), 1e-6f);
        assertEquals((float) (Math.PI / 4d), TfcTopology.latitudeRadians(domain.lowerBound + domain.domainLength / 8), 1e-6f);
        assertEquals(0f, TfcTopology.latitudeRadians(domain.lowerBound + domain.domainLength / 4), 1e-6f);
        assertEquals((float) (-Math.PI / 2d), TfcTopology.latitudeRadians(domain.lowerBound + domain.domainLength / 2), 1e-6f);
        assertEquals(0f, TfcTopology.latitudeRadians(domain.lowerBound + 3 * domain.domainLength / 4), 1e-6f);
        assertTrue(TfcTopology.isNorthernHemisphere(domain.lowerBound));
        assertTrue(!TfcTopology.isNorthernHemisphere(domain.lowerBound + domain.domainLength / 2));
        assertEquals(1f, TfcTopology.polarClimateFactor(domain.lowerBound), 1e-6f);
        assertEquals(0f, TfcTopology.polarClimateFactor(domain.lowerBound + domain.domainLength / 4), 1e-6f);

        final double minGrid = domain.lowerBound / (double) Units.GRID_WIDTH_IN_BLOCK;
        final double gridSize = domain.domainLength / (double) Units.GRID_WIDTH_IN_BLOCK;
        assertEquals(-1d, TfcTopology.polarClimateNoiseGrid(minGrid), 1e-12);
        assertEquals(1d, TfcTopology.polarClimateNoiseGrid(minGrid + gridSize / 4d), 1e-12);
        assertEquals(-1d, TfcTopology.polarClimateNoiseGrid(minGrid + gridSize / 2d), 1e-12);
        assertEquals(1d, TfcTopology.polarClimateNoiseGrid(minGrid + 3d * gridSize / 4d), 1e-12);
    }
}
