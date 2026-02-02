package dev.rolypolyvole.slimesmp

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.core.registries.Registries
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.monster.skeleton.Skeleton
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments

class SlimeSMPMod : DedicatedServerModInitializer {

    override fun onInitializeServer() {
        println("Slime SMP Mod initialized!")

        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            if (entity is Skeleton) {
                if (Math.random() < 0.02) {
                    val sword = ItemStack(Items.IRON_SWORD).apply {
                        val registry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        val sharpnessHolder = registry.getOrThrow(Enchantments.SHARPNESS)

                        enchant(sharpnessHolder, 1)
                    }

                    val helmet = ItemStack(Items.IRON_HELMET)

                    entity.setItemInHand(InteractionHand.MAIN_HAND, sword)
                    entity.setItemSlot(EquipmentSlot.HEAD, helmet)
                }
            }
        }
    }
}
