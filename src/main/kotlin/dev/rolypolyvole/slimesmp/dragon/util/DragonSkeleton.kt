package dev.rolypolyvole.slimesmp.dragon.util

import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.skeleton.Skeleton
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import kotlin.math.sqrt

class DragonSkeleton(level: Level) : Skeleton(EntityType.SKELETON, level) {

    override fun shouldBeSaved(): Boolean = false

    override fun performRangedAttack(target: LivingEntity, pullProgress: Float) {
        val bowHand = ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)
        val bowStack = this.getItemInHand(bowHand)
        val ammoStack = this.getProjectile(bowStack)
        val arrow = this.getArrow(ammoStack, pullProgress, bowStack)

        val dx = target.x - this.x
        val dy = target.getY(0.3333333333333333) - arrow.y
        val dz = target.z - this.z
        val horizontalDist = sqrt(dx * dx + dz * dz)

        val level = this.level()
        if (level is ServerLevel) {
            Projectile.spawnProjectileUsingShoot(
                arrow, level, ammoStack,
                dx, dy + horizontalDist * 0.2, dz,
                3.0F, 6.0F
            )
        }

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F))
    }
}
