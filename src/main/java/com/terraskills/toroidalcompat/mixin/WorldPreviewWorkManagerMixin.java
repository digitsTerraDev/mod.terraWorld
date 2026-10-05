package com.terraskills.toroidalcompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * World Preview derives the maximum batch size from the number of units to
 * queue. For small replacement ranges that value can be below its hard-coded
 * minimum of eight, which makes Math.clamp throw instead of queuing the work.
 */
@Mixin(targets = "com.rustysnail.world.preview.tfc.backend.WorkManager", remap = false)
public abstract class WorldPreviewWorkManagerMixin {
    @ModifyArg(
        method = "queueForLevel",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;clamp(JII)I"),
        index = 2
    )
    private int tfcToroidal$keepPreviewBatchBoundsValid(int maximum) {
        return Math.max(8, maximum);
    }
}
