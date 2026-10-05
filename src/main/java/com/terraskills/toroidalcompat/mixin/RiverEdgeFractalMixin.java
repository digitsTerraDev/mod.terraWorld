package com.terraskills.toroidalcompat.mixin;

import com.terraskills.toroidalcompat.worldgen.TfcTopology;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Builds a river's jittered centreline along the short toroidal route. */
@Mixin(targets = "net.dries007.tfc.world.river.River$Edge", remap = false)
public abstract class RiverEdgeFractalMixin {
    @ModifyArgs(
            method = "fractal",
            at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/world/river/MidpointFractal;<init>(Lnet/minecraft/util/RandomSource;IDDDD)V"))
    private void tfcToroidal$buildFractalAcrossShortestSeam(Args args) {
        final double sourceX = args.get(2);
        final double sourceZ = args.get(3);
        final double drainX = args.get(4);
        final double drainZ = args.get(5);
        args.set(4, sourceX + TfcTopology.shortestGridDelta(drainX - sourceX, Direction.Axis.X));
        args.set(5, sourceZ + TfcTopology.shortestGridDelta(drainZ - sourceZ, Direction.Axis.Z));
    }
}
