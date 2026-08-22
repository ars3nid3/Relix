package arsenide.relix.entity.renderer;

import arsenide.relix.entity.AttendantEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class AttendantEntityRenderer extends HumanoidMobRenderer<AttendantEntity, HumanoidModel<AttendantEntity>> {
    
    public AttendantEntityRenderer(Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
            this,
            new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
            new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)),
            context.getModelManager()
        ));
    }

    private static final ResourceLocation SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton.png");

    public ResourceLocation getTextureLocation(AttendantEntity entity) {
        return SKELETON_LOCATION;
    }
    
}
