package arsenide.relix.entity.renderer;

import arsenide.relix.Relix;
import arsenide.relix.entity.PharaohEntity;
import arsenide.relix.entity.renderer.layers.InvulnerabilityLayer;
import arsenide.relix.entity.renderer.models.PharaohModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class PharaohEntityRenderer extends LivingEntityRenderer<
    PharaohEntity,  
    PharaohModel
> {
    private static final ResourceLocation PHARAOH_LOCATION = 
    ResourceLocation.fromNamespaceAndPath(
        Relix.MODID,
        "textures/entity/pharaoh.png"
    );
    public PharaohEntityRenderer(Context context) {
        this(
            context, 
            new PharaohModel(
                context.bakeLayer(PharaohModel.PHARAOH_LAYER)), 
                0.7F
            );
    }

    public PharaohEntityRenderer(
        Context context, 
        PharaohModel model, 
        float shadow
    ) {
        super(context, model, shadow);
        this.addLayer(new InvulnerabilityLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(PharaohEntity entity) {
        return PHARAOH_LOCATION;
    }
}