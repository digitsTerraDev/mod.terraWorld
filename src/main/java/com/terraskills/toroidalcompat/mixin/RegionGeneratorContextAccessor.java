package com.terraskills.toroidalcompat.mixin;

import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiConsumer;

@Mixin(targets = "net.dries007.tfc.world.region.RegionGenerator$Context", remap = false)
public interface RegionGeneratorContextAccessor {
    @Invoker("<init>")
    static RegionGenerator.Context tfcToroidal$create(
            RegionGenerator generator, BiConsumer<RegionGenerator.Task, Region> viewer,
            Cellular2D.Cell cell, Seed seed) {
        throw new AssertionError();
    }

    @Invoker("runTasks")
    RegionGenerator.Context tfcToroidal$runTasks();
}
