package dev.rolypolyvole.slimesmp.dragon.attacks

import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.enderdragon.phases.*
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball
import net.minecraft.world.phys.Vec3
import kotlin.reflect.KClass

class FireballAttack(dragon: EnderDragon) : AbstractDragonAttack(dragon) {
    private val random: Double
        get() = Math.random()

    private val outpost = this.getOutpostLocation()
    private var reachedOutpost = false

    private var ticks = 0

    private var ticksUntilChangeTarget = 0
    private var target: ServerPlayer? = null

    private var targeted = 0
    private var shouldEnd: Boolean = false

    override fun tick() {
        if (!reachedOutpost) { return }

        if (ticks == 0) dragon.phaseManager.setPhase(EnderDragonPhase.HOVERING)
        if (ticks++ < 40) return

        if (ticks > 15 * 20) {
            shouldEnd = true
            return
        }

        if (ticksUntilChangeTarget > 0) {
            ticksUntilChangeTarget--
        } else {
            if (random < (targeted / (targeted + 3.0))) {
                shouldEnd = true
                return
            }

            this.target = dragon.level().players()
                .filter { !it.isCreative && !it.isSpectator && it.isAlive }
                .filter { it.position().distanceToSqr(dragon.position()) < 22500.0 }
                .filter { dragon.hasLineOfSight(it) }
                .map { it as ServerPlayer }
                .randomOrNull() ?: run { shouldEnd = true; return }

            this.ticksUntilChangeTarget = (80..145).random()
            this.targeted++
        }

        val target = this.target
        if (target == null || target.hasDisconnected()) {
            this.ticksUntilChangeTarget = 0
            return
        }

        val vector = target.position().subtract(dragon.position()).multiply(-1.0, -1.0, -1.0)
        dragon.lookAt(EntityAnchorArgument.Anchor.FEET, dragon.position().add(vector))

        if (ticks % 10 == 0) {
            shootFireballAt(target.eyePosition)
        }
    }

    override fun beforeMove() {
        if (reachedOutpost) return

        val delta = outpost.subtract(dragon.position())

        if (delta.lengthSqr() < 256.0) {
            reachedOutpost = true
            dragon.deltaMovement = Vec3.ZERO
            dragon.level().players()
                .forEach { it.displayClientMessage(Component.literal("reached outpost"), false) }
            return
        }

        val speed = 1.0
        val direction = delta.normalize()
        dragon.deltaMovement = direction.scale(speed)
    }

    override fun start() {
        dragon.phaseManager.setPhase(EnderDragonPhase.CHARGING_PLAYER)
        dragon.phaseManager.getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(outpost)

        dragon.level().players().forEach { it.displayClientMessage(Component.literal("started fireball attack"), false) }
        return
    }

    override fun end() {
        dragon.phaseManager.setPhase(EnderDragonPhase.HOLDING_PATTERN)
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("ending fireball attack"), false) }
        return
    }

    override fun getSpeedMultiplier(): Float = 1.0f
    override fun getTurnSpeedMultiplier(): Float = 1.0f

    override fun invalidPhases(): List<KClass<out DragonPhaseInstance>> = listOf(
        DragonLandingApproachPhase::class,
        DragonLandingPhase::class,
        DragonDeathPhase::class,
        AbstractDragonSittingPhase::class
    )

    override fun canStart(lastAttack: AbstractDragonAttack?): Boolean {
        return super.canStart(lastAttack) && lastAttack !is FireballAttack
    }

    override fun shouldEnd(): Boolean = shouldEnd

    override fun getStartDelayTicks(): Int = (20..35).random() * 20

    private fun getOutpostLocation(): Vec3 {
        return dragon.fightOrigin.center.add(
            random * 100.0 - 50.0,
            100.0 + random * 35.0,
            random * 100.0 - 50.0
        )
    }

    private fun shootFireballAt(targetPos: Vec3) {
        val level = dragon.level() as? ServerLevel ?: return

        val view = dragon.getViewVector(1.0f)
        val head = dragon.head

        val x = head.x - view.x * 1.0
        val y = head.getY(0.5) + 0.5
        val z = head.z - view.z * 1.0

        val raw = Vec3(targetPos.x - x, targetPos.y - y, targetPos.z - z)
        if (raw.lengthSqr() < 1e-6) return
        val direction = raw.normalize()

        level.levelEvent(null, 1017, dragon.blockPosition(), 0)

        DragonFireball(level, dragon, direction).let {
            it.snapTo(x, y, z, 0.0f, 0.0f)
            level.addFreshEntity(it)
        }
    }
}