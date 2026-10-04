package com.terraskills.toroidalcompat.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * World Preview creates a temporary world loader before AStages has populated
 * its restriction cache. AStages 2.5.2 dereferences that cache in its reload
 * callback, aborting preview setup. Skip only that uninitialised callback.
 */
@Pseudo
@Mixin(targets = "com.alessandro.astages.engine.ASimpleRestrictionManager", remap = false)
public abstract class AStagesPreviewReloadMixin {
    @Shadow
    private static Map<?, ?> RESTRICTION_CACHE;

    @Inject(method = "onReloadStarted", at = @At("HEAD"), cancellable = true, require = 0)
    private static void terraWorld$skipUninitialisedPreviewReload(final CallbackInfo callback) {
        if (RESTRICTION_CACHE == null) {
            callback.cancel();
        }
    }
}
