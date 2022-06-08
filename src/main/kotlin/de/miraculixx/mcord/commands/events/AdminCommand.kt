package de.miraculixx.mcord.commands.events

import de.miraculixx.mcord.Main
import de.miraculixx.mcord.commands.SlashCommands
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class AdminCommand : SlashCommands {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        if (it.user.name != "Miraculixx") return
        runBlocking {
            launch {
                when (it.getOption("call")?.asString) {
                    "status" -> {
                        val status = it.getOption("status")!!.asBoolean
                        val guild = Main.INSTANCE.jda!!.getGuildById(908621996009619477)!!
                        val statsChannel = guild.getTextChannelById(975782593997963274)!!
                        val updater = statsChannel.getHistoryFromBeginning(5).complete()?.retrievedHistory?.firstOrNull() ?: statsChannel.sendMessage("loading").complete()

                        if (status) {
                            updater?.editMessage("**API Status**\n> :green_circle: - All Systems are online! Enjoy using MUtils")?.queue()
                        } else updater?.editMessage("**API Status**\n> :red_circle: - Some or all Systems are down or in maintenance! We try our bests to be online again soon!")?.queue()
                    }
                }
            }
        }
    }
}