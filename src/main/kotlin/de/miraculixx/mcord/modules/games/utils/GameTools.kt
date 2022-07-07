package de.miraculixx.mcord.modules.games.utils

import de.miraculixx.mcord.modules.games.GameManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import java.util.*

class GameTools(private val gameTag: String, private val gameName: String, private val game: Games) {
    fun command(it: SlashCommandInteractionEvent) {
        val subcommand = it.subcommandName ?: return
        val member = it.member ?: return
        val hook = it.hook

        when (subcommand) {
            "user" -> {
                val opponent = it.getOption("request")?.asMember
                if (opponent != null) {
                    if (opponent.id == member.id)
                        it.reply("```diff\n- Du kannst nicht gegen dich selbst spielen```").setEphemeral(true).queue()
                    else {
                        it.deferReply().queue()
                        GameManager.requestGame(hook, member, opponent, gameTag, gameName)
                    }
                } else {
                    it.deferReply().queue()
                    GameManager.searchGame(hook, member, gameTag, gameName)
                }
            }
            "bot" -> {
                it.reply("```diff\n+ Neues Bot Game wird gestartet!\n+ Difficulty: Hard```").setEphemeral(true).queue()
                GameManager.newGame(game, it.guild, listOf(member.id, it.jda.selfUser.id), it.channel.idLong)
            }
        }
    }

    suspend fun buttons(it: ButtonInteractionEvent) {
        val id = it.button.id?.removePrefix("GAME_${gameTag}_") ?: return
        val member = it.member ?: return
        val guild = it.guild
        val options = id.split('_')

        // GAME_TTT_ (first snippet)
        // P_<DATA> (options)
        when (options[0]) {
            "P" -> GameManager.getGame(game, UUID.fromString(options[1]))
                    ?.interact(options.subList(2, options.size), member, it)
            "R" -> {
                it.message.delete().queue()
                GameManager.newGame(game, guild, listOf(options[1], options[2]), it.threadChannel.parentMessageChannel.idLong)
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
                GameManager.newGame(game, guild, listOf(options[1], member.id), it.channel.idLong)
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