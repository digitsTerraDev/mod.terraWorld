package com.terraskills.toroidalcompat.mixin;

import com.toroidalworld.engine.gen.ShapedDimensions;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets Toroidal World's creation UI offer shapes for TFC's custom generator. */
@Mixin(value = ShapedDimensions.class, remap = false)
public abstract class ShapedDimensionsEligibilityMixin {
    @Inject(
            method = "canTakeShape(Lnet/minecraft/world/level/chunk/ChunkGenerator;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void tfcToroidal$allowTfcGenerator(
            ChunkGenerator generator, CallbackInfoReturnable<Boolean> cir) {
        if (generator instanceof TFCChunkGenerator) {
            cir.setReturnValue(true);
        }
    }
}
