package dev.rolypolyvole.slimesmp

import dev.rolypolyvole.slimesmp.commands.GearUpCommand
import dev.rolypolyvole.slimesmp.commands.LavaRisingCommand
import dev.rolypolyvole.slimesmp.worldgen.CustomEndSpikes
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.minecraft.world.level.Level


class SlimeSMPMod : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        println("Slime SMP Mod initialized!")

        LavaRisingCommand().register()
        GearUpCommand().register()

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { _, _, destination ->
            if (destination.dimension() == Level.END) {
                CustomEndSpikes.regenerateSpikes(destination)
            }
        }
    }

    companion object {
        const val MOD_ID = "slime-smp-mod"
    }
}
