package com.nubi.events;

import com.nubi.NubiMod;
import com.nubi.item.CloudArmorItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NubiMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    /** Con la armadura de nube completa no recibes daño de caída. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && CloudArmorItem.hasFullSet(player)) {
            event.setCanceled(true);
        }
    }
}
