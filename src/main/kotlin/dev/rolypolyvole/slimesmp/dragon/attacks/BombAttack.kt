package dev.rolypolyvole.slimesmp.dragon.attacks

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.enderdragon.phases.DragonDeathPhase
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingApproachPhase
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
import kotlin.reflect.KClass

class BombAttack(dragon: EnderDragon) : AbstractDragonAttack(dragon) {
    private var lifetime = (160..240).random()
    private var nextBomb = (20..40).random()
    private var shouldEnd = false

    object BombAttackType : DragonAttackType {
        override fun invalidPhases(): List<KClass<out DragonPhaseInstance>> = listOf(
            DragonLandingApproachPhase::class,
            DragonLandingPhase::class,
            DragonDeathPhase::class,
        )

        override fun canStart(dragon: EnderDragon, lastAttack: AbstractDragonAttack?): Boolean {
            return super.canStart(dragon, lastAttack) && lastAttack !is BombAttack
        }

        override fun create(dragon: EnderDragon): AbstractDragonAttack {
            return BombAttack(dragon)
        }
    }

    override fun tick() {
        if (lifetime-- <= 0) {
            this.shouldEnd = true
            return
        }

        if (nextBomb-- == 0) {
            val level = dragon.level() as ServerLevel
            val pos = dragon.blockPosition()

            EntityType.TNT_MINECART.spawn(level, pos, EntitySpawnReason.MOB_SUMMONED)

            this.nextBomb = (20..40).random()
        }
    }

    override fun start(): Boolean {
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("starting dummy attack"), false) }
        return true
    }

    override fun end() {
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("finished dummy attack"), false) }
        return
    }

    override fun getSpeedMultiplier(): Float = 1.0f
    override fun getTurnSpeedMultiplier(): Float = 1.0f

    override fun shouldEnd(): Boolean = shouldEnd
}