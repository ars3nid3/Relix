package arsenide.relix.entity.ai;

import java.util.EnumSet;

import arsenide.relix.entity.AttendantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;

public class SlowlyLookAtTargetAndFloatUpGoal extends Goal {

    private final AttendantEntity attendant;
    private int tickCount = 0;
    private final int duration;
    private final BlockPos targetPos;
    private final BlockPos sourcePos;

    public SlowlyLookAtTargetAndFloatUpGoal(AttendantEntity attendant, BlockPos sourcePos, BlockPos targetPos, int duration) {
        this.attendant = attendant;
        this.targetPos = targetPos;
        this.sourcePos = sourcePos;
        this.duration = duration;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        BlockEntity blockEntity = attendant.level().getBlockEntity(sourcePos);
        if (!(blockEntity instanceof JukeboxBlockEntity jukeboxBlockEntity)) {
            return false;
        }
        if (jukeboxBlockEntity.getItem(0).isEmpty()) {
            return false;
        }
        return attendant.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
    }

    @Override
    public void start() {
        tickCount = 0;
        attendant.setNoGravity(true);
        attendant.setLinked(true);
    }

    @Override
    public void tick() {
        tickCount++;
        
        float progress = Math.min(((float) tickCount / duration), 1.0F);
        double dy = ((float) targetPos.getY() - sourcePos.getY()) * progress;

        attendant.getLookControl().setLookAt(
            Vec3.atCenterOf(sourcePos).add(0, dy, 0)
        );
        attendant.setDeltaMovement(0, 0.02, 0);
    }

    @Override
    public boolean canContinueToUse() {
        return tickCount < duration;
    }
    
}
