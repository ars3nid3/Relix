package arsenide.relix.entity.ai;

import java.util.EnumSet;

import arsenide.relix.entity.AttendantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class MoveToAltarGoal extends Goal {

    private final BlockPos altarPos;
    private final AttendantEntity attendant;

    public MoveToAltarGoal(AttendantEntity attendant, BlockPos altarPos) {
        this.attendant = attendant;
        this.altarPos = altarPos;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !isCloseEnough();
    }

    private boolean isCloseEnough() {
        return attendant.distanceToSqr(
            Vec3.atCenterOf(altarPos)
        ) <= 0.25;
    }

    @Override
    public void start() {
        Vec3 targetPos = Vec3.atCenterOf(altarPos);
        attendant.getNavigation().moveTo(
            targetPos.x, 
            targetPos.y, 
            targetPos.z,
            1.0F
        );
    }

    @Override
    public boolean canContinueToUse() {
        return !isCloseEnough() && !attendant.getNavigation().isDone();
    }

    @Override
    public void stop() {
        attendant.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (attendant.getNavigation().isDone() && !isCloseEnough()) {
            Vec3 targetPos = Vec3.atCenterOf(altarPos);
            attendant.getNavigation().moveTo(
                targetPos.x, 
                targetPos.y, 
                targetPos.z,
                1.0F
            );
        }
    }
    
}
