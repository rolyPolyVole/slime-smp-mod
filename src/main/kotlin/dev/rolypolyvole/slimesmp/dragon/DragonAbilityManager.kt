package dev.rolypolyvole.slimesmp.dragon

import net.minecraft.world.entity.boss.enderdragon.EnderDragon

class DragonAbilityManager(private val dragon: EnderDragon) {
    private var ticks = 0

    fun tick () {
        this.ticks++


    }
}