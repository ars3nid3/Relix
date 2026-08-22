package arsenide.relix.entity.renderer.models;

import org.joml.Vector3f;

import arsenide.relix.Relix;
import arsenide.relix.entity.PharaohEntity;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.entity.animation.json.AnimationHolder;

public class PharaohModel extends HierarchicalModel<PharaohEntity> {

    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public static final ModelLayerLocation PHARAOH_LAYER = 
        new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh"),
        "main"
        );

    public static final AnimationHolder IDLE_ANIMATION =
        getAnimation(ResourceLocation.fromNamespaceAndPath(
            Relix.MODID,
            "pharaoh/idle"
        )
    );
    public static final AnimationHolder SPAWN_ANIMATION =
        getAnimation(ResourceLocation.fromNamespaceAndPath(
            Relix.MODID,
            "pharaoh/spawn"
        )
    );
    public static final AnimationHolder SUMMONING_UNDEAD_ANIMATION =
        getAnimation(ResourceLocation.fromNamespaceAndPath(
            Relix.MODID,
            "pharaoh/summon"
        )
    );
    public static final AnimationHolder SPELLCASTING_ANIMATION =
        getAnimation(ResourceLocation.fromNamespaceAndPath(
            Relix.MODID,
            "pharaoh/cast_spell"
        )
    );
    public static final AnimationHolder SHOCKWAVE_ANIMATION =
        getAnimation(ResourceLocation.fromNamespaceAndPath(
            Relix.MODID,
            "pharaoh/shockwave"
        )
    );

    private void applyAnimation(
        AnimationHolder animation,
        int stateTick
    ) {
        KeyframeAnimations.animate(
            this, 
            animation.get(), 
            stateTick * 50L, 
            1.0F, 
            ANIMATION_VECTOR_CACHE
        );
    }

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    @SuppressWarnings("unused")
    private final ModelPart cape;
    private final ModelPart staff;
    @SuppressWarnings("unused")
    private final ModelPart ball;
    private final ModelPart spellBook;
    @SuppressWarnings("unused")
    private final ModelPart spellBookRight;
    @SuppressWarnings("unused")
    private final ModelPart spellBookLeft;
    @SuppressWarnings("unused")
    private final ModelPart spellBookPage;

    public PharaohModel(ModelPart root) {
        super();
        this.root = root.getChild("root");
        this.leftLeg = this.root.getChild("leftLeg");
        this.rightLeg = this.root.getChild("rightLeg");
        this.body = this.root.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.leftArm = this.chest.getChild("leftArm");
        this.rightArm = this.chest.getChild("rightArm");
        this.cape = this.chest.getChild("cape");
        this.staff = this.rightArm.getChild("staff");
        this.ball = this.staff.getChild("ball");
        this.spellBook = this.leftArm.getChild("spellBook");
        this.spellBookRight = this.spellBook.getChild("spellBookRight");
        this.spellBookLeft = this.spellBook.getChild("spellBookLeft");
        this.spellBookPage = this.spellBook.getChild("spellBookPage");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition root = part.addOrReplaceChild(
            "root",
            CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F)
        );

		PartDefinition leftLeg = root.addOrReplaceChild(
            "leftLeg", 
            CubeListBuilder.create()
                .texOffs(60, 0)
                .addBox(
                    -2.0F, 0.0F, -2.0F, 
                    4.0F, 14.0F, 4.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(2.0F, -14.0F, 0.0F)
        );

		PartDefinition rightLeg = root.addOrReplaceChild(
            "rightLeg", 
            CubeListBuilder.create()
                .texOffs(44, 0)
                .addBox(
                    -2.0F, 0.0F, -2.0F, 
                    4.0F, 14.0F, 4.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(-2.0F, -14.0F, 0.0F)
        );

        PartDefinition body = root.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(51, 19)
                .addBox(
                    -4.0F, -15.0F, -4.0F,
                    8.0F, 15.0F, 4.0F,
                    new CubeDeformation(0.0F)
                ),
            PartPose.offset(0.0F, -14.0F, 2.0F)
        );

        PartDefinition chest = body.addOrReplaceChild(
            "chest",
            CubeListBuilder.create()
                .texOffs(68, 42)
                .addBox(
                    -11.0F, -9.1F, -5.0F,
                    22.0F, 10.0F, 6.0F,
                    new CubeDeformation(0)
                )
                .texOffs(17, 50)
                .addBox(
                    -5.0F, -9.0F, -4.0F,
                    10.0F, 9.0F, 4.0F,
                    new CubeDeformation(0)
                ),
            PartPose.offset(0.0F, -15.0F, 0.0F)
        );

		PartDefinition head = chest.addOrReplaceChild(
            "head", 
            CubeListBuilder.create()
                .texOffs(96, 18)
                .addBox(
                    -4.0F, -8.0F, -4.0F, 
                    8.0F, 8.0F, 8.0F, 
                    new CubeDeformation(0.0F)
                )
		        .texOffs(92, 0)
                .addBox(
                    -4.5F, -8.5F, -4.5F, 
                    9.0F, 9.0F, 9.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(0.0F, -9.0F, -2.0F)
        );

		PartDefinition leftArm = chest.addOrReplaceChild(
            "leftArm", 
            CubeListBuilder.create()
                .texOffs(76, 0)
                .addBox(
                    0.0F, -2.0F, -2.0F, 
                    4.0F, 24.0F, 4.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(5.0F, -6.5F, -2.0F)
        );

		PartDefinition rightArm = chest.addOrReplaceChild(
            "rightArm", 
            CubeListBuilder.create()
                .texOffs(28, 0)
                .addBox(
                    -4.0F, -2.0F, -2.0F, 
                    4.0F, 24.0F, 4.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(-5.0F, -6.5F, -2.0F)
        );

		PartDefinition cape = chest.addOrReplaceChild(
            "cape", 
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(
                    -6.0F, 0.0F, -3.25F, 
                    12.0F, 30.0F, 2.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(0.0F, -8.9F, 3.0F)
        );

		PartDefinition staff = rightArm.addOrReplaceChild(
            "staff", 
            CubeListBuilder.create()
                .texOffs(0, 18)
                .addBox(
                    -1.0F, -0.5F, -20.0F, 
                    2.0F, 2.0F, 45.0F, 
                    new CubeDeformation(0.0F)
                )
		        .texOffs(18, 28)
                .addBox(
                    -1.5F, -5.5F, -29.9F, 
                    3.0F, 11.0F, 10.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(-2.0F, 19.0F, 0.0F)
        );

		PartDefinition ball = staff.addOrReplaceChild(
            "ball", 
            CubeListBuilder.create()
                .texOffs(51, 54)
                .addBox(
                    -2.0F, -2.0F, -1.9F, 
                    4.0F, 4.0F, 4.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(0.0F, 0.5F, -25.0F)
        );

		PartDefinition spellBook = leftArm.addOrReplaceChild(
            "spellBook", 
            CubeListBuilder.create(), 
            PartPose.offset(2.0F, 19.0F, 0.0F)
        );

		PartDefinition spellBookRight = spellBook.addOrReplaceChild(
            "spellBookRight", 
            CubeListBuilder.create()
                .texOffs(76, 65)
                .addBox(
                    -1.25F, -0.25F, -6.0F, 
                    2.0F, 7.0F, 12.0F, 
                    new CubeDeformation(0.0F)
                )
		        .texOffs(24, 65)
                .addBox(
                    -1.0F, 0.0F, -5.5F, 
                    1.0F, 6.0F, 11.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );

		PartDefinition spellBookLeft = spellBook.addOrReplaceChild(
            "spellBookLeft", 
            CubeListBuilder.create()
                .texOffs(0, 65)
                .addBox(
                    0.0F, 0.0F, -5.5F, 
                    1.0F, 6.0F, 11.0F, 
                    new CubeDeformation(0.0F)
                )
		        .texOffs(48, 65)
                .addBox(
                    -0.75F, -0.25F, -6.0F, 
                    2.0F, 7.0F, 12.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );

		PartDefinition spellBookPage = spellBook.addOrReplaceChild(
            "spellBookPage", 
            CubeListBuilder.create()
                .texOffs(0, 71)
                .addBox(
                    0.0F, 0.0F, -5.5F, 
                    0.0F, 6.0F, 11.0F, 
                    new CubeDeformation(0.0F)
                ), 
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 128, 128);
    }

    private void fixAnimationRotations(ModelPart root) {
        // TODO: Fix this inside the animations
        // Stupid shitty blockbench animation export plugin uses different
        // rotation conventions???
        root.getAllParts().forEach(part -> {
            PartPose initial = part.getInitialPose();
            float dx = part.x - initial.x;
            part.x = initial.x - dx;
            part.xRot = -part.xRot;
            part.yRot = -part.yRot;
        });
    }

    @Override
    public void setupAnim(
        PharaohEntity entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        this.root.getAllParts().forEach(part -> part.resetPose());
        switch (entity.getState()) {
            case SPAWNING:
                applyAnimation(SPAWN_ANIMATION, entity.getStateTick());
                fixAnimationRotations(this.root);
                // Rotate the head when looking
                this.head.xRot = headPitch * (float) (Math.PI / 180.0);
                this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);  
                break;
            case SUMMONING_UNDEAD:
                applyAnimation(SUMMONING_UNDEAD_ANIMATION, entity.getStateTick());
                fixAnimationRotations(this.root);
                break;
            case SPELLCASTING:
                applyAnimation(SPELLCASTING_ANIMATION, entity.getStateTick());
                fixAnimationRotations(this.root);
                break;
            case SHOCKWAVE:
                applyAnimation(SHOCKWAVE_ANIMATION, entity.getStateTick());
                fixAnimationRotations(this.root);
                break;
            default:
                applyAnimation(IDLE_ANIMATION, entity.getStateTick());
                fixAnimationRotations(this.root);
                this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
                this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
                // Rotate the head when looking
                this.head.xRot = headPitch * (float) (Math.PI / 180.0);
                this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);        
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
