package dev.rolypolyvole.slimesmp.commands

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ClientboundTransferPacket

class LavaRisingCommand {
    private val name = "lavarising"

    private val host = "192.168.1.50"
    private val port = 25565

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            if (!environment.includeDedicated) return@register

            val command = Commands
                .literal(name)
                .executes(this::onExecution)

            dispatcher.register(command)
        }
    }

    private fun onExecution(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val player = source.playerOrException

        if (source.textName.startsWith(".")) {
            source.sendFailure(Component.literal("Lava Rising does not support Bedrock yet.").withColor(0xFF5555))
            return 0
        }

        source.sendSuccess({ Component.literal("Sending you to Lava Rising!").withColor(0x55FF55) }, false)

        player.connection.send(
            ClientboundTransferPacket(host, port)
        )

        return 1
    }
}