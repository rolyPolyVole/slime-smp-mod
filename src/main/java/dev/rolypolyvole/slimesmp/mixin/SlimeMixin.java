package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Slime.class)
public abstract class SlimeMixin {

    @Unique
    private Slime self() {
        return (Slime) ((Object) this);
    }

    @Unique
    private AttributeInstance attribute(Holder<Attribute> attribute) {
        return Objects.requireNonNull(self().getAttribute(attribute));
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void massiveSlimes(CallbackInfoReturnable<?> info) {
        Slime self = self();

        if (self.getSize() == 4 && self.getRandom().nextFloat() < 0.05F) {
            attribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(8.0);
            attribute(Attributes.FALL_DAMAGE_MULTIPLIER).setBaseValue(0.0);
            attribute(Attributes.JUMP_STRENGTH).setBaseValue(0.6);
            attribute(Attributes.STEP_HEIGHT).setBaseValue(1.0);
            attribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0);
            attribute(Attributes.MAX_HEALTH).setBaseValue(60.0);

            self.setSize(8, true);
            self.setHealth(self.getMaxHealth());
        }
    }


    @Inject(method = "dealDamage", at = @At("TAIL"))
    private void moreKnockback(LivingEntity target, CallbackInfo info) {
        Slime self = self();

        double knockback = self.getAttributeValue(Attributes.ATTACK_KNOCKBACK);

        if (knockback > 0) {
            double dx = self.getX() - target.getX();
            double dz = self.getZ() - target.getZ();

            target.knockback(knockback, dx, dz);
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.5, 0.0));
        }
    }
}
