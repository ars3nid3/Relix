package arsenide.relix.entity.ai;

import java.util.EnumSet;

import arsenide.relix.entity.PharaohEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class PharaohDoNothingGoal extends Goal {

    private PharaohEntity pharaoh;

    public PharaohDoNothingGoal(PharaohEntity entity) {
        this.pharaoh = entity;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return pharaoh.getState() == PharaohState.SPAWNING ||
        pharaoh.getState() == PharaohState.DYING;
    }
    
}
