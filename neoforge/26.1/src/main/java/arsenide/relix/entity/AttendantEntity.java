package arsenide.relix.entity;

import arsenide.relix.entity.ai.LookAtBlockGoal;
import arsenide.relix.entity.ai.MoveToAltarGoal;
import arsenide.relix.entity.ai.PerformAttendantGoal;
import arsenide.relix.entity.ai.SlowlyLookAtTargetAndFloatUpGoal;
import arsenide.relix.items.RelixItems;
import arsenide.relix.util.SpawnUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class AttendantEntity extends Monster {

    private boolean holdsJukebox;
    private BlockPos altarPos;
    private BlockPos linkedPos;

    private int lifeTimeCounter = 0;
    private final int LIFE_TIME = 200; // 10 seconds

    private boolean isLinked = false;

    protected AttendantEntity(EntityType<? extends AttendantEntity> type, Level level) {
        this(level, false, null);
    }

    public AttendantEntity(Level level, boolean holdsJukebox, BlockPos altarPos) {
        this.holdsJukebox = holdsJukebox;
        this.altarPos = altarPos;
        if (altarPos != null) {
            this.linkedPos = altarPos.above(10);
        }
        // Set pos before calling super so that the goal registration works
        super(RelixEntities.ATTENDANT_ENTITY.get(), level);
        assignAttendantTask();
    }

    public static AttributeSupplier getDefaultAttributes() {
        return createMonsterAttributes()
        .add(Attributes.MOVEMENT_SPEED, 0.25D)
        .build();
    }

    private void assignAttendantTask() {
        if (holdsJukebox) {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(
                Items.JUKEBOX, 1
            ));
        } else {
            setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(
                RelixItems.MUSIC_DISC_PHARAOH.get(), 1
            ));
        }
    }

    public void setLinked(boolean isLinked) {
        this.isLinked = isLinked;
    }

    @Override
    protected void registerGoals() {
        if (altarPos != null) {
            goalSelector.addGoal(0, new SlowlyLookAtTargetAndFloatUpGoal(this, altarPos, linkedPos, 160));
            goalSelector.addGoal(1, new PerformAttendantGoal(this, altarPos));
            goalSelector.addGoal(2, new MoveToAltarGoal(
                this, 
                this.holdsJukebox ? altarPos.east() : altarPos.west()
            ));
            goalSelector.addGoal(5, new LookAtBlockGoal(this, altarPos.above()));
        } else {
            super.registerGoals();
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.PARCHED_STEP, 0.15F, 1.0F);
    }

    public boolean isJukeboxAttendant() {
        return holdsJukebox;
    }

    @Override
    public void onAddedToLevel() {
        SpawnUtil.summonPoof(this.position(), level());
        if (!level().isClientSide()) {
            setItemSlot(
                EquipmentSlot.HEAD,
                new ItemStack(RelixItems.PHARAOH_HEADDRESS.get())
            );
            setDropChance(EquipmentSlot.HEAD, 0.0F);
        }
        super.onAddedToLevel();
    }

    @Override
    public void tick() {
        super.tick();
        if (isLinked && linkedPos != null && level() instanceof ServerLevel sLevel) {
            Vec3 linkedPosVec = Vec3.atCenterOf(linkedPos);
            Vec3 start = this.position().add(0, this.getBbHeight() / 2, 0);
            Vec3 delta = linkedPosVec.subtract(start);
            double distance = delta.length();
            double spacing = 0.15;
            Vec3 step = delta.normalize().scale(spacing);
            Vec3 particlePos = start;
            for (double travelled = 0; travelled < distance; travelled += spacing) {
                sLevel.sendParticles(
                    ParticleTypes.SOUL,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0,
                    0,
                    0,
                    0
                );
                particlePos = particlePos.add(step);
            }
        }
        if (this.isAddedToLevel()) {
            lifeTimeCounter++;
        }
        if (lifeTimeCounter >= LIFE_TIME) {
            this.discard();
        }
    }

    @Override
    public void onRemovedFromLevel() {
        SpawnUtil.summonPoof(this.position(), level());
        super.onRemovedFromLevel();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Lifetime", lifeTimeCounter);
        output.putBoolean("HoldsJukebox", holdsJukebox);
        if (altarPos != null) {
            output.putIntArray("AltarPos", new int[] {
                altarPos.getX(),
                altarPos.getY(),
                altarPos.getZ()
            });
        }
        output.putBoolean("IsLinked", isLinked);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        lifeTimeCounter = input.getIntOr("Lifetime", 0);
        holdsJukebox = input.getBooleanOr("HoldsJukebox", false);
        int[] altarPosArray = input.getIntArray("AltarPos").orElse(
            new int[] {}
        );
        if (altarPosArray.length == 0) {
            altarPos = null;
        } else {
            altarPos = new BlockPos(
                altarPosArray[0],
                altarPosArray[1],
                altarPosArray[2]
            );
            linkedPos = altarPos.above(10);
        }
        isLinked = input.getBooleanOr("IsLinked", false);
    }
}