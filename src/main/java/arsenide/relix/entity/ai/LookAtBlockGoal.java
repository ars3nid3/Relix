package arsenide.relix.entity.ai;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class LookAtBlockGoal extends Goal{


    private final Mob mob;
    private final BlockPos targetPos;

    public LookAtBlockGoal(Mob mob, BlockPos targetPos) {
        this.mob = mob;
        this.targetPos = targetPos;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void tick() {
        Vec3 target = Vec3.atCenterOf(targetPos);
        mob.getLookControl().setLookAt(
            target.x,
            target.y,
            target.z
        );
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
    
}
