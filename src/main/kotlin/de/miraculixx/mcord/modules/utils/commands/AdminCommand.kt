package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.Main
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
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
                            .setDescription(
                                "Sende Vorschläge für verschiedene Kategorisieren ein! Alle Vorschläge werden für alle Sichtbar in den jeweiligen Channel gepostet\n" +
                                        "\n⚠️ **Achtung**\n" +
                                        "- Prüfe bitte, dass keine Dopplungen entstehen\n" +
                                        "- Troll oder Missbrauch wird führt zu einer Sperre"
                            )
                        it.textChannel.sendMessageEmbeds(embed.build())
                            .setActionRow(dd.build()).queue()
                        it.reply("fertig").queue()
                    }
                    "idle-game-info" -> {
                        val dd = SelectMenu.create("GIdle_Info")
                            .addOption("Game Info", "gameinfo", "Was ist Idle Builder und was kann ich hier machen?", Emoji.fromUnicode("\uD83C\uDF34"))
                            .addOption("Upgrades", "upgrades", "Was sind Upgrades und wie funktionieren diese?", Emoji.fromUnicode("\uD83D\uDD3A"))
                            .addOption("Buildings", "buildings", "Was sind Buildings und wie funktionieren diese?", Emoji.fromUnicode("\uD83C\uDFD7️"))
                            .addOption("Prestige", "prestige", "Was sind Prestige Vorgänge und was bewirken diese?", Emoji.fromUnicode("\uD83D\uDC8E"))
                        dd.maxValues = 1
                        dd.minValues = 1
                        dd.placeholder = "Informationen und Hilfe zu Idle Builder"
                        val embed = EmbedBuilder()
                            .setColor(0xEEA82D)
                            .setTitle("\uD83C\uDF34 | Idle Builder Informationen")
                            .setDescription("Idle Builder ist ein Idle Game, welches auf Discord getestet wird um irgendwann mal als vollwertiges Spiel veröffentlicht zu werden")
                        it.textChannel.sendMessageEmbeds(embed.build())
                            .setActionRow(dd.build()).queue()
                        it.reply("fertig").queue()
                    }
                    "idle-game-upgrades" -> {
                        val upgradeMessage = it.textChannel.sendMessageEmbeds(
                            EmbedBuilder()
                                .setColor(0xEEA82D)
                                .setTitle("\uD83D\uDD3A | Upgrades")
                                .setDescription("Hier stehen Informationen zu Upgrades")
                                .build()
                        ).complete()

                        val buildingMessage = it.textChannel.sendMessageEmbeds(
                            EmbedBuilder()
                                .setColor(0xEEA82D)
                                .setTitle("\uD83C\uDFD7 | Buildings")
                                .setDescription("Hier stehen Informationen zu Buildings")
                                .build()
                        ).complete()

                        val threadUpgrades = upgradeMessage.createThreadChannel("\uD83D\uDD3A | Upgrades").complete()
                        val threadBuildings = buildingMessage.createThreadChannel("\uD83C\uDFD7️ | Buildings").complete()

                        val buttonUpgrades = threadUpgrades.sendMessageEmbeds(
                            EmbedBuilder().setDescription("Baum").build()
                        ).setActionRow(
                            Button.primary("GIdle_LoadUpgrades", "Lade deine Upgrades").withEmoji(Emoji.fromUnicode("\uD83C\uDFF7️"))
                        ).complete()
                        val buttonBuildings = threadBuildings.sendMessage(" ").setEmbeds(
                            EmbedBuilder().setDescription("Baum").build()
                        ).setActionRow(
                            Button.primary("GIdle_LoadBuildings", "Lade deine Buildings").withEmoji(Emoji.fromUnicode("\uD83C\uDFF7️"))
                        ).complete()

                        buttonBuildings.suppressEmbeds(true).queue()
                        buttonUpgrades.suppressEmbeds(true).queue()
                    }
                }
            }
        }
    }
}