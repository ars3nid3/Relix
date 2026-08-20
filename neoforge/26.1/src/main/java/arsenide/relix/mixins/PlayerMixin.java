package arsenide.relix.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import arsenide.relix.effects.RelixEffects;
import arsenide.relix.world.RelixAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;

@Mixin(Player.class)
public class PlayerMixin {

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemCooldowns;tick()V"
        )
    )
    private void redirectTick(ItemCooldowns cooldowns) {
        Player player = (Player)(Object)this;

        if (!player.hasEffect(RelixEffects.TIME_DILATION)) {
            if (RelixAttachments.hasTimeDilationSlowConsume(player)) {
                RelixAttachments.setTimeDilationSlowConsume(player, false);
            } else {
                cooldowns.tick();
                RelixAttachments.setTimeDilationSlowConsume(player, true);
            }
        } else if (RelixAttachments.hasTimeDilationSlowConsume(player)) {
            RelixAttachments.setTimeDilationSlowConsume(player, false);
        }
    }
    
}
