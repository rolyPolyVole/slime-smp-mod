package dev.rolypolyvole.slimesmp.dragon.util

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.monster.Phantom
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

class DragonPhantom(level: Level) : Phantom(EntityType.PHANTOM, level) {

    companion object {
        private const val SPEED_MULTIPLIER = 0.5
    }

    init {
        getAttribute(Attributes.MAX_HEALTH)?.baseValue = 40.0
        getAttribute(Attributes.ARMOR)?.baseValue = 8.0
        health = maxHealth
    }

    override fun shouldBeSaved(): Boolean = false

    // Prevent the skeleton rider from ever taking control
    override fun getControllingPassenger(): LivingEntity? = null

    override fun travel(movementInput: Vec3) {
        // Scale velocity before flight movement is applied
        // Vanilla PhantomMoveControl computes deltaMovement, then travel() applies it
        val dm = deltaMovement
        deltaMovement = dm.normalize().scale(SPEED_MULTIPLIER)
        super.travel(movementInput)
    }
}
