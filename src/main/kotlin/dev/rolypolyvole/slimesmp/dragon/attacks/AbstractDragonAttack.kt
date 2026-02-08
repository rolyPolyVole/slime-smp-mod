package dev.rolypolyvole.slimesmp.dragon.attacks

import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
import kotlin.reflect.KClass

abstract class AbstractDragonAttack(protected val dragon: EnderDragon) {
    protected val phase: DragonPhaseInstance
        get() = dragon.phaseManager.currentPhase

    abstract fun tick()
    open fun beforeMove() {}

    open fun getSpeedMultiplier(): Float = 1.0f
    open fun getTurnSpeedMultiplier(): Float = 1.0f

    abstract fun shouldEnd(): Boolean

    abstract fun start(): Boolean
    abstract fun end()
}

interface DragonAttackType {
    fun canStart(dragon: EnderDragon, lastAttack: AbstractDragonAttack?): Boolean {
        return invalidPhases().none { it.isInstance(dragon.phaseManager.currentPhase) }
    }

    fun invalidPhases(): List<KClass<out DragonPhaseInstance>>
    fun create(dragon: EnderDragon): AbstractDragonAttack
}