package dev.rolypolyvole.slimesmp

import dev.rolypolyvole.slimesmp.commands.GearUpCommand
import dev.rolypolyvole.slimesmp.commands.LavaRisingCommand
import net.fabricmc.api.DedicatedServerModInitializer


class SlimeSMPMod : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        println("Slime SMP Mod initialized!")

        LavaRisingCommand().register()
        GearUpCommand().register()
    }

    companion object {
        const val MOD_ID = "slime-smp-mod"
    }
}
