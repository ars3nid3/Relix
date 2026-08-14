package arsenide.relix.client.render;

import java.util.Optional;
import java.util.OptionalDouble;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;

import arsenide.relix.client.PharaohVisualController;
import net.minecraft.client.Minecraft;

public class PharaohDarkenRenderer {

    public static void render() {
        if (!PharaohVisualController.isPyramidEnabled()) {
            return;
        }

        var target = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        GpuTextureView source = target.getColorTextureView();

        GpuSampler sampler =
            RenderSystem.getDevice().createSampler(
                AddressMode.CLAMP_TO_EDGE,
                AddressMode.CLAMP_TO_EDGE,
                FilterMode.LINEAR,
                FilterMode.LINEAR,
                1,
                OptionalDouble.empty()
            );

        RenderPass pass =
            RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                    () -> "pharaoh darken",
                    source,
                    Optional.empty(),
                    target.getDepthTextureView(),
                    OptionalDouble.empty()
                );


        pass.setPipeline(
            RelixRenderPipelines.PHARAOH_DARKEN
        );

        pass.bindTexture("InSampler", source, sampler);


        RenderSystem.bindDefaultUniforms(pass);


        pass.draw(
            3,
            1,
            0,
            0
        );


        pass.close();
    }
    
}
