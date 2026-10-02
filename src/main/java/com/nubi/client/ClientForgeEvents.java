package com.nubi.client;

import com.nubi.NubiMod;
import com.nubi.item.CloudArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NubiMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    /**
     * Mientras mantienes la tecla y llevas la armadura de nube completa, planeas suavemente.
     * Bajamos a -0.05 bloques/tick (no a 0) a propósito: así los servidores sin "allow-flight"
     * no expulsan al jugador por "flotar demasiado tiempo".
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.isPaused()) return;
        if (!KeyMappings.FLOAT.isDown() || !CloudArmorItem.hasFullSet(player)) return;
        if (player.onGround() || player.isInWater() || player.isInLava() || player.isPassenger()
                || player.getAbilities().flying || player.isFallFlying()) return;

        Vec3 motion = player.getDeltaMovement();
        if (motion.y < -0.05D) {
            player.setDeltaMovement(motion.x, -0.05D, motion.z);
        }
        player.fallDistance = 0.0F;

        if (player.tickCount % 3 == 0) {
            player.level().addParticle(ParticleTypes.CLOUD,
                    player.getX(), player.getY(), player.getZ(), 0.0D, -0.02D, 0.0D);
        }
    }
}
