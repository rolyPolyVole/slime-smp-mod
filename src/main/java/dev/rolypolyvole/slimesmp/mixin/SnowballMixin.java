package dev.rolypolyvole.slimesmp.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Snowball.class)
public abstract class SnowballMixin extends ThrowableItemProjectile {

    public SnowballMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(
            method = "onHitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V")
    )
    private void damageOnHit(Entity entity, DamageSource source, float amount) {
        if (entity instanceof SnowGolem) return;

        float damage = entity instanceof Blaze ? 3.0F : 0.0F;

        if (owner != null && owner.getEntity(level(), Entity.class) instanceof SnowGolem) {
            damage += 1.0F;
        }

        entity.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
    }
}
