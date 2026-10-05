package com.terraskills.toroidalcompat.mixin;

import com.terraskills.toroidalcompat.worldgen.MantleMountainsTraversal;
import com.terraskills.toroidalcompat.worldgen.TfcTopology;
import net.dries007.tfc.world.region.AnnotateBiomeAltitude;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Replaces Mantle's local-index foothill flood fill only for looped worlds. */
@Mixin(value = AnnotateBiomeAltitude.class, remap = false, priority = 2000)
public abstract class MantleMountainsFoothillMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void terraWorld$propagateMantleFoothillsAcrossSeams(
            RegionGenerator.Context context, CallbackInfo callback) {
        if (!TfcTopology.active()
                || !net.neoforged.fml.ModList.get().isLoaded("tfcmountains")) return;

        final int width = MantleMountainsTraversal.integer("FOOTHILL_WIDTH", 4);
        if (width == 4) return;
        callback.cancel();

        final Region region = context.region;
        final RegionGenerator generator = context.generator();
        final ArrayDeque<MantleMountainsTraversal.Node> queue = new ArrayDeque<>();
        final Set<Long> visited = new HashSet<>();
        final int peakAltitude = 3 * width;

        for (Region.Point point : region.points()) {
            if (point.land() && point.mountain()) {
                point.biomeAltitude = (byte) peakAltitude;
                final MantleMountainsTraversal.Node node = new MantleMountainsTraversal.Node(region, point);
                queue.addLast(node);
                visited.add(MantleMountainsTraversal.key(point));
            }
        }

        while (!queue.isEmpty()) {
            final MantleMountainsTraversal.Node current = queue.removeFirst();
            final int nextAltitude = current.point().biomeAltitude - 1;
            if (nextAltitude < 0) continue;

            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                final MantleMountainsTraversal.Node next = MantleMountainsTraversal.adjacent(generator, current, dx, dz);
                if (next == null || !next.point().land() || next.point().biomeAltitude != 0
                        || !visited.add(MantleMountainsTraversal.key(next.point()))) continue;

                if (context.random.nextInt(13) == 0 && current.point().biomeAltitude != peakAltitude) {
                    next.point().biomeAltitude = current.point().biomeAltitude;
                    queue.addFirst(next);
                } else {
                    next.point().biomeAltitude = (byte) nextAltitude;
                    queue.addLast(next);
                }
            }
        }

        for (Region.Point point : region.points()) {
            if (!point.land()) continue;
            if (point.discreteBiomeAltitude() == 0 && point.baseLandHeight >= 4) {
                point.biomeAltitude = (byte) width;
            } else if (point.discreteBiomeAltitude() == 1 && point.baseLandHeight >= 11) {
                point.biomeAltitude = (byte) (2 * width);
            }
        }
    }
}
