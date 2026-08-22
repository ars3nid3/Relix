package arsenide.relix.client.render;

import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
public class PharaohSkyRenderer {

    private static void assemblePyramid(BufferBuilder builder, Matrix4f pose) {
        float size = 40.0F;
        float height = 40.0F;

        // Vertex assembly
        // Front wall
        builder.addVertex(pose, 0, height, 0)
            .setUv(0.5F, 0)
            .setColor(0xFFFFD080);
        builder.addVertex(pose, size, -size, -size)
            .setUv(1, 1)                
            .setColor(0xFFC08040);
        builder.addVertex(pose, -size, -size, -size)
            .setUv(0, 1)
            .setColor(0xFFC08040);

        // Right wall
        builder.addVertex(pose, 0, height, 0)
            .setUv(0.5F, 0)
            .setColor(0xFFFFD080);
        builder.addVertex(pose, size, -size, size)
            .setUv(1, 1)
            .setColor(0xFFD09050);
        builder.addVertex(pose, size, -size, -size)
            .setUv(0, 1)
            .setColor(0xFFD09050);

        // Back wall
        builder.addVertex(pose, 0, height, 0)
            .setUv(0.5F, 0)
            .setColor(0xFFFFD080);
        builder.addVertex(pose, -size, -size, size)
            .setUv(1, 1)
            .setColor(0xFFB07030);
        builder.addVertex(pose, size, -size, size)
            .setUv(0, 1)
            .setColor(0xFFB07030);

        // Left wall
        builder.addVertex(pose, 0, height, 0)
            .setUv(0.5F, 0)
            .setColor(0xFFFFD080);
        builder.addVertex(pose, -size, -size, -size)
            .setUv(1, 1)
            .setColor(0xFFE0A050);
        builder.addVertex(pose, -size, -size, size)
            .setUv(0, 1)
            .setColor(0xFFE0A050);
    }

    private static void assemblePyramidEdges(BufferBuilder builder, Matrix4f pose) {
        float size = 40.0F;
        float height = 40.0F;
        int color = 0xFFCCA666;

        float[][] edges = {
            {0, height, 0, size, -size, -size},
            {0, height, 0, -size, -size, -size},
            {0, height, 0, size, -size, size},
            {0, height, 0, -size, -size, size},
            {size, -size, -size, -size, -size, -size},
            {-size, -size, -size, -size, -size, size},
            {-size, -size, size, size, -size, size},
            {size, -size, size, size, -size, -size}
        };

        for (float[] e : edges) {
            float dx = e[3] - e[0];
            float dy = e[4] - e[1];
            float dz = e[5] - e[2];
            float length = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
            float nx = dx / length;
            float ny = dy / length;
            float nz = dz / length;

            builder.addVertex(pose, e[0], e[1], e[2])
                .setColor(color)
                .setNormal(nx, ny, nz);
            builder.addVertex(pose, e[3], e[4], e[5])
                .setColor(color)
                .setNormal(nx, ny, nz);
        }
    }

    public static void render(
        Matrix4f modelViewMatrix,
        Matrix4f projectionMatrix,
        float partialTick
    ) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);

        Matrix4f pose = poseStack.last().pose();
        Minecraft mc = Minecraft.getInstance();
        Tesselator tesselator = Tesselator.getInstance();
        ShaderInstance skyShader = RelixShaders.pharaohSky;
        if (skyShader != null) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> skyShader);
            skyShader.setDefaultUniforms(
                Mode.TRIANGLES, 
                pose, 
                projectionMatrix, 
                mc.getWindow()
            );
            BufferBuilder builder = tesselator.begin(
                Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX_COLOR
            );
            assemblePyramid(builder, pose);
            BufferUploader.drawWithShader(builder.buildOrThrow());
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
        ShaderInstance lineShader = RelixShaders.pharaohSkyLines;
        if (lineShader != null) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> lineShader);
            lineShader.setDefaultUniforms(
                Mode.LINES,
                pose,
                projectionMatrix,
                mc.getWindow()
            );
            BufferBuilder lineBuilder = tesselator.begin(
                Mode.LINES,
                DefaultVertexFormat.POSITION_COLOR_NORMAL
            );
            assemblePyramidEdges(lineBuilder, pose);
            BufferUploader.drawWithShader(lineBuilder.buildOrThrow());
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }
}
