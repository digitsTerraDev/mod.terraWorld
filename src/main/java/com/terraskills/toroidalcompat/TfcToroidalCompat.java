package com.terraskills.toroidalcompat;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.terraskills.toroidalcompat.client.ClientClimateTopology;
import com.terraskills.toroidalcompat.config.ToroidalCompatClientConfig;
import com.terraskills.toroidalcompat.client.ToroidalCompatClient;
import com.terraskills.toroidalcompat.worldgen.ToroidalTFCChunkGenerator;
import com.terraskills.toroidalcompat.worldgen.ActiveTfcFold;
import com.toroidalworld.api.v1.ToroidalShape;
import com.toroidalworld.api.v1.ToroidalWorldApi;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

import java.util.Optional;

@Mod(TfcToroidalCompat.MOD_ID)
public final class TfcToroidalCompat {
    public static final String MOD_ID = "tfc_toroidal_compat";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);

    static {
        CHUNK_GENERATORS.register("tfc", () -> ToroidalTFCChunkGenerator.CODEC);
    }

    public TfcToroidalCompat(IEventBus modBus, ModContainer container) {
        CHUNK_GENERATORS.register(modBus);
        NeoForge.EVENT_BUS.register(this);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            container.registerConfig(ModConfig.Type.CLIENT, ToroidalCompatClientConfig.SPEC);
            ToroidalCompatClient.register(modBus, container);
            ClientClimateTopology.init();
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        final ServerLevel overworld = event.getServer().overworld();
        final Optional<ToroidalShape> shape = ToroidalWorldApi.shapeOf(overworld);
        if (shape.isEmpty()) {
            ActiveTfcFold.clear();
            LOGGER.warn("TFC Toroidal Compatibility is loaded but inactive: this save has no Toroidal World shape. "
                    + "Choose a Toroidal or Cylinder shape while creating a new world; existing saves cannot be converted.");
            return;
        }

        final ToroidalShape active = shape.orElseThrow();
        final ChunkGenerator generator = overworld.getChunkSource().getGenerator();
        if (!(generator instanceof ToroidalTFCChunkGenerator)) {
            LOGGER.error("Toroidal shape is active, but the live overworld generator is {} instead of {}. "
                            + "World generation will not stitch.", generator.getClass().getName(),
                    ToroidalTFCChunkGenerator.class.getName());
            return;
        }
        LOGGER.info("TFC Toroidal Compatibility active with live generator {}: X={}, Z={}",
                generator.getClass().getSimpleName(), describe(active, Direction.Axis.X),
                describe(active, Direction.Axis.Z));
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        ActiveTfcFold.clear();
    }

    private static String describe(ToroidalShape shape, Direction.Axis axis) {
        return shape.loops(axis) ? shape.widthBlocks(axis) + " blocks" : "unbounded";
    }
}
