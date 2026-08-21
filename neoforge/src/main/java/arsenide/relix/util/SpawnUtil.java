package arsenide.relix.util;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpawnUtil {

    public static @Nullable Vec3 getRandomSpawnPosInCircle(
        Level level,
        Vec3 center,
        double circleRadius,
        int horizontalSearchRadius,
        int verticalSearchRadius,
        float requiredHeight,
        float requiredWidth
    ) {
        int angleDeg = level.getRandom().nextInt(360);
        return getSpawnPosInCircle(
            level,
            center,
            circleRadius,
            angleDeg,
            horizontalSearchRadius,
            verticalSearchRadius,
            requiredHeight,
            requiredWidth
        );
    }

    public static @Nullable Vec3 getSpawnPosInCircle(
        Level level,
        Vec3 center,
        double radius,
        int angleDeg,
        int horizontalSearchRadius,
        int verticalSearchRadius,
        float requiredHeight,
        float requiredWidth
    ) {
        double angleRad = Math.toRadians(angleDeg);
        Vec3 desiredPos = center.add(
            Math.cos(angleRad) * radius,
            0,
            Math.sin(angleRad) * radius
        );
        return findSpawnPosAroundCenter(
            level, 
            desiredPos, 
            horizontalSearchRadius, 
            verticalSearchRadius,
            requiredHeight,
            requiredWidth
        );
    }

    public static boolean isValidSpawnPosition(
        Level level, 
        BlockPos feet,
        float requiredHeight,
        float requiredWidth
    ) {
        BlockPos below = feet.below();
        if (level.getBlockState(below).isAir()) return false;
        for (int dy = 0; dy < (int) Math.ceil(requiredHeight); dy++) {
            for (int dx = 0; dx < (int) Math.ceil(requiredWidth); dx++) {
                for (int dz = 0; dz < (int) Math.ceil(requiredWidth); dz++) {
                    BlockPos pos = feet.offset(dx, dy, dz);
                    if (!level.getBlockState(pos).isAir()) return false;
                }
            }
        }
        return true;
    }

    public static @Nullable Vec3 findSpawnPosAroundCenter(
        Level level,
        Vec3 center,
        int horizontalRadius,
        int verticalRadius,
        float requiredHeight,
        float requiredWidth
    ) {
        // We search the area in increasing radii.
        for (int radius = 0; radius <= horizontalRadius; radius++) {
            for (int dx = 0; dx <= radius; dx++) {
                for (int dz = 0; dz <= radius; dz++) {
                    // Only search the perimeter to avoid multiple checks
                    // on the same position.
                    if (dx != radius && dz != radius) {
                        continue;
                    }
                    // The column of positions to check.
                    Vec3 evaluatedColumn = center.add(dx, 0, dz);
                    for (int dy = verticalRadius; dy >= -verticalRadius; dy--) {
                        Vec3 feetPos = evaluatedColumn.add(0, dy, 0);
                        BlockPos feetBlockPos = new BlockPos(
                            (int) Math.floor(feetPos.x),
                            (int) Math.floor(feetPos.y),
                            (int) Math.floor(feetPos.z)
                        );
                        if (isValidSpawnPosition(
                            level, 
                            feetBlockPos, 
                            requiredHeight, 
                            requiredWidth
                        )) {
                            return feetPos;
                        }
                    }
                }
            } 
        }
        return null;
    }

    public static void summonPoof(Vec3 poofPos, Level level) {
        if (level.isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        serverLevel.playSound(
            null,
            poofPos.x,
            poofPos.y,
            poofPos.z,
            SoundEvents.EVOKER_CAST_SPELL,
            SoundSource.HOSTILE,
            1.0F,
            0.8F
        );
        serverLevel.sendParticles(
            ParticleTypes.POOF,
            poofPos.x,
            poofPos.y,
            poofPos.z,
            40,
            0.5,
            0.6,
            0.4,
            0.05
        );
    }
}
