package arsenide.relix.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import arsenide.relix.Relix;
import arsenide.relix.client.camera.ScreenShake;
import arsenide.relix.sounds.RelixSounds;
import arsenide.relix.util.SpawnUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class PharaohSpawnSceneEntity extends Entity {

    private int tickCounter = 0;

    private BlockPos pos;
    private AttendantEntity jukeboxAttendant = null;
    private AttendantEntity discAttendant = null;
    private boolean hasBossSpawned = false;
    private UUID jukeboxAttendantUUID = null;
    private UUID discAttendantUUID = null;

    public PharaohSpawnSceneEntity(EntityType<? extends PharaohSpawnSceneEntity> type, Level level) {
        super(RelixEntities.PHARAOH_SPAWN_SCENE_ENTITY.get(), level);
        this.pos = null;
    }

    public PharaohSpawnSceneEntity(ServerLevel level, BlockPos pos, Player player) {
        super(RelixEntities.PHARAOH_SPAWN_SCENE_ENTITY.get(), level);
        this.pos = pos;
    }

    private @Nullable AttendantEntity spawnAttendant(int angleDeg, boolean holdsJukebox) {
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 spawnPos = SpawnUtil.getSpawnPosInCircle(
            level(), 
            center, 
            5.0D, // Radius of the circle around the center
            angleDeg,
            4, // Horizontal spawn margin
            8, // Vertical spawn margin
            2.0F, // Height of the entity
            1.0F // Width of the entity
        );
        if (spawnPos == null) return null;
        AttendantEntity attendant = new AttendantEntity(
            level(),
            holdsJukebox,
            pos
        );
        attendant.setPos(spawnPos);
        attendant.setInvulnerable(true);
        level().addFreshEntity(attendant);
        return attendant;
    }

    private void trySpawnAttendants() {
        int randomDeg = level().getRandom().nextInt(360);
        jukeboxAttendant = spawnAttendant(randomDeg, true);
        discAttendant = spawnAttendant((randomDeg + 180) % 360, false);
    }

    private boolean didAttendantsArrive() {
        return jukeboxAttendant != null && discAttendant != null;
    }

    private void trySpawnPharaoh(boolean ignoreAttendants) {
        // If the attendants are still doing their thing,
        // don't spawn the pharaoh
        if (!ignoreAttendants && didAttendantsArrive()) {
            return;
        }
        // If the pharaoh has already spawned for this scene,
        // don't spawn it again
        if (hasBossSpawned) {
            return;
        }
        // Spawn the pharaoh
        spawnPharaoh();
        // Set the boolean just in case discarding takes a few more ticks
        hasBossSpawned = true;
        // Remove the scene
        this.discard();
    }

    private void spawnPharaoh() {
        PharaohEntity pharaoh = RelixEntities.PHARAOH_ENTITY.get().create(
            level(),
            EntitySpawnReason.MOB_SUMMONED
        );
        pharaoh.informJukebox(pos);
        pharaoh.setPos(Vec3.atCenterOf(pos).add(0, 10, 0));
        level().addFreshEntity(pharaoh);
        level().playSound(
            null, 
            pos.above(10),
            SoundEvents.WITHER_SPAWN,
            SoundSource.HOSTILE
        );
    }

    private void spawnSandParticles() {
        if (tickCounter % 2 != 0) {
            return;
        }

        double angle = random.nextDouble() * Math.PI * 2;
        double radius = 0.4 + random.nextDouble() * 0.3;

        level().addParticle(
            new BlockParticleOption(
                ParticleTypes.FALLING_DUST,
                Blocks.SAND.defaultBlockState()
            ),
            getX() + Math.cos(angle) * radius,
            getY(),
            getZ() + Math.sin(angle) * radius,
            0,
            0.5,
            0
        );
    }

    private void resolveAttendants() {
        if (jukeboxAttendantUUID != null) {
            Entity entity = level().getEntity(jukeboxAttendantUUID);
            if (entity instanceof AttendantEntity attendantEntity) {
                jukeboxAttendant = attendantEntity;
                jukeboxAttendantUUID = null;
            }
        }
        if (discAttendantUUID != null) {
            Entity entity = level().getEntity(discAttendantUUID);
            if (entity instanceof AttendantEntity attendantEntity) {
                discAttendant = attendantEntity;
                discAttendantUUID = null;
            }
        }
    }

    @Override
    public void tick() {
        if (level().isClientSide()) {
            spawnSandParticles();
            if (tickCounter == 60) {
                ScreenShake.shake(2.0F, 140, 0, 10);
            }
            tickCounter++;
            return;
        }
        resolveAttendants();
        switch (tickCounter) {
            case 0: {
                // Try to spawn the attendants
                trySpawnAttendants();
                // Try to spawn the pharaoh.
                // If the attendants spawned, this will do nothing
                trySpawnPharaoh(false);
                break;
            }
            case 60: {
                level().playSound(
                    null, 
                    blockPosition(),
                    RelixSounds.EARTHQUAKE.get(),
                    SoundSource.AMBIENT
                );
                break;
            }
            // If the attendants spawned, they get 10 seconds to do their thing
            case 200: {
                // Force spawn the pharaoh if this hasn't happened yet.
                trySpawnPharaoh(true);
                break;
            }
            default: {}
        }
        tickCounter++;
    }

    @Override
    protected void defineSynchedData(Builder entityData) {
        // Probably use it to spawn cool visual effects
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        tickCounter = input.getIntOr("ElapsedTicks", 0);
        hasBossSpawned = input.getBooleanOr("HasBossSpawned", false);
        int[] posCoords = input.getIntArray("AltarPos").orElse(new int[] {});
        if (posCoords.length == 3) {
            pos = new BlockPos(
                posCoords[0],
                posCoords[1],
                posCoords[3]
            );
        }
        String jukeboxAttendantUuidString = input.getStringOr("JukeboxAttendantID", "");
        try {
            if (jukeboxAttendantUuidString.length() > 0) {
                jukeboxAttendantUUID = UUID.fromString(jukeboxAttendantUuidString);
            }
        } catch (IllegalArgumentException e) {
            Relix.LOGGER.error(
                "Pharaoh spawn scene was given invalid UUID format: " + jukeboxAttendantUuidString
            );
        }
        String discAttendantUuidString = input.getStringOr("DiscAttendantID", "");
        try {
            if (discAttendantUuidString.length() > 0) {
                discAttendantUUID = UUID.fromString(discAttendantUuidString);
            }
        } catch (IllegalArgumentException e) {
            Relix.LOGGER.error(
                "Pharaoh spawn scene was given invalid UUID format: " + jukeboxAttendantUuidString
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("ElapsedTicks", tickCounter);
        output.putBoolean("HasBossSpawned", hasBossSpawned);
        if (pos != null) {
            output.putIntArray("AltarPos", new int[] {
                pos.getX(),
                pos.getY(),
                pos.getZ()
            });
        }
        if (jukeboxAttendant != null) {
            output.putString("JukeboxAttendantID", jukeboxAttendant.getStringUUID());
        }
        if (discAttendant != null) {
            output.putString("DiscAttendantID", discAttendant.getStringUUID());
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}