package com.nubi.init;

import com.nubi.NubiMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/** Armadura de nube: protege como el hierro. */
public enum ModArmorMaterials implements ArmorMaterial {
    CLOUD("cloud", 15, 2, 6, 5, 2, 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F,
            () -> Ingredient.of(ModItems.MAGIC_MOISTURE_DROP.get()));

    private final String name;
    private final int durabilityMultiplier;
    private final int helmet, chest, legs, boots;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, int helmet, int chest, int legs, int boots,
                      int enchantability, SoundEvent equipSound, float toughness, float knockbackResistance,
                      Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.helmet = helmet;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        switch (type) {
            case HELMET: return 11 * durabilityMultiplier;
            case CHESTPLATE: return 16 * durabilityMultiplier;
            case LEGGINGS: return 15 * durabilityMultiplier;
            default: return 13 * durabilityMultiplier;
        }
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        switch (type) {
            case HELMET: return helmet;
            case CHESTPLATE: return chest;
            case LEGGINGS: return legs;
            default: return boots;
        }
    }

    @Override public int getEnchantmentValue() { return enchantability; }
    @Override public SoundEvent getEquipSound() { return equipSound; }
    @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
    // Forge busca la textura en assets/nubi/textures/models/armor/cloud_layer_1.png (y _2)
    @Override public String getName() { return NubiMod.MOD_ID + ":" + name; }
    @Override public float getToughness() { return toughness; }
    @Override public float getKnockbackResistance() { return knockbackResistance; }
}
