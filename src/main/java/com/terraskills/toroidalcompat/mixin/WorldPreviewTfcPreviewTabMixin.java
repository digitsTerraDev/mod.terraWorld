package com.terraskills.toroidalcompat.mixin;

import com.toroidalworld.shape.WorldShapes;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.server.WorldLoader;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * World Preview TFC rebuilds a preset for its background sampler, so it bypasses
 * Toroidal World's normal Create World handoff. Apply the currently selected
 * shape while it rebuilds that preview-only set of dimensions.
 */
@Pseudo
@Mixin(targets = "com.rustysnail.world.preview.tfc.client.gui.screens.PreviewTab", remap = false)
public abstract class WorldPreviewTfcPreviewTabMixin {
    @ModifyVariable(
            method = "lambda$previewWorldCreationContext$0",
            at = @At(value = "STORE"),
            ordinal = 0
    )
    private WorldGenSettings tfcToroidal$shapePreviewDimensions(
            WorldGenSettings settings,
            WorldCreationContext creationContext,
            WorldLoader.DataLoadContext ignored) {
        return new WorldGenSettings(
                settings.options(),
                WorldShapes.applyAtCreation(creationContext.worldgenLoadContext(), settings.dimensions())
        );
    }
}
