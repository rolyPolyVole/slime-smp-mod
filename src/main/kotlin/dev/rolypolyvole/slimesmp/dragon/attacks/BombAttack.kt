package dev.rolypolyvole.slimesmp.dragon.attacks

import com.mojang.math.Transformation
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.PowerParticleOption
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED
import net.minecraft.world.entity.EntitySpawnReason.TRIGGERED
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.enderdragon.phases.*
import net.minecraft.world.level.block.Blocks
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.reflect.KClass


class BombAttack(dragon: EnderDragon) : AbstractDragonAttack(dragon) {
    private var lifetime = (160..240).random()
    private var nextBomb = (20..40).random()
    private var shouldEnd = false

    private val bombs = mutableListOf<Display.BlockDisplay>()

    object BombAttackType : DragonAttackType {
        override fun invalidPhases(): List<KClass<out DragonPhaseInstance>> = listOf(
            AbstractDragonSittingPhase::class,
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

            if (lifetime >= -40) {
                bombs.forEach(Display.BlockDisplay::discard)
                bombs.clear()
            }
        }

        bombs.toList()
            .filter { it.vehicle == null || it.vehicle!!.isRemoved }
            .forEach { it.discard(); bombs.remove(it); }

        bombs.forEach(::animateBomb)

        if (nextBomb-- == 0) {
            val pos = dragon.blockPosition()

            if (!level.getBlockState(pos).isAir) {
                this.nextBomb = 10
                return
            }

            EntityType.TNT_MINECART.spawn(level, pos, MOB_SUMMONED)?.let {
                it.setDeltaMovement(
                    Math.random() * 0.15 - 0.15,
                    -Math.random() * 0.6,
                    Math.random() * 0.15 - 0.15
                )

                it.fallDistance = 3.0

                createDisplay(it)
            }

            this.nextBomb = (20..40).random()
        }
    }

    override fun start(): Boolean {
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("starting bomb attack"), false) }
        return true
    }

    override fun end() {
        dragon.level().players().forEach { it.displayClientMessage(Component.literal("finished bomb attack"), false) }
        return
    }

    override fun getSpeedMultiplier(): Float = 1.0f
    override fun getTurnSpeedMultiplier(): Float = 1.0f

    override fun shouldEnd(): Boolean = shouldEnd && bombs.isEmpty()

    private fun createDisplay(entity: Entity) {
        EntityType.BLOCK_DISPLAY.spawn(level, entity.blockPosition(), TRIGGERED)?.let {
            it.blockState = Blocks.DRAGON_HEAD.defaultBlockState()
            it.startRiding(entity)
            it.setTransformation(DISPLAY_TRANSFORMATION)
            it.yRot = entity.yRot - 90.0F
            it.setGlowingTag(true)
            it.glowColorOverride = 0xFF00FF
            bombs.add(it)
        }
    }

    private fun animateBomb(bomb: Display.BlockDisplay) {
        val age = bomb.tickCount.toFloat()

        bomb.transformationInterpolationDuration = 1

        val matrix = Matrix4f()
            .translationRotateScale(Vector3f(), Quaternionf().rotateY(age * 0.1f), Vector3f(DISPLAY_SCALE))
            .translate(DISPLAY_TRANSLATION)
            .translate(0.35f, 0.1f, 0.4025f) // ABSOLUTE MAGIC NUMBER

        bomb.setTransformation(Transformation(matrix))

        bomb.vehicle?.yRot -= Math.toDegrees(0.1).toFloat()

        level.sendParticles(
            PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0f),
            bomb.x, bomb.y, bomb.z,
            1, 0.0, 0.0, 0.0, 0.0
        )
    }

    companion object {
        val DISPLAY_TRANSLATION = Vector3f(-0.85f, -0.2f, -1.0f)
        val DISPLAY_SCALE = Vector3f(1.7f, 1.7f, 1.7f)

        val DISPLAY_TRANSFORMATION = Transformation(DISPLAY_TRANSLATION, null, DISPLAY_SCALE, null)
    }
}