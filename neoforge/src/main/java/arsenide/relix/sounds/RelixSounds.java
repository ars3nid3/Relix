package arsenide.relix.sounds;

import arsenide.relix.Relix;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelixSounds {
    
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Relix.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> PHARAOH_THEME = SOUND_EVENTS.register(
        "pharaoh_theme",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_theme")
        )
    );

    public static final ResourceKey<JukeboxSong> PHARAOH_SONG = ResourceKey.create(
        Registries.JUKEBOX_SONG,
        ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_theme")
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PHARAOH_SHOCKWAVE = SOUND_EVENTS.register(
        "pharaoh_shockwave",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_shockwave")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> DILATION = SOUND_EVENTS.register(
        "dilation",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "dilation")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PHARAOH_IDLE = SOUND_EVENTS.register(
        "pharaoh_idle",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_idle")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PHARAOH_HURT = SOUND_EVENTS.register(
        "pharaoh_hurt",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_hurt")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PHARAOH_DEATH = SOUND_EVENTS.register(
        "pharaoh_death",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_death")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PHARAOH_BOLT_SHOOT = SOUND_EVENTS.register(
        "pharaoh_bolt_shoot",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_bolt_shoot")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PHARAOH_SUMMON = SOUND_EVENTS.register(
        "pharaoh_summon",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_summon")
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> EARTHQUAKE = SOUND_EVENTS.register(
        "earthquake",
        () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "earthquake")
        )
    );

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
