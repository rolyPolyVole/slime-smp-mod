package dev.rolypolyvole.slimesmp.dragon

import dev.rolypolyvole.slimesmp.dragon.attacks.AbstractDragonAttack
import dev.rolypolyvole.slimesmp.dragon.attacks.ChargeAttack
import dev.rolypolyvole.slimesmp.dragon.attacks.DummyAttack
import dev.rolypolyvole.slimesmp.dragon.attacks.FireballAttack
import net.minecraft.world.entity.boss.enderdragon.EnderDragon

class DragonAttackManager(private val dragon: EnderDragon) {
    private val attacks = listOf(
        ::ChargeAttack,
        ::FireballAttack,
        ::DummyAttack
    )

    private var currentAttack: AbstractDragonAttack? = null
    private var lastAttack: AbstractDragonAttack? = null
    private var nextAttack: AbstractDragonAttack? = null

    private var ticksUntilNextAttack: Int = 0

    val speedMultiplier: Float
        get() = currentAttack?.getSpeedMultiplier() ?: 1.0F
    val turnSpeedMultiplier: Float
        get() = currentAttack?.getTurnSpeedMultiplier() ?: 1.0F

    fun tick() {
        currentAttack?.let {
            it.tick()

            if (it.shouldEnd()) {
                it.end()
                this.lastAttack = it
                this.currentAttack = null
            }

            return
        }

        if (ticksUntilNextAttack > 0) {
            this.ticksUntilNextAttack--
            return
        }

        if (nextAttack != null) {
            this.currentAttack = nextAttack
            this.nextAttack = null

            return currentAttack!!.start()
        }

        val next = attacks
            .map { it(dragon) }
            .filter { it.canStart(lastAttack) }
            .randomOrNull() ?: return

        this.ticksUntilNextAttack = next.getStartDelayTicks()
        this.nextAttack = next
    }

    fun onBeforeMove() {
        currentAttack?.beforeMove()
    }
}