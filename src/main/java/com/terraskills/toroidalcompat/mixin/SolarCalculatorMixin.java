package com.terraskills.toroidalcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.terraskills.toroidalcompat.worldgen.TfcTopology;
import net.dries007.tfc.client.overworld.SolarCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SolarCalculator.class, remap = false)
public abstract class SolarCalculatorMixin {
    @ModifyReturnValue(method = "getLatitude", at = @At("RETURN"))
    private static float tfcToroidal$twoPoleLatitude(float latitude, int z, float hemisphereScale) {
        final float toroidalLatitude = TfcTopology.latitudeRadians(z);
        return Float.isNaN(toroidalLatitude) ? latitude : toroidalLatitude;
    }

    @Inject(method = "getInNorthernHemisphere(IF)Z", at = @At("HEAD"), cancellable = true)
    private static void tfcToroidal$mirrorHemisphere(
            int z, float hemisphereScale, CallbackInfoReturnable<Boolean> cir) {
        if (TfcTopology.loops(net.minecraft.core.Direction.Axis.Z)) {
            cir.setReturnValue(TfcTopology.isNorthernHemisphere(z));
        }
    }
}
