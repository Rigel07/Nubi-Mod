package com.nubi.init;

import com.nubi.NubiMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class ModBiomes {
    /** Bioma "Mundo Nubi" (definido en data/nubi/worldgen/biome/mundo_nubi.json). */
    public static final ResourceKey<Biome> MUNDO_NUBI =
            ResourceKey.create(Registries.BIOME, new ResourceLocation(NubiMod.MOD_ID, "mundo_nubi"));
}
