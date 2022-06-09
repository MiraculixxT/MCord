package de.miraculixx.mcord.commands.events

import de.miraculixx.mcord.Main
import de.miraculixx.mcord.commands.SlashCommands
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu

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
                    "vorschlag" -> {
                        val dd = SelectMenu.create("vorschlag")
                            .addOption("Challenges", "vorschlag1", "Schlage Minecraft Challenges für Basti vor", Emoji.fromEmote("mutils", 975780449903341579, false))
                            .addOption("Seltene Sachen", "vorschlag2", "Schlage Seltene Dinge in Minecraft vor für Seltene Sachen suchen", Emoji.fromUnicode("\uD83D\uDD0D"))
                            .addOption("Discord Verbesserungen", "vorschlag3", "Schlage Verbesserungen für diesen Discord vor", Emoji.fromUnicode("⚒️"))
                        dd.maxValues = 1
                        dd.minValues = 0
                        dd.placeholder = "Sende einen neuen Vorschlag ein"
                        it.reply("Blablabla, hier irgendeine Nachricht hin, die den Usern erklärt was zu machen ist")
                            .addActionRow(dd.build()).queue()
                    }
                }
            }
        }
    }
}