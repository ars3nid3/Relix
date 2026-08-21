package arsenide.relix.entity;

import java.util.function.Supplier;

import arsenide.relix.Relix;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Relix.MODID)
public class RelixEntities {

    public static final DeferredRegister.Entities ENTITY_TYPES =
        DeferredRegister.createEntities(Relix.MODID);
    
    public static final Supplier<EntityType<PharaohSpawnSceneEntity>> PHARAOH_SPAWN_SCENE_ENTITY =
        ENTITY_TYPES.registerEntityType(
            "pharaoh_spawn_scene", 
            PharaohSpawnSceneEntity::new,
            MobCategory.MISC
    );
    public static final Supplier<EntityType<AttendantEntity>> ATTENDANT_ENTITY = 
        ENTITY_TYPES.registerEntityType(
            "attendant",
            AttendantEntity::new,
            MobCategory.MONSTER
    );
    public static final Supplier<EntityType<PharaohEntity>> PHARAOH_ENTITY = 
        ENTITY_TYPES.register(
            "pharaoh", 
            () -> EntityType.Builder.of(
                PharaohEntity::new,
                MobCategory.MONSTER
            )
            .sized(1.25F, 2.75F)
            .eyeHeight(2.625F)
            .clientTrackingRange(10)
            .fireImmune()
            .build(ResourceKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Relix.MODID, "pharaoh")
            ))
        );

    public static final Supplier<EntityType<PharaohBoltEntity>> PHARAOH_BOLT_ENTITY =
        ENTITY_TYPES.registerEntityType(
            "pharaoh_bolt",
            PharaohBoltEntity::new,
            MobCategory.MISC,
            builder -> builder
                .sized(0.3125F, 0.3125F)
                .clientTrackingRange(4)
                .updateInterval(1)
        );


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(
            ATTENDANT_ENTITY.get(),
            AttendantEntity.getDefaultAttributes()
        );
        event.put(
            PHARAOH_ENTITY.get(),
            PharaohEntity.getDefaultAttributes()
        );
    }
}
