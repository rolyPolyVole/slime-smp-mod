package dev.rolypolyvole.slimesmp.dragon.attacks

import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.enderdragon.phases.*
import net.minecraft.world.phys.Vec3
import kotlin.reflect.KClass

class ChargeAttack(dragon: EnderDragon) : AbstractDragonAttack(dragon) {

    object ChargeAttackType : DragonAttackType {
        override fun invalidPhases(): List<KClass<out DragonPhaseInstance>> = listOf(
            DragonChargePlayerPhase::class,
            DragonLandingApproachPhase::class,
            DragonLandingPhase::class,
            DragonDeathPhase::class,
            AbstractDragonSittingPhase::class
        )

        override fun create(dragon: EnderDragon): AbstractDragonAttack {
            return ChargeAttack(dragon)
        }
    }

    private var timer = 0

    private var target: ServerPlayer? = null
    private var targetLocation: Vec3? = null
    private var direction: Vec3? = null

    private var resurfacing = false
    private var shouldEnd = false

    override fun tick() {
        this.timer++

        if (targetLocation != null) {
            val vector = targetLocation!!.subtract(dragon.position()).reverse()
            dragon.lookAt(EntityAnchorArgument.Anchor.FEET, dragon.position().add(vector))
        }

        if (direction == null) return

        if (phase is DragonHoldingPatternPhase && !resurfacing) {
            this.resurfacing = true
            this.timer = 0

            val horizontal = direction!!.multiply(1.0, 0.0, 1.0).normalize()
            val newDirection = Vec3(horizontal.x, 1.0, horizontal.z).normalize()

            this.targetLocation = dragon.position().add(newDirection.scale(50.0))

            dragon.phaseManager.setPhase(EnderDragonPhase.CHARGING_PLAYER)
            dragon.phaseManager.getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(targetLocation!!)
        }
    }

    override fun beforeMove() {
        if (!resurfacing && target == null || target!!.hasDisconnected()) run { this.shouldEnd = true; return }
        if (timer < 20) return

        if (timer == 20) {
            this.targetLocation = this.getTargetLocation()
            this.direction = targetLocation!!.subtract(dragon.position()).normalize()
        }

        val to = targetLocation!!.subtract(dragon.position())
        val movement = to.normalize()
        val speed = if (resurfacing) 1.2 else 1.8

        dragon.deltaMovement = movement.scale(speed)
    }

    override fun getTurnSpeedMultiplier(): Float {
        return 2.5F
    }

    override fun shouldEnd(): Boolean {
        return shouldEnd || (phase !is DragonChargePlayerPhase && resurfacing) || timer > 200
    }

    override fun start(): Boolean {
        this.target = dragon.level().players()
            .filter { !it.isCreative && !it.isSpectator && it.isAlive }
            .filter { it.position().distanceToSqr(dragon.position()) in 900.0..22500.0 }
            .map { it as ServerPlayer }
            .randomOrNull() ?: run { this.shouldEnd = true; return false }

        this.targetLocation = this.getTargetLocation()

        dragon.phaseManager.setPhase(EnderDragonPhase.CHARGING_PLAYER)
        dragon.phaseManager.getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(targetLocation!!)

        dragon.level().players().forEach { it.displayClientMessage(Component.literal("start dragon charge"), false) }

        return true
    }

    override fun end() {
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("stop dragon charge"), false) }
        return
    }

    private fun getTargetLocation(): Vec3 {
        val dragonPos = dragon.position()
        val followThrough = target!!.position().subtract(dragonPos).normalize().scale(8.0)

        return target!!.position().add(followThrough)
    }
}