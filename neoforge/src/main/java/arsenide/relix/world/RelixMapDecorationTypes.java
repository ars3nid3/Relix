package arsenide.relix.world;

import arsenide.relix.Relix;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelixMapDecorationTypes {
    
    public static final DeferredRegister<MapDecorationType> MAP_DECORATION_TYPES = 
    DeferredRegister.create(BuiltInRegistries.MAP_DECORATION_TYPE, Relix.MODID);

    public static final DeferredHolder<MapDecorationType, MapDecorationType> DESERT_PYRAMID =
    MAP_DECORATION_TYPES.register(
        "desert_pyramid",
        () -> new MapDecorationType(
            ResourceLocation.fromNamespaceAndPath(Relix.MODID, "desert_pyramid"),
            true, 
            MapColor.COLOR_LIGHT_GRAY.col,
            true,
            false
        )
    );

    public static void register(IEventBus eventBus) {
        MAP_DECORATION_TYPES.register(eventBus);
    }
}
