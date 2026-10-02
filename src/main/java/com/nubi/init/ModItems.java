package com.nubi.init;

import com.nubi.NubiMod;
import com.nubi.item.CloudArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NubiMod.MOD_ID);

    public static final RegistryObject<Item> MAGIC_MOISTURE_DROP =
            ITEMS.register("magic_moisture_drop", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MAGIC_LEAD =
            ITEMS.register("magic_moisture_lead", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CLOUD_HELMET =
            ITEMS.register("cloud_helmet",
                    () -> new CloudArmorItem(ModArmorMaterials.CLOUD, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_CHESTPLATE =
            ITEMS.register("cloud_chestplate",
                    () -> new CloudArmorItem(ModArmorMaterials.CLOUD, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_LEGGINGS =
            ITEMS.register("cloud_leggings",
                    () -> new CloudArmorItem(ModArmorMaterials.CLOUD, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_BOOTS =
            ITEMS.register("cloud_boots",
                    () -> new CloudArmorItem(ModArmorMaterials.CLOUD, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> NUBI_SPAWN_EGG =
            ITEMS.register("nubi_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.NUBI, 0xF4F8FF, 0x8FC6FF, new Item.Properties()));
}
