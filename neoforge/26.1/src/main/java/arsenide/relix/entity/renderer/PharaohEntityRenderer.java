package arsenide.relix.entity.renderer;

import arsenide.relix.Relix;
import arsenide.relix.entity.PharaohEntity;
import arsenide.relix.entity.ai.PharaohState;
import arsenide.relix.entity.renderer.layers.InvulnerabilityLayer;
import arsenide.relix.entity.renderer.models.PharaohModel;
import arsenide.relix.entity.renderer.state.PharaohRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.Identifier;

public class PharaohEntityRenderer extends LivingEntityRenderer<
    PharaohEntity, 
    PharaohRenderState, 
    PharaohModel
> {
    private static final Identifier PHARAOH_LOCATION = 
    Identifier.fromNamespaceAndPath(
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
    public Identifier getTextureLocation(PharaohRenderState state) {
        return PHARAOH_LOCATION;
    }

    @Override
    public void extractRenderState(
        PharaohEntity entity, 
        PharaohRenderState state, 
        float partialTicks
    ) {
        super.extractRenderState(entity, state, partialTicks);

        state.isInvulnerable = entity.getState() == PharaohState.SPAWNING;
        state.pharaohStateTick = entity.getStateTick();
        state.pharaohState = entity.getState();
    }

    @Override
    public PharaohRenderState createRenderState() {
        return new PharaohRenderState();
    }
}