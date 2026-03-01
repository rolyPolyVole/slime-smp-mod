package dev.rolypolyvole.slimesmp.dragon

import dev.rolypolyvole.slimesmp.dragon.util.PhantomRiderSpawner
import dev.rolypolyvole.slimesmp.worldgen.CustomEndSpikes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt


class DragonAbilityManager(private val dragon: EnderDragon) {
    private val level = dragon.level() as ServerLevel

    private var ticks = 0
    private var ticksUntilCrystalRespawn = 500
    private var ticksUntilMobSpawn = (100..200).random()

    fun tick () {
        this.ticks++

        if (ticksUntilCrystalRespawn > 0) {
            this.ticksUntilCrystalRespawn--
        } else {
            respawnCrystal()

            val playerCount = nearbyPlayers().size.coerceAtLeast(1)
            val base = 400 + ((300..400).random() / sqrt(playerCount.toDouble())).toInt()
            this.ticksUntilCrystalRespawn = base + (getTotalCrystals() * 40)
        }

        if (ticksUntilMobSpawn > 0) {
            this.ticksUntilMobSpawn--
        } else {
            spawnPhantomRider()
            this.ticksUntilMobSpawn = (100..200).random()
        }
    }

    private fun nearbyPlayers(): List<ServerPlayer> {
        val center = dragon.fightOrigin.center
        return level.players().filter{ it.position().distanceToSqr(center) < 40000.0 }
    }

    private fun respawnCrystal() {
        val pos = CustomEndSpikes.getCrystalLocations(level)
            .filterNot(::doesCrystalExist)
            .randomOrNull() ?: return

        val crystal = EntityType.END_CRYSTAL.create(level, EntitySpawnReason.EVENT) ?: return

        crystal.setPos(pos)
        crystal.setShowBottom(false)
        crystal.playSound(SoundEvents.BEACON_ACTIVATE)

        level.addFreshEntity(crystal)

        nearbyPlayers().forEach {
            it.sendSystemMessage(Component.literal("An End Crystal has respawned!").withColor(0xFF55FF))
        }
    }

    private fun spawnPhantomRider() {
        val crystalPos = CustomEndSpikes.getCrystalLocations(level)
            .filter(::doesCrystalExist)
            .randomOrNull() ?: return

        val spawnPos = crystalPos.add(0.0, 5.0, 0.0)

        if (Math.random() < 0.5) {
            PhantomRiderSpawner.spawnArcher(level, spawnPos)

            nearbyPlayers().forEach {
                it.sendSystemMessage(Component.literal("A Phantom Archer descends from the sky...").withColor(0xFF00DD))
            }
        } else {
            PhantomRiderSpawner.spawnHoplite(level, spawnPos)

            nearbyPlayers().forEach {
                it.sendSystemMessage(Component.literal("A Phantom Hoplite emerges from the void...").withColor(0xFF00BB))
            }
        }
    }

    private fun doesCrystalExist(vec: Vec3): Boolean {
        val aabb = AABB(vec.add(-0.5, -0.5, -0.5), vec.add(0.5, 0.5, 0.5)).inflate(1.5)

        return !level.getEntitiesOfClass(EndCrystal::class.java, aabb).isEmpty()
    }

    private fun getTotalCrystals(): Int {
        return CustomEndSpikes.getCrystalLocations(level).filter(::doesCrystalExist).size
    }
}