package arsenide.relix.client.render;

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

public class PharaohDarkenRenderer {

    public static void render() {
        if (!PharaohVisualController.isPyramidEnabled()) {
            return;
        }

        ShaderInstance shader = RelixShaders.darkenOverlay;
        if (shader == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        RenderTarget target = mc.getMainRenderTarget();

        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        target.bindWrite(false);
        shader.setSampler("DiffuseSampler", target.getColorTextureId());
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
        GlStateManager._colorMask(true, true, true, true);
    }
    
}
