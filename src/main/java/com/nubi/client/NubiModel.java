package com.nubi.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nubi.NubiMod;
import com.nubi.entity.NubiEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo de la Nubi: cuerpo redondeado hecho de cubos (cuerpo + 4 "bolitas" de nube).
 * La cara (ojos, rubor y boca) está pintada en la cara frontal del cuerpo de la textura.
 * Textura: 64x64.
 */
public class NubiModel extends EntityModel<NubiEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(NubiMod.MOD_ID, "nubi"), "main");

    private final ModelPart root;
    private final ModelPart body;

    public NubiModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        PartDefinition body = parts.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -10.0F, -6.0F, 12.0F, 10.0F, 12.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("puff_left",
                CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(-7.0F, -4.0F, 0.0F));
        body.addOrReplaceChild("puff_right",
                CubeListBuilder.create().texOffs(32, 24).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(7.0F, -4.0F, 0.0F));
        body.addOrReplaceChild("puff_top",
                CubeListBuilder.create().texOffs(0, 40).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -10.0F, 1.0F));
        body.addOrReplaceChild("puff_back",
                CubeListBuilder.create().texOffs(32, 40).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -4.0F, 6.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(NubiEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Suave vaivén flotando
        this.body.y = 24.0F + Mth.sin(ageInTicks * 0.12F) * 1.2F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
