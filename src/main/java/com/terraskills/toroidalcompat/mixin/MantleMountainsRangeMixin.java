package com.terraskills.toroidalcompat.mixin;

import com.terraskills.toroidalcompat.worldgen.MantleMountainsTraversal;
import com.terraskills.toroidalcompat.worldgen.TfcTopology;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.dries007.tfc.world.region.AddMountainsAndBarrierIslands;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Makes Mantle Mountains' optional extended mountain ranges cross a looping edge. */
@Mixin(value = AddMountainsAndBarrierIslands.class, remap = false, priority = 2000)
public abstract class MantleMountainsRangeMixin {
    @Invoker("placeVolcanicArc")
    abstract IntSet terraWorld$placeVolcanicArc(Region region, net.minecraft.util.RandomSource random, int originIndex);

    @Invoker("placeBarrier")
    abstract IntSet terraWorld$placeBarrier(Region region, net.minecraft.util.RandomSource random, int originIndex);

    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void terraWorld$placeMantleRangesAcrossSeams(
            RegionGenerator.Context context, CallbackInfo callback) {
        if (!TfcTopology.active() || !net.neoforged.fml.ModList.get().isLoaded("tfcmountains")
                || !MantleMountainsTraversal.enabled("EXTENDED_MOUNTAIN_RANGES")) return;
        callback.cancel();

        final Region region = context.region;
        final var random = context.random;
        final RegionGenerator generator = context.generator();

        for (Region.Point point : region.points()) {
            if (point.land() && point.divergence < 0f
                    && point.distanceToEdge < 1.5d * generator.continentNoise.noise(point.x, point.z) - 5.2d) {
                point.setMountain();
            }
        }

        int mountains = 0;
        int volcanicArcs = 0;
        for (int attempts = 0; attempts < 40 && (mountains < 3 || volcanicArcs < 6); attempts++) {
            final Region.Point origin = region.random(random);
            if (origin == null) continue;

            if (mountains < 3 && origin.land() && (origin.baseLandHeight > 1
                    || origin.baseLandHeight >= 4 && origin.baseLandHeight <= 11)) {
                final List<MantleMountainsTraversal.Node> range = terraWorld$range(generator, region, origin, random);
                if (range.size() > 45) {
                    for (MantleMountainsTraversal.Node node : range) {
                        node.point().setMountain();
                        if (origin.divergence < 0f && origin.baseLandHeight < 8) node.point().setVolcanic();
                        if (origin.baseLandHeight <= 2) node.point().setCoastalMountain();
                    }
                    mountains++;
                }
            } else if (volcanicArcs < 6 && !origin.land() && origin.oceanDepth == 2
                    && origin.divergence < 0f && origin.distanceToDeepOcean <= 3 && origin.distanceToLand > 2) {
                final IntSet arc = terraWorld$placeVolcanicArc(region, random, origin.index);
                if (arc.size() > 45) {
                    final byte depth = origin.distanceToDeepOcean;
                    arc.forEach(index -> {
                        final Region.Point point = region.atIndex(index);
                        if (point.distanceToDeepOcean >= depth && point.distanceToDeepOcean <= depth + 1) point.setBarrierIsland();
                        point.setVolcanic();
                        point.oceanDepth = 1;
                    });
                    volcanicArcs += 2;
                }
            } else if (volcanicArcs < 6 && !origin.land() && origin.oceanDepth == 2
                    && origin.divergence > 0f && origin.distanceToLand > 2 && origin.distanceToLand < 6) {
                final IntSet barrier = terraWorld$placeBarrier(region, random, origin.index);
                final byte distance = (byte) Math.max(1, origin.distanceToLand);
                if (barrier.size() > 45) {
                    barrier.forEach(index -> {
                        final Region.Point point = region.atIndex(index);
                        if (point.distanceToLand == distance) point.setBarrierIsland();
                        point.oceanDepth = 1;
                    });
                    volcanicArcs++;
                }
            }
        }
    }

    private static List<MantleMountainsTraversal.Node> terraWorld$range(
            RegionGenerator generator, Region region, Region.Point origin, net.minecraft.util.RandomSource random) {
        final ArrayDeque<MantleMountainsTraversal.Node> queue = new ArrayDeque<>();
        final List<MantleMountainsTraversal.Node> range = new ArrayList<>();
        final Set<Long> visited = new HashSet<>();
        final MantleMountainsTraversal.Node first = new MantleMountainsTraversal.Node(region, origin);
        queue.add(first);
        range.add(first);
        visited.add(MantleMountainsTraversal.key(origin));

        final int originHeight = Math.max(1, origin.baseLandHeight);
        final double multiplier = MantleMountainsTraversal.decimal("EXTENDED_MOUNTAIN_RANGES_MULTIPLIER", 1d);
        final int maxSize = (int) Math.round((70 + random.nextInt(40)) * multiplier);
        final int tolerance = Math.max(1, (int) Math.round(multiplier));

        while (!queue.isEmpty() && range.size() <= maxSize) {
            final MantleMountainsTraversal.Node current = queue.removeFirst();
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                final MantleMountainsTraversal.Node next = MantleMountainsTraversal.adjacent(generator, current, dx, dz);
                if (next == null || !next.point().land()
                        || next.point().baseLandHeight < originHeight - tolerance
                        || next.point().baseLandHeight > originHeight + tolerance
                        || (next.point().baseLandHeight <= 2 && next.point().distanceToOcean >= 3)
                        || !visited.add(MantleMountainsTraversal.key(next.point()))) continue;

                if (current.point().baseLandHeight == next.point().baseLandHeight) queue.addFirst(next);
                else queue.addLast(next);
                range.add(next);
            }
        }
        return range;
    }
}
