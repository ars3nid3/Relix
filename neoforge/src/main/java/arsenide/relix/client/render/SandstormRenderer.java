package arsenide.relix.client.render;

import java.util.Optional;
import java.util.OptionalDouble;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;

import arsenide.relix.client.PharaohVisualController;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;

public class SandstormRenderer {
    
    public static void render(Matrix4fc modelViewMatrix) {
        if (PharaohVisualController.getSandStormIntensity() <= 0.0F) {
            return;
        }

        var target = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        GpuTextureView depthTexture = target.getDepthTextureView();

        GpuSampler sampler = RenderSystem.getDevice().createSampler(
            AddressMode.CLAMP_TO_EDGE,
            AddressMode.CLAMP_TO_EDGE,
            FilterMode.NEAREST,
            FilterMode.NEAREST,
            1,
            OptionalDouble.empty()
        );

        RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                () -> "sandstorm",
                target.getColorTextureView(),
                Optional.empty(),
                depthTexture,
                OptionalDouble.empty()
            );

        pass.setPipeline(RelixRenderPipelines.SANDSTORM);

        pass.bindTexture("DepthSampler", depthTexture, sampler);

        RenderSystem.bindDefaultUniforms(pass);

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
            new Matrix4f(modelViewMatrix),
            ARGB.vector4fFromARGB32(0xFFFFFFFF)
        );
        pass.setUniform("DynamicTransforms", dynamicTransforms);

        pass.draw(3, 1, 0, 0);

        pass.close();
        
    }
}
