package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SnowGolem.class)
public abstract class SnowGolemMixin {

    @Redirect(
            method = "registerGoals",
            at = @At(value = "NEW", target = "net/minecraft/world/entity/ai/goal/RangedAttackGoal")
    )
    private RangedAttackGoal fasterAttackGoal(RangedAttackMob mob, double speed, int interval, float range) {
        return new RangedAttackGoal(mob, speed, 2, (int) (range * 1.5));
    }
}
