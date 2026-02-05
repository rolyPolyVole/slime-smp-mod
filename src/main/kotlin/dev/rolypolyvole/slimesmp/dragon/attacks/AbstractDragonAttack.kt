package dev.rolypolyvole.slimesmp.dragon.attacks

import net.minecraft.world.entity.boss.enderdragon.EnderDragon

abstract class AbstractDragonAttack(protected val dragon: EnderDragon) {
    abstract fun tick()
    open fun beforeMove() {}

    open fun canStart(lastAttack: AbstractDragonAttack?): Boolean = true
    open fun shouldEnd(): Boolean = false

    abstract fun start()
    abstract fun end()

    abstract fun getStartDelayTicks(): Int
}