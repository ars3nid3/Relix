package arsenide.relix.entity;

import arsenide.relix.Relix;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Relix.MODID)
public class RelixEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Relix.MODID);
    
    public static final DeferredHolder<EntityType<?>, EntityType<PharaohSpawnSceneEntity>> PHARAOH_SPAWN_SCENE_ENTITY =
        ENTITY_TYPES.register(
            "pharaoh_spawn_scene",
            () -> EntityType.Builder.<PharaohSpawnSceneEntity>of(
                PharaohSpawnSceneEntity::new,
                MobCategory.MISC
            ).build("pharaoh_spawn_scene")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<AttendantEntity>> ATTENDANT_ENTITY = 
        ENTITY_TYPES.register(
            "attendant",
            () -> EntityType.Builder.<AttendantEntity>of(
                AttendantEntity::new,
                MobCategory.MONSTER
            ).build("attendant")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<PharaohEntity>> PHARAOH_ENTITY = 
        ENTITY_TYPES.register(
            "pharaoh", 
            () -> EntityType.Builder.<PharaohEntity>of(
                PharaohEntity::new,
                MobCategory.MONSTER
            )
            .sized(1.25F, 2.75F)
            .eyeHeight(2.625F)
            .clientTrackingRange(10)
            .fireImmune()
            .build("pharaoh")
        );

    public static final DeferredHolder<EntityType<?>, EntityType<PharaohBoltEntity>> PHARAOH_BOLT_ENTITY =
        ENTITY_TYPES.register(
            "pharaoh_bolt",
            () -> EntityType.Builder.<PharaohBoltEntity>of(
                PharaohBoltEntity::new,
                MobCategory.MISC
            )
            .sized(0.3125F, 0.3125F)
            .clientTrackingRange(4)
            .updateInterval(1)
            .build("pharaoh_bolt")
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
