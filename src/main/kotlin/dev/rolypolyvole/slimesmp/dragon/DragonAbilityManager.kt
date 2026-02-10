package dev.rolypolyvole.slimesmp.dragon

import dev.rolypolyvole.slimesmp.util.toVec3
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.feature.SpikeFeature
import net.minecraft.world.phys.AABB


class DragonAbilityManager(private val dragon: EnderDragon) {
    private val level = dragon.level() as ServerLevel

    private var ticks = 0
    private var ticksUntilCrystalRespawn = 0

    fun tick () {
        this.ticks++

        if (ticksUntilCrystalRespawn > 0) {
            this.ticksUntilCrystalRespawn--
        } else {
            respawnCrystal()
            this.ticksUntilCrystalRespawn = (200..300).random() + (getTotalCrystals() * 10)
        }
    }

    private fun respawnCrystal() {
        val pos = SpikeFeature.getSpikesForLevel(level)
            .map { BlockPos(it.centerX, it.height + 1, it.centerZ) }
            .filterNot(::doesCrystalExist)
            .randomOrNull() ?: return

        val crystal = EntityType.END_CRYSTAL.create(level, EntitySpawnReason.EVENT) ?: return
        val vec3 = pos.toVec3().add(0.5, 0.5, 0.5)

        level.explode(
            null,
            vec3.x, vec3.y, vec3.z,
            6.0F, false,
            Level.ExplosionInteraction.TRIGGER
        )

        crystal.setPos(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        crystal.setShowBottom(true)
        crystal.beamTarget = null

        level.addFreshEntity(crystal)

        dragon.level().players().forEach {
            it.displayClientMessage(
                net.minecraft.network.chat.Component.literal("An end crystal has respawned!"),
                false
            )
        }
    }

    private fun doesCrystalExist(pos: BlockPos): Boolean {
        return !level.getEntitiesOfClass(EndCrystal::class.java, AABB(pos).inflate(1.5)).isEmpty()
    }

    private fun getTotalCrystals(): Int {
        return level.allEntities.filterIsInstance<EndCrystal>().size
    }
}