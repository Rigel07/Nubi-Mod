package com.nubi.init;

import com.nubi.NubiMod;
import com.nubi.entity.NubiEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NubiMod.MOD_ID);

    public static final RegistryObject<EntityType<NubiEntity>> NUBI = ENTITIES.register("nubi",
            () -> EntityType.Builder.of(NubiEntity::new, MobCategory.CREATURE)
                    .sized(1.1F, 0.9F)
                    .clientTrackingRange(8)
                    .build("nubi"));
}
