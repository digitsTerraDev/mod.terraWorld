package com.terraskills.toroidalcompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.terraskills.toroidalcompat.worldgen.TfcCoordinateFold;
import com.terraskills.toroidalcompat.worldgen.TfcTopology;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OverworldClimateModel.class, remap = false)
public abstract class OverworldClimateModelMixin {
    @Shadow @Final protected float temperatureScale;

    @ModifyReturnValue(method = "hemisphereScale", at = @At("RETURN"))
    private float tfcToroidal$effectiveHemisphereScale(float original) {
        return TfcTopology.climateScaleBlocks(Direction.Axis.Z, Math.round(original));
    }

    @ModifyVariable(
            method = {
                    "getAverageTemperature", "getInstantTemperature", "getAverageRainfall",
                    "getRainfallVariance", "getInstantRainfall", "getBaseGroundwater",
                    "getAverageGroundwater", "getInstantGroundwater", "getFog", "getWind"
            },
            at = @At("HEAD"), argsOnly = true)
    private BlockPos tfcToroidal$foldClimatePosition(BlockPos pos) {
        return new BlockPos(
                TfcCoordinateFold.block(pos.getX(), Direction.Axis.X),
                pos.getY(),
                TfcCoordinateFold.block(pos.getZ(), Direction.Axis.Z));
    }

    @ModifyExpressionValue(
            method = "getInstantTemperature",
            at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/calendar/Month;getTemperatureModifier()F"))
    private float tfcToroidal$reverseSouthernSeason(
            float modifier, LevelReader level, BlockPos pos, long calendarTick, int daysInMonth) {
        return TfcTopology.isNorthernHemisphere(pos.getZ()) ? modifier : -modifier;
    }

    @Inject(method = "calculateMonthlyTemperature", at = @At("HEAD"), cancellable = true)
    private void tfcToroidal$twoPoleMonthlyTemperature(
            int z, float monthTemperatureModifier, CallbackInfoReturnable<Float> cir) {
        final float polarFactor = TfcTopology.polarClimateFactor(z);
        if (!Float.isNaN(polarFactor)) {
            cir.setReturnValue(-18f * polarFactor * monthTemperatureModifier);
        }
    }
}
