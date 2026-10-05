package com.terraskills.toroidalcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.terraskills.toroidalcompat.worldgen.PeriodicField2D;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.noise.Noise2D;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mantle Mountains replaces this factory with a separate OpenSimplex ridge field.
 * Unlike TFC's normal height-field route, that replacement does not pass through
 * {@link BiomeNoiseSamplerMixin}, so it needs its own toroidal boundary wrapper.
 */
@Mixin(value = BiomeNoise.class, remap = false)
public abstract class MantleMountainsBiomeNoiseMixin {
    @ModifyReturnValue(
            method = "ridgeMountains(JDDFII)Lnet/dries007/tfc/world/noise/Noise2D;",
            at = @At("RETURN"),
            remap = false)
    private static Noise2D terraWorld$makeMantleRidgeFieldPeriodic(Noise2D original) {
        if (!ModList.get().isLoaded("tfcmountains")) return original;
        return (x, z) -> PeriodicField2D.sample(x, z, original::noise);
    }
}
