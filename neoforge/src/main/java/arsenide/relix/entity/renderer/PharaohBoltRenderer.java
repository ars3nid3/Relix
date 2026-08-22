package arsenide.relix.entity.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import arsenide.relix.Relix;
import arsenide.relix.entity.PharaohBoltEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class PharaohBoltRenderer extends EntityRenderer<PharaohBoltEntity> {

    public static final int FRAME_COUNT = 8;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Relix.MODID,
        "textures/entity/pharaoh_bolt.png"
    );
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    public PharaohBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(PharaohBoltEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void render(
        PharaohBoltEntity entity, 
        float partialTick, 
        float yaw, 
        PoseStack poseStack, 
        MultiBufferSource bufferSource, 
        int packedLight
    ) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        int frame = (((int) entity.tickCount) / 2) % FRAME_COUNT;
        float frameWidth = 1.0F / FRAME_COUNT;
        float u0 = frame * frameWidth;
        float u1 = u0 + frameWidth;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RENDER_TYPE);
        float size = 0.45F;
        float halfSize = size / 2.0F;
        consumer.addVertex(matrix, -halfSize, -halfSize, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(u0, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, -halfSize, halfSize, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(u0, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, halfSize, halfSize, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(u1, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        consumer.addVertex(matrix, halfSize, -halfSize, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(u1, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, partialTick, yaw, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PharaohBoltEntity entity) {
        return TEXTURE;
    }
}
