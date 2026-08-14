package arsenide.relix.effects;

import arsenide.relix.Relix;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelixEffects {
    
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = 
        DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Relix.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> TIME_DILATION = 
        MOB_EFFECTS.register(
            "time_dilation",
            TimeDilationEffect::new
        );

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}
