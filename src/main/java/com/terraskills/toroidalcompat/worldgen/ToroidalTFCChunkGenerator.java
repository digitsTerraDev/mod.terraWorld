package com.terraskills.toroidalcompat.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraskills.toroidalcompat.mixin.TFCChunkGeneratorAccessor;
import com.toroidalworld.core.CarriedShape;
import com.toroidalworld.core.FlatShape;
import com.toroidalworld.core.ShapedChunkGenerator;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class ToroidalTFCChunkGenerator extends TFCChunkGenerator implements ShapedChunkGenerator {
    public static final MapCodec<ToroidalTFCChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TFCChunkGenerator.CODEC.forGetter(ToroidalTFCChunkGenerator::unshaped),
            ShapedChunkGenerator.CARRIED_CODEC.forGetter(ToroidalTFCChunkGenerator::carriedShape)
    ).apply(instance, ToroidalTFCChunkGenerator::new));

    private final CarriedShape carriedShape;

    public ToroidalTFCChunkGenerator(TFCChunkGenerator source, CarriedShape carriedShape) {
        this((BiomeSourceExtension) source.getBiomeSource(), noiseSettings(source), source.settings(), carriedShape);
    }

    private ToroidalTFCChunkGenerator(
            BiomeSourceExtension biomeSource,
            Holder<NoiseGeneratorSettings> noiseSettings,
            net.dries007.tfc.world.settings.Settings settings,
            CarriedShape carriedShape) {
        super(biomeSource, noiseSettings, settings);
        this.carriedShape = carriedShape;
        ActiveTfcFold.set(carriedShape.fold());
    }

    @Override
    public CarriedShape carriedShape() {
        return carriedShape;
    }

    @Override
    public TFCChunkGenerator unshaped() {
        return new TFCChunkGenerator((BiomeSourceExtension) getBiomeSource(), noiseSettings(this), settings());
    }

    public TFCChunkGenerator copyToroidal() {
        return new ToroidalTFCChunkGenerator(
                ((BiomeSourceExtension) getBiomeSource()).copy(),
                noiseSettings(this),
                settings(),
                carriedShape
        );
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected MapCodec<TFCChunkGenerator> codec() {
        // TFC narrows ChunkGenerator's codec return type to MapCodec<TFCChunkGenerator>.
        // The registry still needs the concrete subtype codec so saved worlds decode
        // back into this shaped generator; MapCodec is invariant, hence this bridge.
        return (MapCodec) CODEC;
    }

    private static Holder<NoiseGeneratorSettings> noiseSettings(TFCChunkGenerator generator) {
        return ((TFCChunkGeneratorAccessor) generator).tfcToroidal$noiseSettings();
    }

}
