package com.nubi.client;

import com.nubi.NubiMod;
import com.nubi.entity.NubiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NubiRenderer extends MobRenderer<NubiEntity, NubiModel> {
    private static final ResourceLocation[] TEXTURES = {
            tex("nubi_white"), tex("nubi_pink"), tex("nubi_blue"), tex("nubi_yellow")
    };
    private static final ResourceLocation STORM = tex("nubi_storm");

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(NubiMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    public NubiRenderer(EntityRendererProvider.Context context) {
        super(context, new NubiModel(context.bakeLayer(NubiModel.LAYER)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(NubiEntity entity) {
        if (entity.isStormy()) return STORM;
        return TEXTURES[entity.getVariant().getId()];
    }
}
