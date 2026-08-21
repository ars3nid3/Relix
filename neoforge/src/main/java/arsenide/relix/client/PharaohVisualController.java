package arsenide.relix.client;

import java.util.HashSet;
import java.util.Set;

public final class PharaohVisualController {

    private static float sandStormIntensity = 0.0F;
    private static boolean enablePyramid;

    private static Set<Integer> activePharaohs = new HashSet<>();

    private PharaohVisualController() {}

    public static boolean isPyramidEnabled() {
        return enablePyramid;
    }

    public static float getSandStormIntensity() {
        return sandStormIntensity;
    }

    public static void activate(boolean enablePyramid, float sandStormIntensity) {
        if (!enablePyramid && (sandStormIntensity <= 0.0F)) {
            return;
        }
        PharaohVisualController.enablePyramid = enablePyramid;
        PharaohVisualController.sandStormIntensity = sandStormIntensity;
    }

    public static void addPharaoh(int entityId) {
        PharaohVisualController.activePharaohs.add(entityId);
        PharaohVisualController.activate(true, 1.0F);
    }

    public static void removePharaoh(int entityId) {
        PharaohVisualController.activePharaohs.remove(entityId);
        if (PharaohVisualController.activePharaohs.size() <= 0) {
            PharaohVisualController.clear();
        }
    }

    public static void clear() {
        PharaohVisualController.enablePyramid = false;
        PharaohVisualController.sandStormIntensity = 0.0F;
    }
    
}
