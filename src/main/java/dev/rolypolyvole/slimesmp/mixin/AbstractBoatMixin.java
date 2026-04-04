package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class AbstractBoatMixin {

    @Unique
    private boolean isBoatLeashedToGhast() {
        Entity self = (Entity) ((Object) this);

        return self instanceof AbstractBoat &&
               self instanceof Leashable leashable &&
               leashable.isLeashed() &&
               leashable.getLeashHolder() instanceof HappyGhast;
    }

    @Inject(method = "isClientAuthoritative", at = @At("HEAD"), cancellable = true)
    private void setServerControlled(CallbackInfoReturnable<Boolean> info) {
        if (isBoatLeashedToGhast()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "getKnownMovement", at = @At("HEAD"), cancellable = true)
    private void alwaysUseBoatMovement(CallbackInfoReturnable<Vec3> info) {
        Entity self = (Entity) ((Object) this);

        if (isBoatLeashedToGhast()) {
            info.setReturnValue(self.getDeltaMovement());
        }
    }
}
