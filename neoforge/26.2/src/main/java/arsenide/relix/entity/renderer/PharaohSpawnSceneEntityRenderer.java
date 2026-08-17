package arsenide.relix.entity.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import arsenide.relix.Relix;
import arsenide.relix.entity.PharaohSpawnSceneEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class PharaohSpawnSceneEntityRenderer extends EntityRenderer<PharaohSpawnSceneEntity, EntityRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        Relix.MODID,
        "textures/entity/pharaoh_spawn_scene.png"
    );

    public PharaohSpawnSceneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
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
        // A bit above the ground plane
        poseStack.translate(0, 0.1, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.ageInTicks));
        submitNodeCollector.submitCustomGeometry(
            poseStack,
            RenderTypes.text(TEXTURE),
            (pose, vertexConsumer) -> {
                Matrix4f matrix = pose.pose();
                float size = 1.0F;
                vertexConsumer.addVertex(matrix, -size / 2, 0, -size / 2)
                    .setColor(255, 255, 255, 255)
                    .setUv(0, 0)
                    .setLight(state.lightCoords);
                vertexConsumer.addVertex(matrix, -size / 2, 0, size / 2)
                    .setColor(255, 255, 255, 255)
                    .setUv(0, 1)
                    .setLight(state.lightCoords);
                vertexConsumer.addVertex(matrix, size / 2, 0, size / 2)
                    .setColor(255, 255, 255, 255)
                    .setUv(1, 1)
                    .setLight(state.lightCoords);
                vertexConsumer.addVertex(matrix, size / 2, 0, -size / 2)
                    .setColor(255, 255, 255, 255)
                    .setUv(1, 0)
                    .setLight(state.lightCoords);
            }
        );

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
    
    
}
