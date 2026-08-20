package arsenide.relix.client.render;

import org.joml.Matrix4fc;

import arsenide.relix.client.PharaohVisualController;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomCloudsRenderer;

public class BossCloudRenderer implements CustomCloudsRenderer {

    @Override
    public boolean renderClouds(
        LevelRenderState levelRenderState, 
        Vec3 camPos, 
        CloudStatus cloudStatus, 
        int cloudColor,
        float cloudHeight, 
        int cloudRange, 
        Matrix4fc modelViewMatrix
    ) {
        if (PharaohVisualController.isPyramidEnabled()) {
            return true;
        }
        return false;
    }
    
}
