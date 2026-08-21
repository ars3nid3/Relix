package arsenide.relix.client.render;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;

public class PharaohSkyRenderer implements CustomSkyboxRenderer {

        private void assemblePyramid(BufferBuilder builder) {
            float size = 40.0F;
            float height = 40.0F;

            // Vertex assembly
            // Front wall
            builder.addVertex(0, height, 0)
                .setUv(0.5F, 0)
                .setColor(0xFFFFD080);
            builder.addVertex(size, -size, -size)
                .setUv(1, 1)                
                .setColor(0xFFC08040);
            builder.addVertex(-size, -size, -size)
                .setUv(0, 1)
                .setColor(0xFFC08040);

            // Right wall
            builder.addVertex(0, height, 0)
                .setUv(0.5F, 0)
                .setColor(0xFFFFD080);
            builder.addVertex(size, -size, size)
                .setUv(1, 1)
                .setColor(0xFFD09050);
            builder.addVertex(size, -size, -size)
                .setUv(0, 1)
                .setColor(0xFFD09050);

            // Back wall
            builder.addVertex(0, height, 0)
                .setUv(0.5F, 0)
                .setColor(0xFFFFD080);
            builder.addVertex(-size, -size, size)
                .setUv(1, 1)
                .setColor(0xFFB07030);
            builder.addVertex(size, -size, size)
                .setUv(0, 1)
                .setColor(0xFFB07030);

            // Left wall
            builder.addVertex(0, height, 0)
                .setUv(0.5F, 0)
                .setColor(0xFFFFD080);
            builder.addVertex(-size, -size, -size)
                .setUv(1, 1)
                .setColor(0xFFE0A050);
            builder.addVertex(-size, -size, size)
                .setUv(0, 1)
                .setColor(0xFFE0A050);
        }

        private void assemblePyramidEdges(BufferBuilder builder, float lineWidth) {
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

                builder.addVertex(e[0], e[1], e[2])
                    .setColor(color)
                    .setNormal(nx, ny, nz)
                    .setLineWidth(lineWidth);
                builder.addVertex(e[3], e[4], e[5])
                    .setColor(color)
                    .setNormal(nx, ny, nz)
                    .setLineWidth(lineWidth);
            }
        }

        @Override
        public boolean renderSky(
            LevelRenderState levelRenderState, 
            SkyRenderState skyRenderState,
            Matrix4fc modelViewMatrix, 
            Runnable setupFog
        ) {
            // setupFog.run();
            int skyColor = skyRenderState.skyColor;

            // Get required info from current render target
            var renderTarget = Minecraft.getInstance().getMainRenderTarget();
            GpuTextureView colorTexture = renderTarget.getColorTextureView();
            GpuTextureView depthTextureView = renderTarget.getDepthTextureView();

            // Build a buffer to hold exactly 12 vertices
            ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize() * 12
            );

            // Here we specify the buffer contains vertices that automatically assemble into triangles
            // And that they only require position and color
            BufferBuilder buffer = new BufferBuilder(
                byteBufferBuilder,
                Mode.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX_COLOR
            );
            assemblePyramid(buffer);
            // Build vertices into a mesh (opr throw an error)
            MeshData mesh = buffer.buildOrThrow();
            
            // Pass the instructions of the mesh to the GPU
            GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(
                () -> "pyramid sky", 
                GpuBuffer.USAGE_VERTEX, 
                mesh.vertexBuffer()
            );

            // Binds dynamic transforms (required by sky pipeline, provides matrix and color)
            GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                ARGB.vector4fFromARGB32(skyColor),
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Matrix4f()
            );
            
            RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                    () -> "pyramid sky",
                    colorTexture,
                    OptionalInt.empty(),
                    depthTextureView,
                    OptionalDouble.empty()
                );

            // Defines the constraints of the rendering pipeline (aka which vertex and fragment shader, expected uniforms, vertexbinding) 
            pass.setPipeline(RelixRenderPipelines.PHARAOH_SKY);

            // Binds default uniforms (required by most pipelines)
            RenderSystem.bindDefaultUniforms(pass);

            pass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind the vertex buffer to the correct index
            pass.setVertexBuffer(0, gpuBuffer);

            // Perform the render
            pass.draw(0, mesh.drawState().vertexCount());

            pass.close();

            gpuBuffer.close();
            mesh.close();
            byteBufferBuilder.close();

            // Render edges of the pyramid (LINES duplicates each vertex, so 16 logical -> 32 buffer verts)
            ByteBufferBuilder lineByteBuffer = ByteBufferBuilder.exactlySized(
                DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH.getVertexSize() * 32
            );

            BufferBuilder lineBuilder = new BufferBuilder(
                lineByteBuffer,
                Mode.LINES,
                DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH
            );

            float lineWidth = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
            assemblePyramidEdges(lineBuilder, lineWidth);
            
            MeshData lineMesh = lineBuilder.buildOrThrow();
            
            GpuBuffer lineGpuBuffer = RenderSystem.getDevice().createBuffer(
                () -> "pyramid edges",
                GpuBuffer.USAGE_VERTEX,
                lineMesh.vertexBuffer()
            );

            RenderPass linePass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                () -> "pyramid edges",
                colorTexture,
                OptionalInt.empty(),
                depthTextureView,
                OptionalDouble.empty()
            );
        
            linePass.setPipeline(RelixRenderPipelines.PHARAOH_SKY_LINES);
            
            RenderSystem.bindDefaultUniforms(linePass);
            
            GpuBufferSlice lineTransforms =
                RenderSystem.getDynamicUniforms().writeTransform(
                    RenderSystem.getModelViewMatrix(),
                    ARGB.vector4fFromARGB32(skyColor),
                    new Vector3f(0.0F, 0.0F, 0.0F),
                    new Matrix4f()
                );
            
            linePass.setUniform("DynamicTransforms", lineTransforms);
            
            linePass.setVertexBuffer(0, lineGpuBuffer);

            int indexCount = lineMesh.drawState().indexCount();
            var sequentialIndices = RenderSystem.getSequentialBuffer(Mode.LINES);
            linePass.setIndexBuffer(sequentialIndices.getBuffer(indexCount), sequentialIndices.type());
            linePass.drawIndexed(0, 0, indexCount, 1);
            
            linePass.close();

            lineGpuBuffer.close();
            lineMesh.close();
            lineByteBuffer.close();
            
            // Signal to not render anything else of the sky
            return true;
        }
}
