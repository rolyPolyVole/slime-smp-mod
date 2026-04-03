package dev.rolypolyvole.slimesmp

import dev.rolypolyvole.slimesmp.commands.FlexCommand
import dev.rolypolyvole.slimesmp.commands.LavaRisingCommand
import dev.rolypolyvole.slimesmp.events.RuinedPortalLootEvent
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.chat.Component


class SlimeSMPMod : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        println("Slime SMP Mod initialized!")

        ServerLifecycleEvents.SERVER_STARTING.register { adventure = MinecraftServerAudiences.of(it) }
        ServerLifecycleEvents.SERVER_STOPPED.register { adventure = null }

        RuinedPortalLootEvent().register()

        LavaRisingCommand().register()
        FlexCommand().register()
    }

    companion object {
        const val MOD_ID = "slime-smp-mod"

        var adventure: MinecraftServerAudiences? = null
            private set

        fun adventure(): MinecraftServerAudiences =
            adventure ?: throw IllegalStateException("Tried to access Adventure without a running server!")

        fun mm(miniMessage: String): Component =
            adventure().asNative(MiniMessage.miniMessage().deserialize(miniMessage))
    }
}
