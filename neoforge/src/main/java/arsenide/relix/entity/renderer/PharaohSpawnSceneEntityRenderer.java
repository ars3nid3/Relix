package arsenide.relix.entity.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import arsenide.relix.Relix;
import arsenide.relix.entity.PharaohSpawnSceneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PharaohSpawnSceneEntityRenderer extends EntityRenderer<PharaohSpawnSceneEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Relix.MODID,
        "textures/entity/pharaoh_spawn_scene.png"
    );

    public PharaohSpawnSceneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
        PharaohSpawnSceneEntity entity, 
        float partialTick, 
        float yaw, 
        PoseStack poseStack,
        MultiBufferSource bufferSource, 
        int packedLight
    ) {

        poseStack.pushPose();
        // A bit above the ground plane
        poseStack.translate(0, 0.1, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.tickCount + partialTick));
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.text(TEXTURE));
        float size = 1.0F;
        float halfSize = size / 2.0F;
        consumer.addVertex(matrix, -halfSize, 0.0F, -halfSize)
            .setColor(255, 255, 255, 255)
            .setUv(0.0F, 0.0F)
            .setLight(packedLight);
        consumer.addVertex(matrix, -halfSize, 0.0F, halfSize)
            .setColor(255, 255, 255, 255)
            .setUv(0.0F, 1.0F)
            .setLight(packedLight);
        consumer.addVertex(matrix, halfSize, 0.0F, halfSize)
            .setColor(255, 255, 255, 255)
            .setUv(1.0F, 1.0F)
            .setLight(packedLight);
        consumer.addVertex(matrix, halfSize, 0.0F, -halfSize)
            .setColor(255, 255, 255, 255)
            .setUv(1.0F, 0.0F)
            .setLight(packedLight);
        poseStack.popPose();
        super.render(entity, partialTick, yaw, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PharaohSpawnSceneEntity p_114482_) {
        return TEXTURE;
    }
    
    
}
