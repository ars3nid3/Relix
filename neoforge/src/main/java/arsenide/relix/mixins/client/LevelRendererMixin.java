package arsenide.relix.mixins.client;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import arsenide.relix.client.PharaohVisualController;
import arsenide.relix.client.render.PharaohSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void relix$pyramidSky(
        Matrix4f modelViewMatrix,
        Matrix4f projectionMatrix,
        float partialTick,
        Camera camera,
        boolean isFoggy,
        Runnable setupFog,
        CallbackInfo ci
    ) {
        if (PharaohVisualController.isPyramidEnabled()) {
            setupFog.run();
            PharaohSkyRenderer.render(modelViewMatrix, projectionMatrix, partialTick);
            ci.cancel(); // skip vanilla sun/moon/stars
        }
    }
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void relix$hideClouds(
        PoseStack poseStack,
        Matrix4f modelViewMatrix,
        Matrix4f projectionMatrix,
        float partialTick,
        double x,
        double y,
        double z,
        CallbackInfo ci
    ) {
        if (PharaohVisualController.isPyramidEnabled()) {
            ci.cancel();
        }
    }
}