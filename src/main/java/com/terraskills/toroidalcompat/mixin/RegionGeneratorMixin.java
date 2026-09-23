package com.terraskills.toroidalcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.terraskills.toroidalcompat.worldgen.TfcCoordinateFold;
import com.terraskills.toroidalcompat.worldgen.TfcTopology;
import com.terraskills.toroidalcompat.worldgen.RegionGeneratorBridge;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.Units;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegionGenerator.class, remap = false)
public abstract class RegionGeneratorMixin implements RegionGeneratorBridge {
    @Shadow @Final public Cellular2D cellNoise;
    @Shadow @Final @Mutable public Noise2D continentNoise;
    @Shadow @Final @Mutable public Noise2D temperatureNoise;
    @Shadow @Final @Mutable public Noise2D oceanicInfluenceNoise;
    @Shadow @Final @Mutable public Noise2D rainfallNoise;
    @Shadow @Final @Mutable public Noise2D rainfallVarianceNoise;
    @Shadow @Final @Mutable public Noise2D hotSpotAgeNoise;
    @Shadow @Final @Mutable public Noise2D hotSpotIntensityNoise;

    @Shadow
    private Region getOrCreateRegion(Cellular2D.Cell cell) {
        throw new AssertionError();
    }

    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/world/settings/Settings;temperatureScale()I"))
    private int tfcToroidal$effectiveTemperatureScale(Settings settings) {
        return TfcTopology.climateScaleBlocks(Direction.Axis.Z, settings.temperatureScale());
    }

    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/world/settings/Settings;rainfallScale()I"))
    private int tfcToroidal$effectiveRainfallScale(Settings settings) {
        return TfcTopology.climateScaleBlocks(Direction.Axis.X, settings.rainfallScale());
    }

    @ModifyReturnValue(method = "baseNoise", at = @At("RETURN"))
    private static Noise2D tfcToroidal$alignTemperaturePole(
            Noise2D original, boolean axisIsX, float scale, float constant) {
        if (axisIsX || !TfcTopology.mirrorsSouthernHemisphere()) return original;
        return (x, z) -> TfcTopology.polarClimateNoiseGrid(z);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tfcToroidal$makeRegionalFieldsPeriodic(Settings settings, Seed seed, CallbackInfo ci) {
        if (!TfcTopology.active()) return;
        continentNoise = periodic(continentNoise);
        temperatureNoise = periodic(temperatureNoise);
        oceanicInfluenceNoise = periodic(oceanicInfluenceNoise);
        rainfallNoise = periodic(rainfallNoise);
        rainfallVarianceNoise = periodic(rainfallVarianceNoise);
        hotSpotAgeNoise = periodic(hotSpotAgeNoise);
        hotSpotIntensityNoise = periodic(hotSpotIntensityNoise);
    }

    @ModifyVariable(method = "getOrCreateRegionPoint", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int tfcToroidal$foldRegionGridX(int gridX) {
        return TfcCoordinateFold.grid(gridX, Direction.Axis.X);
    }

    @ModifyVariable(method = "getOrCreateRegionPoint", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int tfcToroidal$foldRegionGridZ(int gridZ) {
        return TfcCoordinateFold.grid(gridZ, Direction.Axis.Z);
    }

    @ModifyVariable(method = "getOrCreatePartitionPoint", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int tfcToroidal$foldRiverGridX(int gridX) {
        return TfcCoordinateFold.grid(gridX, Direction.Axis.X);
    }

    @ModifyVariable(method = "getOrCreatePartitionPoint", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int tfcToroidal$foldRiverGridZ(int gridZ) {
        return TfcCoordinateFold.grid(gridZ, Direction.Axis.Z);
    }

    @ModifyVariable(method = "getOrCreateRegion(II)Lnet/dries007/tfc/world/region/Region;", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int tfcToroidal$foldDirectRegionGridX(int gridX) {
        return TfcCoordinateFold.grid(gridX, Direction.Axis.X);
    }

    @ModifyVariable(method = "getOrCreateRegion(II)Lnet/dries007/tfc/world/region/Region;", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int tfcToroidal$foldDirectRegionGridZ(int gridZ) {
        return TfcCoordinateFold.grid(gridZ, Direction.Axis.Z);
    }

    @ModifyVariable(method = "sampleCell", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int tfcToroidal$foldCellGridX(int gridX) {
        return TfcCoordinateFold.grid(gridX, Direction.Axis.X);
    }

    @ModifyVariable(method = "sampleCell", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int tfcToroidal$foldCellGridZ(int gridZ) {
        return TfcCoordinateFold.grid(gridZ, Direction.Axis.Z);
    }

    @Override
    public Region.Point tfcToroidal$getOrCreateRegionPointUnwrapped(int gridX, int gridZ) {
        // Periodic interpolation samples a second copy of a point across the
        // seam. TFC's Region stores only its canonical 4x4 grid coordinates,
        // so fold that copy before indexing its fixed-size point array.
        final int foldedX = TfcCoordinateFold.grid(gridX, Direction.Axis.X);
        final int foldedZ = TfcCoordinateFold.grid(gridZ, Direction.Axis.Z);
        return ((RegionAccessor) (Object) getOrCreateRegion(cellNoise.cell(foldedX, foldedZ)))
                .tfcToroidal$atOrThrow(foldedX, foldedZ);
    }

    private static Noise2D periodic(Noise2D source) {
        return (x, z) -> TfcTopology.samplePeriodicGrid(x, z, source::noise);
    }
}
