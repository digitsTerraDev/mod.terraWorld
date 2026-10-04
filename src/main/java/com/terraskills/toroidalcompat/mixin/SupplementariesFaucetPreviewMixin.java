package com.terraskills.toroidalcompat.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * World Preview's temporary server has not created an overworld yet.
 * Supplementaries tries to build a fake level from that null overworld while
 * reloading faucet interactions, which aborts preview creation.
 */
@Pseudo
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager", remap = false)
public abstract class SupplementariesFaucetPreviewMixin {
    @Inject(method = "onLevelLoad", at = @At("HEAD"), cancellable = true, require = 0)
    private void terraWorld$skipMissingPreviewOverworld(final ServerLevel original, final CallbackInfo callback) {
        if (original == null) {
            callback.cancel();
        }
    }
}
