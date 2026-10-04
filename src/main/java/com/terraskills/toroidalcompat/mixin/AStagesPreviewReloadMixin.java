package com.terraskills.toroidalcompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
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
    @Inject(method = "onReloadStarted", at = @At("HEAD"), cancellable = true, require = 0)
    private void terraWorld$skipUninitialisedPreviewReload(final CallbackInfo callback) {
        try {
            final var cache = getClass().getDeclaredField("RESTRICTION_CACHE");
            cache.setAccessible(true);
            if (cache.get(null) == null) {
                callback.cancel();
            }
        } catch (final ReflectiveOperationException ignored) {
            // AStages changed or is absent: leave its normal callback intact.
        }
    }
}
