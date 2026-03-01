package dev.rolypolyvole.slimesmp.dragon.util

import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.equipment.trim.ArmorTrim
import net.minecraft.world.item.equipment.trim.TrimMaterials
import net.minecraft.world.item.equipment.trim.TrimPatterns
import net.minecraft.world.phys.Vec3
import java.util.*

object PhantomRiderSpawner {

    fun spawnArcher(level: ServerLevel, pos: Vec3) {
        val phantom = createPhantom(level, pos)
        val skeleton = createSkeleton(level, pos)

        // Power III bow
        val bow = ItemStack(Items.BOW)
        val enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        val power = enchantmentRegistry.getOrThrow(Enchantments.POWER)
        bow.enchant(power, 3)
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, bow)

        // Blindness tipped arrows in offhand
        val arrows = ItemStack(Items.TIPPED_ARROW, 64)
        arrows.set(DataComponents.POTION_CONTENTS, PotionContents(
            Optional.empty(), Optional.empty(),
            listOf(MobEffectInstance(MobEffects.BLINDNESS, 100, 0)),
            Optional.empty()
        ))
        skeleton.setItemSlot(EquipmentSlot.OFFHAND, arrows)

        equipArmor(level, skeleton)
        skeleton.customName = Component.literal("Phantom Archer").withColor(0xAA55FF)
        skeleton.isCustomNameVisible = true

        level.addFreshEntity(phantom)
        level.addFreshEntity(skeleton)
        skeleton.startRiding(phantom, true, true)
    }

    fun spawnHoplite(level: ServerLevel, pos: Vec3) {
        val phantom = createPhantom(level, pos)
        val skeleton = createSkeleton(level, pos)

        val trident = ItemStack(Items.NETHERITE_SPEAR)
        trident.set(DataComponents.CUSTOM_NAME, Component.literal("Phantom Spear").withColor(0x555555))
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, trident)
        skeleton.setItemSlot(EquipmentSlot.OFFHAND, ItemStack(Items.SHIELD))

        equipArmor(level, skeleton)
        skeleton.customName = Component.literal("Phantom Hoplite").withColor(0xFF5555)
        skeleton.isCustomNameVisible = true

        level.addFreshEntity(phantom)
        level.addFreshEntity(skeleton)
        skeleton.startRiding(phantom, true, true)
    }

    private fun createPhantom(level: ServerLevel, pos: Vec3): DragonPhantom {
        val phantom = DragonPhantom(level)
        phantom.snapTo(pos.x, pos.y + 2.0, pos.z, level.random.nextFloat() * 360f, 0f)
        phantom.phantomSize = 6
        phantom.customName = Component.literal("Void Phantom").withColor(0x555555)
        phantom.isCustomNameVisible = false
        return phantom
    }

    private fun createSkeleton(level: ServerLevel, pos: Vec3): DragonSkeleton {
        val skeleton = DragonSkeleton(level)
        skeleton.snapTo(pos.x, pos.y + 2.0, pos.z, level.random.nextFloat() * 360f, 0f)
        skeleton.getAttribute(Attributes.FOLLOW_RANGE)?.baseValue = 128.0

        // Prevent equipment drops
        for (slot in EquipmentSlot.entries) {
            skeleton.setDropChance(slot, 0.0f)
        }

        return skeleton
    }

    private fun equipArmor(level: ServerLevel, skeleton: DragonSkeleton) {
        val trimRegistry = level.registryAccess()
        val materialHolder = trimRegistry.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.AMETHYST)
        val patternHolder = trimRegistry.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.RIB)
        val trim = ArmorTrim(materialHolder, patternHolder)

        val enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        val projectileProt = enchantmentRegistry.getOrThrow(Enchantments.PROJECTILE_PROTECTION)

        val helmet = ItemStack(Items.NETHERITE_HELMET)
        helmet.enchant(projectileProt, 2)
        helmet.set(DataComponents.TRIM, trim)
        skeleton.setItemSlot(EquipmentSlot.HEAD, helmet)

        val chestplate = ItemStack(Items.NETHERITE_CHESTPLATE)
        chestplate.enchant(projectileProt, 2)
        chestplate.set(DataComponents.TRIM, trim)
        skeleton.setItemSlot(EquipmentSlot.CHEST, chestplate)
    }
}
