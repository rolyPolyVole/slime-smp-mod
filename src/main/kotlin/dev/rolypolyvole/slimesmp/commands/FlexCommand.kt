package dev.rolypolyvole.slimesmp.commands

import com.mojang.brigadier.context.CommandContext
import dev.rolypolyvole.slimesmp.SlimeSMPMod.Companion.mm
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

class FlexCommand {
    private val name = "flex"

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
        val item = player.mainHandItem

        if (item.isEmpty) {
            player.sendSystemMessage(
                mm("<red>You're not holding anything!</red>")
            )

            return 0
        }

        val itemName = item.displayName.copy().withStyle(
            Style.EMPTY.withHoverEvent(HoverEvent.ShowItem(item))
        )

        val prefix = mm("<gray>${player.gameProfile.name} is holding </gray>")
        val suffix = mm("<gray> x${item.count}!</gray>")

        val message = net.minecraft.network.chat.Component.empty()
            .append(prefix)
            .append(itemName)
            .append(suffix)

        source.server.playerList.broadcastSystemMessage(message, false)

        return 1
    }
}
