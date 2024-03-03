package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.utils.entities.ButtonEvent
import de.miraculixx.mcord.utils.entities.DropDownEvent
import de.miraculixx.mcord.utils.entities.ModalEvent
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import de.miraculixx.mcord.utils.toError
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class AdminCommand : SlashCommandEvent, ModalEvent, ButtonEvent, DropDownEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        runBlocking {
            launch {
                when (it.getOption("call")?.asString) {
                    "status" -> {
                        val status = it.getOption("status")!!.asBoolean
                        val guild = it.jda.getGuildById(908621996009619477)!!
                        val statsChannel = guild.getTextChannelById(975782593997963274)!!
                        val updater = statsChannel.getHistoryFromBeginning(5).complete()?.retrievedHistory?.firstOrNull() ?: statsChannel.sendMessage("loading").complete()

                        if (status) {
                            updater?.editMessage("**API Status**\n> :green_circle: - All Systems are online! Enjoy using MUtils")?.queue()
                        } else updater?.editMessage("**API Status**\n> :red_circle: - Some or all Systems are down or in maintenance! We try our bests to be online again soon!")?.queue()
                    }
                    "vorschlag" -> {
                        val dd = StringSelectMenu.create("vorschlag")
                            .addOption("Challenges", "vorschlag1", "Schlage Minecraft Challenges für MUtils vor", Emoji.fromCustom("mutils", 975780449903341579, false))
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
                        it.messageChannel.sendMessageEmbeds(embed.build())
                            .setActionRow(dd.build()).queue()
                        it.reply("fertig").queue()
                    }
                    "idle-game" -> {
                        //Game Info
                        val dd = StringSelectMenu.create("GIdle_Info")
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
                        it.messageChannel.sendMessageEmbeds(embed.build())
                            .setActionRow(dd.build()).queue()

                        //Upgrades & Buildings
                        val upgradeMessage = it.messageChannel.sendMessageEmbeds(
                            EmbedBuilder()
                                .setColor(0xEEA82D)
                                .setTitle("\uD83D\uDD3A | Upgrades")
                                .setDescription("Hier stehen Informationen zu Upgrades")
                                .build()
                        ).complete()

                        val buildingMessage = it.messageChannel.sendMessageEmbeds(
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
                    "delete-threads" -> {
                        val threads = it.channel.asThreadContainer().threadChannels
                        it.reply("Deleting ${threads.size} Threads").setEphemeral(true).queue()
                        threads.forEach {
                            it.delete().queue()
                        }
                    }
                    "mcreate-panel" -> {
                        val channel = it.messageChannel
                        channel.sendMessageEmbeds(Embed {
                            title = "\uD83C\uDFF7️  **|| ROLE MANAGER**"
                            description = "Mange your Self-Roles to optimize your server experience!\n\n" +
                                    "**═══> Notifications & Appearance**\n" +
                                    "> - Decide about updates & event notifications (pings) \n" +
                                    "> - Customize the server appearance -> Channel visibility \n" +
                                    "> - Click on one button below to change your setup"
                            color = 0xD5B716
                        }).addComponents(ActionRow.of(
                            button("NOTIFY:NOTIFY", "Change Notifications", Emoji.fromFormatted("\uD83D\uDD14"), ButtonStyle.PRIMARY),
                            button("NOTIFY:APPEARANCE", "Change Appearance", Emoji.fromFormatted("⚙️"), ButtonStyle.PRIMARY)
                        )).queue()
                    }
                    "mcreate-rules" -> {
                        val channel = it.messageChannel
                        val status = it.getOption("status")?.asBoolean ?: return@launch
                        channel.send("**Server Rules**\n" +
                                "```fix\n" +
                                "- Have patience when asking for help.\n" +
                                "- Be respectful to everyone.\n" +
                                "- Be mindful of swearing, only use it moderately.\n" +
                                "- Follow staff directions, they have the final say.\n" +
                                "- Stick to the Channel Language. E. g. no english in german Support Channel\n" +
                                "- Respect the Discord wide Rules https://discordapp.com/guidelines```\n" +
                                "\n" +
                                "**Strict NoGo's**\n" +
                                "```diff\n" +
                                "- Not Safe For Work [NSFW-Content] is strict forbidden.\n" +
                                "- Trading or sharing premium content.\n" +
                                "- Sharing scam links or phishing links.\n" +
                                "- Any kind of discrimination is prohibited.```\n" +
                                "```diff\n" +
                                "But now...\n" +
                                "+ Click on \"Accept Rules\" to join this Discord Server!```", components = listOf(ActionRow.of(
                                    button("RULES:ACCEPT", "Accept Rules", Emoji.fromFormatted("<:protected:908823765830500446>"), style = ButtonStyle.SUCCESS, status)
                                ))).queue()
                    }
                    "add-all-role" -> {
                        val server = it.guild ?: return@launch
                        val role = it.getOption("role")?.asRole ?: return@launch
                        server.loadMembers().onSuccess { list ->
                            it.deferReply(true).queue()
                            list.forEach { member ->
                                server.addRoleToMember(member, role).queue()
                            }
                            it.hook.editOriginal("Adding roles to ${list.size} Members... This take some time").queue()
                        }.onError {  _ ->
                            it.reply_("Something went wrong".toError(), ephemeral = true).queue()
                        }
                    }

                    "test1" -> {
                        println("1")
                        try {
                            it.reply_(embeds = listOf(Embed { description = "Lustige Daten und so" }), components = listOf(
                                ActionRow.of(button("ban", "Ban den Weg", style = ButtonStyle.DANGER), button("delete", "Nur löschen"), button("fake", "Abuse"))
                            )).queue()
                        } catch (e: Exception) { println(e.message) }

                        println("2")
                    }

                    "test2" -> {
                        try {
                            it.reply_(embeds = listOf(Embed { description = "Lustige Daten und so" }), components = listOf(
                                ActionRow.of(StringSelectMenu("test2", "Wähle eine Aktion", ) {
                                    addOption("Ban den Weg", "ban", "Starke Regelverletzung")
                                    addOption("Nur löschen", "bb", "Entferne nur das Bild")
                                    addOption("Abuse", "bbb", "Dieses Bild ist legitim")
                              })
                            )).queue()
                        } catch (e: Exception) { println(e.message) }
                    }
                }
            }
        }
    }


    override suspend fun trigger(it: StringSelectInteractionEvent) {
        it.replyModal("test2", "Grund der Bestrafung") {
            paragraph("tt", "Warum soll das passieren?",true, placeholder = "Das Bild ist...")
            paragraph("ttt", "Nachricht an den Nutzer", false, placeholder = "Soll eine Nachricht gesendet werden?")
        }.queue()
    }

    override suspend fun trigger(it: ModalInteractionEvent) {
        val id = it.modalId
        when (id) {
            "test2" -> it.reply_("Danke für die Meldung!\nGrund: ``${it.getValue("tt")?.asString}``").queue()
        }
    }
}