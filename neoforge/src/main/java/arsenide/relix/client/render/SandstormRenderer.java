package arsenide.relix.client.render;

import org.joml.Matrix4f;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import arsenide.relix.client.PharaohVisualController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;

public class SandstormRenderer {
    
    public static void render(Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        float intensity = PharaohVisualController.getSandStormIntensity();
        if (intensity <= 0.0F) {
            return;
        }

        ShaderInstance shader = RelixShaders.sandstorm;
        if (shader == null) return;

        Minecraft mc = Minecraft.getInstance();
        RenderTarget target = mc.getMainRenderTarget();
        RenderSystem.assertOnRenderThread();
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA.value,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value,
            GlStateManager.SourceFactor.ONE.value,
            GlStateManager.DestFactor.ZERO.value
        );
        target.bindWrite(false);

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        if (shader.getUniform("CameraPos") != null) {
            shader.getUniform("CameraPos").set(
                (float) cam.x,
                (float) cam.y,
                (float) cam.z
            );
        }
        if (shader.getUniform("Intensity") != null) {
            shader.getUniform("Intensity").set(intensity);
        }

        shader.setSampler("DepthSampler", target.getDepthTextureId());
        shader.setDefaultUniforms(
            Mode.QUADS,
            modelViewMatrix,
            projectionMatrix,
            mc.getWindow()
        );
        shader.apply();

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(
            Mode.QUADS,
            DefaultVertexFormat.BLIT_SCREEN
        );
        builder.addVertex(0.0F, 0.0F, 0.0F);
        builder.addVertex(1.0F, 0.0F, 0.0F);
        builder.addVertex(1.0F, 1.0F, 0.0F);
        builder.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.draw(builder.buildOrThrow());
        shader.clear();
        GlStateManager._depthMask(true);
        GlStateManager._disableBlend();
        
    }
}
