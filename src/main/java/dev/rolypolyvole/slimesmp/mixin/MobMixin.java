package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "getAttackBoundingBox", at = @At("RETURN"), cancellable = true)
    private void extendSlimeAttackRange(double reach, CallbackInfoReturnable<AABB> info) {
        Mob self = (Mob) ((Object) this);

        if (self instanceof Slime slime && slime.getSize() == 8) {
            info.setReturnValue(info.getReturnValue().inflate(1.0, 0.0, 1.0));
        }
    }
}
