package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.Main
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu

class AdminCommand : SlashCommandEvent {
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
                            .addOption("Challenges", "vorschlag1", "Schlage Minecraft Challenges für MUtils vor", Emoji.fromEmote("mutils", 975780449903341579, false))
                            .addOption("Emotes (DC/Twitch)", "vorschlag2", "Schlage Emotes für Discord oder Twitch vor", Emoji.fromUnicode("\uD83D\uDD0D"))
                            .addOption("Discord Verbesserungen", "vorschlag3", "Schlage Verbesserungen für diesen Discord vor", Emoji.fromUnicode("⚒️"))
                        dd.maxValues = 1
                        dd.minValues = 0
                        dd.placeholder = "Sende einen neuen Vorschlag ein"
                        val embed = EmbedBuilder()
                            .setColor(0x1CE721)
                            .setTitle("\uD83D\uDCDD | Vorschläge Einsenden")
                            .setDescription("Sende Vorschläge für verschiedene Kategorisieren ein! Alle Vorschläge werden für alle Sichtbar in den jeweiligen Channel gepostet\n" +
                                    "\n⚠️ **Achtung**\n" +
                                    "- Prüfe bitte, dass keine Dopplungen entstehen\n" +
                                    "- Troll oder Missbrauch wird führt zu einer Sperre")
                        it.textChannel.sendMessageEmbeds(embed.build())
                            .setActionRow(dd.build()).queue()
                        it.reply("fertig").queue()
                    }
                }
            }
        }
    }
}