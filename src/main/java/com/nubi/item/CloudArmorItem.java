package com.nubi.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class CloudArmorItem extends ArmorItem {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public CloudArmorItem(ArmorMaterial material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    /** true si la entidad lleva las 4 piezas de la armadura de nube. */
    public static boolean hasFullSet(LivingEntity entity) {
        for (EquipmentSlot slot : SLOTS) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof CloudArmorItem)) {
                return false;
            }
        }
        return true;
    }
}
