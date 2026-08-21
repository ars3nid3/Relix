package arsenide.relix.networking;

import arsenide.relix.Relix;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PharaohVisualPayload(
    int entityId,
    boolean active
) implements CustomPacketPayload {

    public static final Type<PharaohVisualPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Relix.MODID, "pharaoh_visual"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PharaohVisualPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PharaohVisualPayload::entityId,
            ByteBufCodecs.BOOL, 
            PharaohVisualPayload::active, 
            PharaohVisualPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
