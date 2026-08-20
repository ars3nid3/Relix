package arsenide.relix.entity.renderer;

import arsenide.relix.entity.AttendantEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class AttendantEntityRenderer extends HumanoidMobRenderer<AttendantEntity, SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {
    
    public AttendantEntityRenderer(Context context) {
        this(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.PARCHED)), 0.5F);
    }
    
    public AttendantEntityRenderer(Context context, SkeletonModel<SkeletonRenderState> model, float shadow) {
        super(context, model, shadow);
        this.addLayer(new HumanoidArmorLayer<>(
            this,
            ArmorModelSet.bake(
                ModelLayers.PARCHED_ARMOR,
                context.getModelSet(),
                SkeletonModel::new
            ),
            context.getEquipmentRenderer()
        ));
    }

    private static final Identifier PARCHED_SKELETON_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/parched.png");


    public void extractRenderState(AttendantEntity entity, SkeletonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
        state.isShaking = false;
        state.isHoldingBow = entity.getMainHandItem().is(Items.BOW);
    }

    protected boolean isShaking(SkeletonRenderState state) {
        return state.isShaking;
    }

    public Identifier getTextureLocation(SkeletonRenderState state) {
        return PARCHED_SKELETON_LOCATION;
    }

    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
    
}
