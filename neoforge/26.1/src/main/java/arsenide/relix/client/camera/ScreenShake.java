package arsenide.relix.client.camera;

import net.minecraft.client.Minecraft;

public class ScreenShake {
    private static float maxIntensity = 0;
    private static int fadeInDuration = 0;
    private static int holdDuration = 0;
    private static int fadeOutDuration = 0;
    private static int age = 0;

    public static void shake(
        float maxIntensity, 
        int fadeInDuration, 
        int holdDuration, 
        int fadeOutDuration
    ) {
        ScreenShake.maxIntensity = maxIntensity;
        ScreenShake.fadeInDuration = fadeInDuration;
        ScreenShake.holdDuration = holdDuration;
        ScreenShake.fadeOutDuration = fadeOutDuration;
        ScreenShake.age = 0;
    }

    public static void tick() {
        int totalDuration = fadeInDuration + holdDuration + fadeOutDuration;
        if (totalDuration <= 0) return;
        age++;
        if (age >= totalDuration) {
            maxIntensity = 0;
        }
    }

    public static float getIntensity() {
        if (age < fadeInDuration) {
            return maxIntensity * (float) age / fadeInDuration;
        } else if (age < fadeInDuration + holdDuration) {
            return maxIntensity;
        } else if (age < fadeInDuration + holdDuration + fadeOutDuration) {
            return maxIntensity * (1 - (float) (age - fadeInDuration - holdDuration) / fadeOutDuration);
        }
        return 0;
    }

    public static float getRandomOffset(float intensity) {
        return (Minecraft.getInstance().level.getRandom().nextFloat() * 2 - 1) * intensity;
    }

}
