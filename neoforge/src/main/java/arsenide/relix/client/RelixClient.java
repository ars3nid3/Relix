package arsenide.relix.client;

import arsenide.relix.Relix;
import arsenide.relix.client.camera.ScreenShake;
import arsenide.relix.client.render.BossCloudRenderer;
import arsenide.relix.client.render.PharaohDarkenRenderer;
import arsenide.relix.client.render.PharaohSkyRenderer;
import arsenide.relix.client.render.RelixRenderPipelines;
import arsenide.relix.client.render.SandstormRenderer;
import arsenide.relix.entity.RelixEntities;
import arsenide.relix.entity.renderer.AttendantEntityRenderer;
import arsenide.relix.entity.renderer.PharaohBoltRenderer;
import arsenide.relix.entity.renderer.PharaohEntityRenderer;
import arsenide.relix.entity.renderer.PharaohSpawnSceneEntityRenderer;
import arsenide.relix.entity.renderer.layers.PharaohHeaddressLayer;
import arsenide.relix.entity.renderer.models.PharaohModel;
import arsenide.relix.menu.RelixMenus;
import arsenide.relix.menu.TabletTableScreen;
import arsenide.relix.world.RelixAttachments;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Relix.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Relix.MODID, value = Dist.CLIENT)
public class RelixClient {

    public static final PharaohSkyRenderer PHARAOH_SKY = new PharaohSkyRenderer();

    public static final BossCloudRenderer BOSS_CLOUD = new BossCloudRenderer();

    public static final ContextKey<Boolean> HAS_HEADDRESS = new ContextKey<>(
        Identifier.fromNamespaceAndPath(
            Relix.MODID,
            "has_headdress"
        )
    );

    public RelixClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        RelixRenderPipelines.PHARAOH_SKY.getLocation();
    }

    

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(
            RelixMenus.TABLET_TABLE_MENU.get(),
            TabletTableScreen::new
        );
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
            PharaohModel.PHARAOH_LAYER,
            PharaohModel::createBodyLayer
        );
        event.registerLayerDefinition(
            PharaohHeaddressLayer.FELINE_HEADDRESS,
            PharaohHeaddressLayer::createFelineHeaddressLayer
        );
        event.registerLayerDefinition(
            PharaohHeaddressLayer.CAT_HEADDRESS,
            PharaohHeaddressLayer::createCatHeaddressLayer
        );
        event.registerLayerDefinition(
            PharaohHeaddressLayer.FELINE_BABY_HEADDRESS,
            PharaohHeaddressLayer::createFelineBabyHeaddressLayer
        );
        event.registerLayerDefinition(
            PharaohHeaddressLayer.CAT_BABY_HEADDRESS,
            PharaohHeaddressLayer::createCatBabyHeaddressLayer
        );
    }

    @SubscribeEvent
    public static void onRegisterEntities(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
            RelixEntities.PHARAOH_SPAWN_SCENE_ENTITY.get(),
            PharaohSpawnSceneEntityRenderer::new
        );
        event.registerEntityRenderer(
            RelixEntities.ATTENDANT_ENTITY.get(),
            AttendantEntityRenderer::new
        );
        event.registerEntityRenderer(
            RelixEntities.PHARAOH_ENTITY.get(),
            PharaohEntityRenderer::new
        );
        event.registerEntityRenderer(
            RelixEntities.PHARAOH_BOLT_ENTITY.get(),
            PharaohBoltRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerRenderers(RegisterCustomEnvironmentEffectRendererEvent event) {
        event.registerSkyboxRenderer(
            Identifier.fromNamespaceAndPath(Relix.MODID, "pharaoh_sky"),
            PHARAOH_SKY
        );
        event.registerCloudRenderer(
            Identifier.fromNamespaceAndPath(
                Relix.MODID,
                "boss_cloud"
            ),
            BOSS_CLOUD
        );
    }

    @SubscribeEvent
    public static void onRenderLevelAfterSky(RenderLevelStageEvent.AfterLevel event) {
        PharaohDarkenRenderer.render();
        SandstormRenderer.render(event.getModelViewMatrix());
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (PharaohVisualController.isPyramidEnabled()) {
            // #413822
            event.setRed(0.2549F);
            event.setGreen(0.2196F);
            event.setBlue(0.1333F);
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float intensity = ScreenShake.getIntensity();
        if (intensity <= 0) return;

        event.setYaw(event.getYaw() + ScreenShake.getRandomOffset(intensity));
        event.setPitch(event.getPitch() + ScreenShake.getRandomOffset(intensity));
        event.setRoll(event.getRoll() + ScreenShake.getRandomOffset(intensity));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ScreenShake.tick();
    }

    @SubscribeEvent
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
            CatRenderer.class,
            (cat, state) -> state.setRenderData(HAS_HEADDRESS, RelixAttachments.hasHeaddress(cat))
        );
        event.registerEntityModifier(
            OcelotRenderer.class,
            (ocelot, state) -> state.setRenderData(HAS_HEADDRESS, RelixAttachments.hasHeaddress(ocelot))
        );
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        EntityModelSet models = event.getEntityModels();
        if (event.getRenderer(EntityType.CAT) instanceof CatRenderer cat) {
            cat.addLayer(new PharaohHeaddressLayer<>(cat, models));
        }
        if (event.getRenderer(EntityType.OCELOT) instanceof OcelotRenderer ocelot) {
            ocelot.addLayer(new PharaohHeaddressLayer<>(ocelot, models));
        }
    }
}
