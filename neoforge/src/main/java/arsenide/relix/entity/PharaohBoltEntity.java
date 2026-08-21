package arsenide.relix.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PharaohBoltEntity extends Projectile {

    public static final float SPEED = 0.45F;
    private static final float HOMING_STRENGTH = 0.12F;
    private static final float DAMAGE = 6.0F;
    private static final int LIFETIME = 100;

    private LivingEntity target;

    public PharaohBoltEntity(EntityType<? extends PharaohBoltEntity> type, Level level) {
        super(RelixEntities.PHARAOH_BOLT_ENTITY.get(), level);
    }

    public PharaohBoltEntity(Level level, LivingEntity owner, LivingEntity target) {
        this(RelixEntities.PHARAOH_BOLT_ENTITY.get(), level);
        setOwner(owner);
        this.target = target;
    }

    @Override
    protected void defineSynchedData(Builder entityData) {
    }

    @Override
    public void tick() {
        super.tick();

        HitResult hitResult = null;
        if (!level().isClientSide()) {
            if (tickCount > LIFETIME) {
                discard();
                return;
            }

            steerTowardTarget();
            spawnSandTrail();

            hitResult = ProjectileUtil.getHitResultOnMoveVector(
                this,
                entity -> !entity.isSpectator() && entity.isPickable() && entity != getOwner()
            );
        }

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);

        if (!level().isClientSide()) {
            applyEffectsFromBlocks();
            if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                onHit(hitResult);
            }
        }

        ProjectileUtil.rotateTowardsMovement(this, 0.5F);
    }

    private void steerTowardTarget() {
        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-7D) {
            return;
        }

        Vec3 direction = motion.normalize();
        if (target != null && target.isAlive()) {
            Vec3 desired = target.getEyePosition().subtract(position()).normalize();
            direction = direction.lerp(desired, HOMING_STRENGTH).normalize();
        }

        setDeltaMovement(direction.scale(SPEED));
    }

    private void spawnSandTrail() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(
            new BlockParticleOption(
                ParticleTypes.FALLING_DUST,
                Blocks.SAND.defaultBlockState()
            ),
            getX() + (random.nextDouble() - 0.5D) * 0.15D,
            getY() + (random.nextDouble() - 0.5D) * 0.15D,
            getZ() + (random.nextDouble() - 0.5D) * 0.15D,
            1,
            0.0D,
            -0.04D,
            0.0D,
            0.01D
        );
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity hit = hitResult.getEntity();
        if (hit == getOwner()) {
            return;
        }
        if (hit instanceof Parched || hit instanceof Husk) {
            return;
        }
        if (hit instanceof LivingEntity livingEntity) {
            livingEntity.hurtServer(
                serverLevel,
                damageSources().mobProjectile(
                    this,
                    (LivingEntity) getOwner()
                ),
                DAMAGE
            );
        }
        discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }
        return !(entity instanceof Parched || entity instanceof Husk);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 0.35F;
    }
}
