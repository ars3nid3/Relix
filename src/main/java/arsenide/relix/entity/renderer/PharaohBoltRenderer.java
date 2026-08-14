package arsenide.relix.entity.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import arsenide.relix.Relix;
import arsenide.relix.entity.PharaohBoltEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class PharaohBoltRenderer extends EntityRenderer<PharaohBoltEntity, EntityRenderState> {

    public static final int FRAME_COUNT = 8;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        Relix.MODID,
        "textures/entity/pharaoh_bolt.png"
    );
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucent(TEXTURE);

    public PharaohBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(PharaohBoltEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(
        EntityRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);

        int frame = (((int) state.ageInTicks) / 2) % FRAME_COUNT;
        float frameWidth = 1.0F / FRAME_COUNT;
        float u0 = frame * frameWidth;
        float u1 = u0 + frameWidth;

        submitNodeCollector.submitCustomGeometry(
            poseStack,
            RENDER_TYPE,
            (pose, vertexConsumer) -> {
                Matrix4f matrix = pose.pose();
                float size = 0.45F;
                float half = size / 2.0F;

                vertexConsumer.addVertex(matrix, -half, -half, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(u0, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(state.lightCoords)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
                vertexConsumer.addVertex(matrix, -half, half, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(u0, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(state.lightCoords)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
                vertexConsumer.addVertex(matrix, half, half, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(u1, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(state.lightCoords)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
                vertexConsumer.addVertex(matrix, half, -half, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(u1, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(state.lightCoords)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
            }
        );

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
