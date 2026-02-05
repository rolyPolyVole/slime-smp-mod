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

    open fun invalidPhases(): List<KClass<out DragonPhaseInstance>> = emptyList()
    open fun canStart(lastAttack: AbstractDragonAttack?): Boolean = invalidPhases().none { it.isInstance(phase) }
    abstract fun shouldEnd(): Boolean

    abstract fun start()
    abstract fun end()

    abstract fun getStartDelayTicks(): Int
}