package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private MobEffectInstance hideInvisibilityParticles(MobEffectInstance instance) {
        if (instance.getEffect().equals(MobEffects.INVISIBILITY) && instance.isVisible()) {
            return new MobEffectInstance(
                    instance.getEffect(),
                    instance.getDuration(),
                    instance.getAmplifier(),
                    instance.isAmbient(),
                    false,
                    instance.showIcon()
            );
        }

        return instance;
    }
}
