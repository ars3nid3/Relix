package arsenide.relix.entity.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import arsenide.relix.Relix;
import arsenide.relix.world.RelixAttachments;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class PharaohHeaddressLayer<
    T extends Mob,
    M extends OcelotModel<T>
> extends RenderLayer<T, M> {

    public static final ModelLayerLocation FELINE_HEADDRESS =
        register("feline", "pharaoh_headdress");
    public static final ModelLayerLocation CAT_HEADDRESS =
        register("cat", "pharaoh_headdress");

    private static final ResourceLocation ADULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        Relix.MODID,
        "textures/entity/equipment/feline/pharaoh_headdress.png"
    );

    private final OcelotModel<T> headdressModel;

    private static final CubeDeformation HEADDRESS_DEFORMATION = new CubeDeformation(
        0.25F
    );

    public PharaohHeaddressLayer(
        RenderLayerParent<T, M> renderer,
        EntityModelSet modelSet,
        boolean isCat
    ) {
        super(renderer);
        ModelLayerLocation layer = isCat ? CAT_HEADDRESS : FELINE_HEADDRESS;
        if (isCat) {
            @SuppressWarnings("unchecked")
            OcelotModel<T> model = (OcelotModel<T>) new CatModel<>(modelSet.bakeLayer(layer));
            this.headdressModel = model;
        } else {
            this.headdressModel = new OcelotModel<>(modelSet.bakeLayer(layer));
        }
    }

    private static ModelLayerLocation register(String model, String layer) {
        return new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, model),
            layer
        );
    }

    public static LayerDefinition createFelineHeaddressLayer() {
        return LayerDefinition.create(
            CatModel.createBodyMesh(PharaohHeaddressLayer.HEADDRESS_DEFORMATION),
            64,
            32
        );
    }

    public static LayerDefinition createCatHeaddressLayer() {
        return createFelineHeaddressLayer();
    }

    @Override
    public void render(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        T entity,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        if (!RelixAttachments.hasHeaddress(entity)) {
            return;
        }

        coloredCutoutModelCopyLayerRender(
            this.getParentModel(),
            headdressModel,
            ADULT_TEXTURE,
            poseStack,
            bufferSource,
            packedLight,
            entity,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            netHeadYaw,
            headPitch,
            partialTick,
            -1
        );
    }
}
