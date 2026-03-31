package dev.rolypolyvole.slimesmp.commands

import com.mojang.brigadier.context.CommandContext
import dev.rolypolyvole.slimesmp.SlimeSMPMod
import dev.rolypolyvole.slimesmp.SlimeSMPMod.Companion.mm
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.EntityType
import java.util.*

class FlexCommand {
    private val name = "flex"
    private val cooldowns = mutableMapOf<UUID, Long>()

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
        val now = System.currentTimeMillis()

        val lastUsed = cooldowns[player.uuid] ?: 0
        val remaining = 30000 - (now - lastUsed)

        if (remaining > 0) {
            val seconds = (remaining / 1000) + 1
            player.sendSystemMessage(mm("<red>You can flex again in ${seconds}s!</red>"))
            return 0
        }

        if (item.isEmpty) {
            player.sendSystemMessage(mm("<red>You're not holding anything!</red>"))
            return 0
        }

        cooldowns[player.uuid] = now

        val playerName = Component.literal(player.gameProfile.name)
            .withStyle(Style.EMPTY.withColor(0x55FF55).withHoverEvent(
                HoverEvent.ShowEntity(
                    HoverEvent.EntityTooltipInfo(EntityType.PLAYER, player.uuid, player.displayName)
                )
            )
        )

        val itemName = item.styledHoverName.copy().withStyle(
            Style.EMPTY.withHoverEvent(HoverEvent.ShowItem(item))
        )

        val count = if (item.count > 1) {
            Component.empty()
                .append(mm("<gray>${item.count}x</gray>"))
                .append(Component.literal(" "))
        } else {
            Component.empty()
        }

        val message = Component.empty()
            .append(playerName)
            .append(mm("<gold> is flexing their </gold>"))
            .append(count)
            .append(mm("<dark_gray><b>[</b></dark_gray>"))
            .append(itemName)
            .append(mm("<dark_gray><b>]</b></dark_gray>"))

        source.server.playerList.broadcastSystemMessage(message, false)

        val sound = Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1.0f, 1.0f)
        SlimeSMPMod.adventure().players().playSound(sound)

        return 1
    }
}
