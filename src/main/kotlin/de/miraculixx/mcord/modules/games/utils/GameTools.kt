package de.miraculixx.mcord.modules.games.utils

import de.miraculixx.mcord.modules.games.GameManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import java.util.*

class GameTools(private val gameTag: String, private val gameName: String) {
    fun command(event: SlashCommandInteractionEvent) {
        val member = event.member ?: return
        val opponent = event.getOption("user")?.asMember
        val hook = event.hook
        if (opponent == null) {
            event.deferReply().queue()
            GameManager.searchGame(hook, member, gameTag, gameName)
            return
        }

        if (opponent.id == member.id) {
            event.reply("```diff\n- Du kannst dich nicht selbst herausfordern! Gebe keinen Nutzer an, um gegen den Bot zu spielen```")
                .setEphemeral(true).queue()
            return
        }
        event.deferReply().queue()
        GameManager.requestGame(hook, member, opponent, gameTag, gameName)
    }

    suspend fun buttons(it: ButtonInteractionEvent, game: Games) {
        val id = it.button.id?.removePrefix("GAME_${gameTag}_") ?: return
        val member = it.member ?: return
        val guild = it.guild
        val options = id.split('_')

        // GAME_TTT_ (first snippet)
        // P_<DATA> (options)
        when (options[1]) {
            "P" -> GameManager.getGame(game, UUID.fromString(options[1]))
                    ?.interact(options.subList(1, options.size), member, it)
            "R" -> {
                it.message.delete().queue()
                GameManager.newGame(game, guild, listOf(options[1], options[2]), it.channel.idLong)
            }
            "YES" -> {
                if (options[2] != member.id)
                    it.reply("```diff\n- Du kannst diese Herausforderung nicht annehmen!```").setEphemeral(true).queue()
                else {
                    it.message.delete().queue()
                    GameManager.newGame(game, guild, listOf(options[1], options[2]), it.channel.idLong)
                }
            }
            "NO" -> {
                if (options[1] != member.id)
                    it.reply("```diff\n- Du kannst diese Herausforderung nicht ablehnen!```").setEphemeral(true).queue()
                else it.editMessage(
                    "\uD83C\uDFAE || ${gameName.uppercase()}\n" +
                            "❌ ${member.asMention} hat die Einladung abgelehnt!"
                ).setActionRow(it.message.actionRows.first().buttons.map { it.asDisabled() }).queue()
            }
            "ACCEPT" -> if (options[1] == member.id)
                it.reply("```diff\n- Du kannst dich nicht mit dir selbst spielen!```").setEphemeral(true).queue()
            else {
                it.message.delete().queue()
                it.reply("```diff\n+ Spiel wird gestartet...```").setEphemeral(true).queue()
                GameManager.newGame(game, guild, listOf(options[1], options[2]), it.channel.idLong)
            }
            "CANCEL" -> if (options[1] != member.id)
                it.reply("```diff\n- Du kannst diese Spielersuche nicht beenden!```").setEphemeral(true).queue()
            else {
                it.message.delete().queue()
                it.reply("```diff\n+ Deine Spielersuche wurde beendet!```").setEphemeral(true).queue()
            }
        }
    }
}