package arsenide.relix.entity.ai;

import java.util.List;

import arsenide.relix.entity.PharaohEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class PharaohChooseStateGoal extends Goal {

    private PharaohEntity pharaoh;
    
    public PharaohChooseStateGoal(PharaohEntity entity) {
        this.pharaoh = entity;
    }

    @Override
    public boolean canUse() {
        return pharaoh.getState() == PharaohState.DEFAULT &&
        pharaoh.getSpecialAttackCooldown() <= 0;
    }

    @Override
    public void start() {
        pharaoh.setState(PharaohState.SUMMONING_UNDEAD);
        List<Player> playersInMeleeRange = pharaoh.level().getEntitiesOfClass(
            Player.class,
            new AABB(
                pharaoh.position().x - 4,
                pharaoh.position().y - 4,
                pharaoh.position().z - 4,
                pharaoh.position().x + 4,
                pharaoh.position().y + 4,
                pharaoh.position().z + 4
            ),
            player -> true
        );
        if (playersInMeleeRange.size() > 0) {
            pharaoh.setState(PharaohState.SHOCKWAVE);
            return;
        } else if (pharaoh.hasLivingDead()) {
            pharaoh.setState(PharaohState.SPELLCASTING);
        } else {
            pharaoh.setState(PharaohState.SUMMONING_UNDEAD);
        }
    }
    
}
