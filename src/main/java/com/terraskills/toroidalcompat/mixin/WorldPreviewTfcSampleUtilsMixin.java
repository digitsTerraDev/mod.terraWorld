package com.terraskills.toroidalcompat.mixin;

import com.terraskills.toroidalcompat.worldgen.ActiveTfcFold;
import com.terraskills.toroidalcompat.worldgen.ToroidalTFCChunkGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * World Preview TFC creates its own RegionGenerator instead of using the one
 * inside the selected chunk generator. Propagate the selected generator's
 * fold before it creates those TFC sampling structures.
 */
@Pseudo
@Mixin(targets = "com.rustysnail.world.preview.tfc.backend.worker.tfc.TFCSampleUtils", remap = false)
public abstract class WorldPreviewTfcSampleUtilsMixin {
    @Inject(method = "create", at = @At("HEAD"))
    private static void tfcToroidal$usePreviewFold(
            ChunkGenerator generator,
            RegistryAccess registryAccess,
            long seed,
            CallbackInfoReturnable<?> cir) {
        if (generator instanceof ToroidalTFCChunkGenerator toroidal) {
            ActiveTfcFold.set(toroidal.carriedShape().fold());
        } else {
            ActiveTfcFold.clear();
        }
    }
}
