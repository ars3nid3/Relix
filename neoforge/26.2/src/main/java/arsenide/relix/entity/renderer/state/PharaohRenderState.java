package arsenide.relix.entity.renderer.state;

import arsenide.relix.entity.ai.PharaohState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class PharaohRenderState extends HumanoidRenderState {
    public PharaohState pharaohState;
    public boolean isInvulnerable;
    public int pharaohStateTick;

}
