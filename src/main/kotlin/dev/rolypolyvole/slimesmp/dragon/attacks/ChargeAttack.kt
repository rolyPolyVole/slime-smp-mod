package dev.rolypolyvole.slimesmp.dragon.attacks

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.enderdragon.phases.*
import net.minecraft.world.phys.Vec3

class ChargeAttack(dragon: EnderDragon) : AbstractDragonAttack(dragon) {
    private val phase: DragonPhaseInstance get() = dragon.phaseManager.currentPhase
    private var target: Vec3? = null
    private var prepareTicks = 0

    override fun tick() {
        prepareTicks++
    }

    override fun beforeMove() {
        if (prepareTicks < 20) return
        if (target == null) return

        val to = target!!.subtract(dragon.position())
        if (to.lengthSqr() < 1e-6) return

        val movement = to.normalize()
        val speed = 1.8

        dragon.deltaMovement = movement.scale(speed)
    }

    override fun canStart(lastAttack: AbstractDragonAttack?): Boolean {
        return phase !is DragonChargePlayerPhase &&
                phase !is DragonLandingApproachPhase &&
                phase !is DragonLandingPhase &&
                phase !is DragonDeathPhase &&
                phase !is AbstractDragonSittingPhase
    }

    override fun shouldEnd(): Boolean {
        return phase !is DragonChargePlayerPhase
    }

    override fun start() {
        val target = dragon.level().players()
            .filter { !it.isCreative && !it.isSpectator && it.isAlive }
            .filter { it.position().distanceToSqr(dragon.position()) in 400.0..22500.0 }
            .randomOrNull() ?: return

        this.target = target.position()

        dragon.phaseManager.setPhase(EnderDragonPhase.CHARGING_PLAYER)
        dragon.phaseManager.getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(this.target!!)

        dragon.level().server!!.playerList.broadcastSystemMessage(Component.literal("Dragon is charging towards player"), false)
    }

    override fun end() {
        dragon.level().server!!.playerList.broadcastSystemMessage(Component.literal("Dragon finished charging attack"), false)
        return
    }

    override fun getStartDelayTicks(): Int = (12..20).random() * 20
}