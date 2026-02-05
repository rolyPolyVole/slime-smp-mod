package dev.rolypolyvole.slimesmp.dragon.attacks

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.enderdragon.phases.*
import net.minecraft.world.phys.Vec3
import kotlin.reflect.KClass

class ChargeAttack(dragon: EnderDragon) : AbstractDragonAttack(dragon) {
    private var target: ServerPlayer? = null
    private var targetLocation: Vec3? = null
    private var prepareTicks = 0

    override fun tick() {
        prepareTicks++
    }

    override fun beforeMove() {
        if (prepareTicks < 20) return
        if (prepareTicks == 20) this.targetLocation = this.getTargetLocation()
        if (target == null || target!!.hasDisconnected()) return

        val to = targetLocation!!.subtract(dragon.position())
        if (to.lengthSqr() < 1e-6) return

        val movement = to.normalize()
        val speed = 1.8

        dragon.deltaMovement = movement.scale(speed)
    }

    override fun getTurnSpeedMultiplier(): Float {
        return 2.5F
    }

    override fun invalidPhases(): List<KClass<out DragonPhaseInstance>> = listOf(
        DragonChargePlayerPhase::class,
        DragonLandingApproachPhase::class,
        DragonLandingPhase::class,
        DragonDeathPhase::class,
        AbstractDragonSittingPhase::class
    )

    override fun shouldEnd(): Boolean {
        return phase !is DragonChargePlayerPhase
    }

    override fun start() {
        this.target = dragon.level().players()
            .filter { !it.isCreative && !it.isSpectator && it.isAlive }
            .filter { it.position().distanceToSqr(dragon.position()) in 900.0..22500.0 }
            .map { it as ServerPlayer }
            .randomOrNull() ?: return

        this.targetLocation = this.getTargetLocation()

        dragon.phaseManager.setPhase(EnderDragonPhase.CHARGING_PLAYER)
        dragon.phaseManager.getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(targetLocation!!)

        dragon.level().players().forEach { it.displayClientMessage(Component.literal("start dragon charge"), false) }
    }

    override fun end() {
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("stop dragon charge"), false) }
        return
    }

    override fun getStartDelayTicks(): Int = (12..20).random() * 20

    private fun getTargetLocation(): Vec3 {
        val dragonPos = dragon.position()
        val followThrough = target!!.position().subtract(dragonPos).normalize().scale(8.0)

        return target!!.position().add(followThrough)
    }
}