package arsenide.relix.networking;

import arsenide.relix.Relix;
import arsenide.relix.client.PharaohVisualController;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Relix.MODID)
public class RelixPayloads {
    
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
            PharaohVisualPayload.TYPE,
            PharaohVisualPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    if (payload.active()) {
                        PharaohVisualController.addPharaoh(payload.entityId());
                    } else {
                        PharaohVisualController.removePharaoh(payload.entityId());;
                    }
                });
            }
        );
    }

}
