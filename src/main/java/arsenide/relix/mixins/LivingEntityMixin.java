package arsenide.relix.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import arsenide.relix.effects.RelixEffects;
import arsenide.relix.items.RelixItems;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    
    @Shadow
    protected int attackStrengthTicker;

    @Inject(
        method = "tick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void checkTimeDilationForAttack(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        if (
            livingEntity instanceof Player player &&
            player.hasEffect(RelixEffects.TIME_DILATION)
        ) {
            attackStrengthTicker--;
        }
    }

    @ModifyVariable(
        method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
        at = @At("HEAD"),
        argsOnly = true
    )
    private MobEffectInstance scaleEffectDuration(MobEffectInstance effectInstance) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        if (!(livingEntity instanceof Player player)) {
            return effectInstance;
        }
        if (
            player.getItemBySlot(EquipmentSlot.HEAD).getItem() == RelixItems.SACRED_GOLD_HELMET.get()
            || player.getItemBySlot(EquipmentSlot.CHEST).getItem() == RelixItems.SACRED_GOLD_CHESTPLATE.get()
            || player.getItemBySlot(EquipmentSlot.LEGS).getItem() == RelixItems.SACRED_GOLD_LEGGINGS.get()
            || player.getItemBySlot(EquipmentSlot.FEET).getItem() == RelixItems.SACRED_GOLD_BOOTS.get()
        ) {
            MobEffectCategory effectCategory = effectInstance.getEffect().value().getCategory();
            if (effectCategory == MobEffectCategory.HARMFUL) {
                return effectInstance.withScaledDuration(0.25F);
            } else {
                return effectInstance.withScaledDuration(0.75F);
            }
        }
        return effectInstance;
    }
}
