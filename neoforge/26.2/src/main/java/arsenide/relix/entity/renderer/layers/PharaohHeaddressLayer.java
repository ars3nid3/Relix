package arsenide.relix.entity.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import arsenide.relix.Relix;
import arsenide.relix.client.RelixClient;
import net.minecraft.client.model.animal.feline.AbstractFelineModel;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.animal.feline.AdultFelineModel;
import net.minecraft.client.model.animal.feline.AdultOcelotModel;
import net.minecraft.client.model.animal.feline.BabyCatModel;
import net.minecraft.client.model.animal.feline.BabyFelineModel;
import net.minecraft.client.model.animal.feline.BabyOcelotModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.resources.Identifier;

public class PharaohHeaddressLayer<
    T extends FelineRenderState,
    M extends AbstractFelineModel<? super T>
> extends RenderLayer<T, M> {

    public static final ModelLayerLocation FELINE_HEADDRESS =
        register("feline", "pharaoh_headdress");
    public static final ModelLayerLocation CAT_HEADDRESS =
        register("cat", "pharaoh_headdress");
    public static final ModelLayerLocation FELINE_BABY_HEADDRESS =
        register("feline_baby", "pharaoh_headdress");
    public static final ModelLayerLocation CAT_BABY_HEADDRESS =
        register("cat_baby", "pharaoh_headdress");

    private static final Identifier ADULT_TEXTURE = Identifier.fromNamespaceAndPath(
        Relix.MODID,
        "textures/entity/equipment/feline/pharaoh_headdress.png"
    );
    private static final Identifier BABY_TEXTURE = Identifier.fromNamespaceAndPath(
        Relix.MODID,
        "textures/entity/equipment/feline_baby/pharaoh_headdress.png"
    );

    private final AdultCatModel adultCatModel;
    private final BabyCatModel babyCatModel;
    private final AdultOcelotModel adultOcelotModel;
    private final BabyOcelotModel babyOcelotModel;

    private static final CubeDeformation HEADDRESS_DEFORMATION = new CubeDeformation(
        0.25F
    );

    private static final MeshTransformer BABY_HEADDRESS_TRANSFORMER = mesh ->
        MeshTransformer.scaling(1.08F)
        .apply(mesh)
        .transformed(pose -> pose.translated(0.0F, 0.0F, 0.25F));

    public PharaohHeaddressLayer(
        RenderLayerParent<T, M> renderer,
        EntityModelSet modelSet
    ) {
        super(renderer);
        this.adultCatModel = new AdultCatModel(modelSet.bakeLayer(CAT_HEADDRESS));
        this.babyCatModel = new BabyCatModel(modelSet.bakeLayer(CAT_BABY_HEADDRESS));
        this.adultOcelotModel = new AdultOcelotModel(modelSet.bakeLayer(FELINE_HEADDRESS));
        this.babyOcelotModel = new BabyOcelotModel(modelSet.bakeLayer(FELINE_BABY_HEADDRESS));
    }

    private static ModelLayerLocation register(String model, String layer) {
        return new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Relix.MODID, model),
            layer
        );
    }

    public static LayerDefinition createFelineHeaddressLayer() {
        return LayerDefinition.create(
            AdultFelineModel.createBodyMesh(PharaohHeaddressLayer.HEADDRESS_DEFORMATION),
            64,
            32
        );
    }

    public static LayerDefinition createCatHeaddressLayer() {
        return createFelineHeaddressLayer().apply(AdultCatModel.CAT_TRANSFORMER);
    }

    public static LayerDefinition createFelineBabyHeaddressLayer() {
        return BabyFelineModel.createBabyLayer().apply(BABY_HEADDRESS_TRANSFORMER);
    }

    public static LayerDefinition createCatBabyHeaddressLayer() {
        return BabyFelineModel.createBabyLayer().apply(BABY_HEADDRESS_TRANSFORMER);
    }

    @Override
    public void submit(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        T state,
        float yRot,
        float xRot
    ) {
        if (!state.getRenderDataOrDefault(RelixClient.HAS_HEADDRESS, false)) {
            return;
        }

        boolean isCat = state instanceof CatRenderState;
        AbstractFelineModel<? super T> model = selectModel(state, isCat);
        Identifier texture = state.isBaby ? BABY_TEXTURE : ADULT_TEXTURE;

        coloredCutoutModelCopyLayerRender(
            model,
            texture,
            poseStack,
            submitNodeCollector,
            lightCoords,
            state,
            -1,
            1
        );
    }

    @SuppressWarnings("unchecked")
    private AbstractFelineModel<? super T> selectModel(T state, boolean isCat) {
        if (state.isBaby) {
            return isCat
                ? (AbstractFelineModel<? super T>) this.babyCatModel
                : (AbstractFelineModel<? super T>) this.babyOcelotModel;
        }

        return isCat
            ? (AbstractFelineModel<? super T>) this.adultCatModel
            : (AbstractFelineModel<? super T>) this.adultOcelotModel;
    }
}
