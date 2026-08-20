package arsenide.relix.world;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import arsenide.relix.Relix;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class RelixAttachments {
    
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, 
            Relix.MODID
        );

    private static final Supplier<AttachmentType<Boolean>> TIME_DILATION_SLOW_CONSUME =
        ATTACHMENT_TYPES.register(
            "time_dilation_slow_consume",
            () -> AttachmentType.builder(
                () -> false
            )
            .serialize(Codec.BOOL.fieldOf("time_dilation_slow_consume"))
            .sync(StreamCodec.of(
                ByteBufCodecs.BOOL,
                ByteBufCodecs.BOOL
            ))
            .copyOnDeath()
            .build()
        );

    private static final Supplier<AttachmentType<Boolean>> TIME_DILATION_SLOW_USE =
        ATTACHMENT_TYPES.register(
            "time_dilation_slow_use",
            () -> AttachmentType.builder(
                () -> false
            )
            .serialize(Codec.BOOL.fieldOf("time_diltation_slow_use"))
            .sync(StreamCodec.of(
                ByteBufCodecs.BOOL,
                ByteBufCodecs.BOOL
            ))
            .copyOnDeath()
            .build()
        );

    private static final Supplier<AttachmentType<Boolean>> TIME_DILATION_SLOW_ATTACK =
        ATTACHMENT_TYPES.register(
            "time_dilation_slow_attack",
            () -> AttachmentType.builder(
                () -> false
            )
            .serialize(Codec.BOOL.fieldOf("time_dilation_slow_attack"))
            .sync(StreamCodec.of(
                ByteBufCodecs.BOOL,
                ByteBufCodecs.BOOL
            ))
            .copyOnDeath()
            .build()
        );

    private static final Supplier<AttachmentType<Boolean>> HAS_HEADDRESS =
        ATTACHMENT_TYPES.register(
            "has_headdress",
            () -> AttachmentType.builder(
                () -> false
            )
            .serialize(Codec.BOOL.fieldOf("has_headdress"))
            .sync(StreamCodec.of(
                ByteBufCodecs.BOOL,
                ByteBufCodecs.BOOL
            ))
            .copyOnDeath()
            .build()
        );

    public static boolean hasHeaddress(LivingEntity entity) {
        return entity.getData(HAS_HEADDRESS.get());
    }

    public static void setHasHeaddress(LivingEntity entity, boolean hasHeaddress) {
        entity.setData(HAS_HEADDRESS.get(), hasHeaddress);
    }

    public static boolean hasTimeDilationSlowConsume(LivingEntity entity) {
        return entity.getData(TIME_DILATION_SLOW_CONSUME.get());
    }

    public static void setTimeDilationSlowConsume(LivingEntity entity, boolean timeDilationSlowConsume) {
        entity.setData(TIME_DILATION_SLOW_CONSUME.get(), timeDilationSlowConsume);
    }
    
    
    public static boolean hasTimeDilationSlowUse(LivingEntity entity) {
        return entity.getData(TIME_DILATION_SLOW_USE.get());
    }

    public static void setTimeDilationSlowUse(LivingEntity entity, boolean timeDilationSlowUse) {
        entity.setData(TIME_DILATION_SLOW_USE.get(), timeDilationSlowUse);
    }

    public static boolean hasTimeDilationSlowAttack(LivingEntity entity) {
        return entity.getData(TIME_DILATION_SLOW_ATTACK.get());
    }

    public static void setTimeDilationSlowAttack(LivingEntity entity, boolean timeDilationSlowAttack) {
        entity.setData(TIME_DILATION_SLOW_ATTACK.get(), timeDilationSlowAttack);
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
