package com.terraskills.toroidalcompat.mixin;

import com.mojang.logging.LogUtils;
import com.terraskills.toroidalcompat.worldgen.ToroidalTFCChunkGenerator;
import com.toroidalworld.core.CarriedShape;
import com.toroidalworld.engine.gen.ShapedDimensions;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = ShapedDimensions.class, remap = false)
public abstract class ShapedDimensionsMixin {
    private static final Logger TFC_TOROIDAL_LOGGER = LogUtils.getLogger();

    @Inject(method = "withShape", at = @At("HEAD"), cancellable = true)
    private static void tfcToroidal$wrapTFCGenerator(
            WorldDimensions dimensions,
            ResourceKey<LevelStem> key,
            CarriedShape carriedShape,
            CallbackInfoReturnable<WorldDimensions> cir) {
        final LevelStem stem = dimensions.get(key).orElse(null);
        if (stem == null || !(stem.generator() instanceof TFCChunkGenerator tfc)
                || tfc instanceof ToroidalTFCChunkGenerator) {
            return;
        }

        final Map<ResourceKey<LevelStem>, LevelStem> stems = new HashMap<>(dimensions.dimensions());
        stems.put(key, new LevelStem(stem.type(), new ToroidalTFCChunkGenerator(tfc, carriedShape)));
        TFC_TOROIDAL_LOGGER.info("Applied Toroidal World shape to TFC dimension {}", key.location());
        cir.setReturnValue(new WorldDimensions(stems));
    }
}
