package dev.rolypolyvole.slimesmp.dragon.attacks

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon

class DummyAttack(dragon: EnderDragon) : AbstractDragonAttack(dragon) {
    override fun tick() {
        if ((0..80).random() == 0) {
            val level = dragon.level() as ServerLevel
            val pos = dragon.blockPosition()

            EntityType.TNT_MINECART.spawn(level, pos, EntitySpawnReason.MOB_SUMMONED)
        }
    }

    override fun start() {
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("starting dummy attack"), false) }
        return
    }

    override fun end() {
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("finished dummy attack"), false) }
        return
    }

    override fun getSpeedMultiplier(): Float = 1.0f
    override fun getTurnSpeedMultiplier(): Float = 1.0f

    override fun canStart(lastAttack: AbstractDragonAttack?): Boolean = true
    override fun shouldEnd(): Boolean = (0..300).random() == 0

    override fun getStartDelayTicks(): Int = (10..30).random() * 20
}