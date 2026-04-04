package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerMixin {

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void increaseHealth(CallbackInfoReturnable<?> info) {
        ZombieVillager self = (ZombieVillager) (Object) this;
        AttributeInstance maxHealth = self.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealth != null) {
            maxHealth.setBaseValue(24.0);
            self.setHealth(self.getMaxHealth());
        }
    }
}
