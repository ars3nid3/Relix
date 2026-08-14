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

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
