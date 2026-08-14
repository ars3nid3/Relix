package arsenide.relix.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import arsenide.relix.client.PharaohVisualController;
import arsenide.relix.client.RelixClient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.client.CustomEnvironmentEffectsRendererManager;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;

@Mixin(CustomEnvironmentEffectsRendererManager.class)
public class CustomEnvironmentEffectsRendererManagerMixin {
    
    @Inject(
        method = "getCustomSkyboxRenderer(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;)Lnet/neoforged/neoforge/client/CustomSkyboxRenderer;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void pharaohSkyOverride(
        Level level,
        Vec3 position,
        CallbackInfoReturnable<CustomSkyboxRenderer> cir
    ) {
        if (PharaohVisualController.isPyramidEnabled()) {
            cir.setReturnValue(RelixClient.PHARAOH_SKY);
        }
    }

    @Inject(
        method = "getCustomCloudsRenderer(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;)Lnet/neoforged/neoforge/client/CustomCloudsRenderer;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void pharaohCloudOverride(
        Level level,
        Vec3 position,
        CallbackInfoReturnable<CustomCloudsRenderer> cir
    ) {
        if (PharaohVisualController.isPyramidEnabled()) {
            cir.setReturnValue(RelixClient.BOSS_CLOUD);
        }
    }
}
