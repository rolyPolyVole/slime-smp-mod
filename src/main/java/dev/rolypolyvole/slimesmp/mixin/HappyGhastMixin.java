package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HappyGhast.class)
public abstract class HappyGhastMixin {

    @Unique
    private static final double BASE_FLYING_SPEED = 0.05;
    @Unique
    private static final double BOOSTED_FLYING_SPEED = 0.09;

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void modifyAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> info) {
        info.setReturnValue(info.getReturnValue()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.TEMPT_RANGE, 32.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
        );
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void adjustFlyingSpeed(CallbackInfo info) {
        HappyGhast self = (HappyGhast) (Object) this;
        AttributeInstance flyingSpeed = self.getAttribute(Attributes.FLYING_SPEED);
        assert flyingSpeed != null;

        boolean hasPassenger = self.hasControllingPassenger();
        flyingSpeed.setBaseValue(hasPassenger ? BOOSTED_FLYING_SPEED : BASE_FLYING_SPEED);
    }
}
