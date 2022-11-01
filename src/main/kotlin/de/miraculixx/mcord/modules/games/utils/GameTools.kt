package de.miraculixx.mcord.modules.games.utils

import de.miraculixx.mcord.config.msg
import de.miraculixx.mcord.config.msgDiff
import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.GoalManager
import de.miraculixx.mcord.modules.games.utils.enums.Game
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import java.util.*

class GameTools(private val gameTag: String, private val gameName: String, private val game: Game) {
    suspend fun command(it: SlashCommandInteractionEvent) {
        val subcommand = it.subcommandName ?: return
        val member = it.member ?: return
        val hook = it.hook
        val discordID = it.guild?.idLong ?: return

        when (subcommand) {
            "user" -> {
                val opponent = it.getOption("request")?.asMember
                if (opponent != null) {
                    if (opponent.id == member.id)
                        it.reply(msgDiff(msgDiff(msg("commandSamePlayer", discordID)))).setEphemeral(true).queue()
                    else if (opponent.user.isBot) {
                        it.reply(msgDiff(msg("commandNotHuman", discordID))).setEphemeral(true).queue()
                    } else {
                        it.deferReply().queue()
                        GameManager.requestGame(hook, member, opponent, gameTag, gameName)
                    }
                } else {
                    it.deferReply().queue()
                    GameManager.searchGame(hook, member, gameTag, gameName)
                }
            }
            "bot" -> {
                val option = it.getOption("difficulty")!!.asString
                val level = when (option) {
                    "Hard" -> 3
                    "Medium" -> 2
                    else -> 1
                }
                it.reply(msg("commandStartBotGame", discordID).replace("%DIFF%", option)).setEphemeral(true).queue()
                GameManager.newGame(game, it.guild ?: return, listOf(member.id, it.jda.selfUser.id), it.channel.idLong, level)
            }
        }
    }

    suspend fun buttons(it: ButtonInteractionEvent) {
        val id = it.button.id?.removePrefix("GAME_${gameTag}_") ?: return
        val member = it.member ?: return
        val guild = it.guild ?: return
        val options = id.split('_')
        val guildID = guild.idLong

        // GAME_TTT_ (first snippet)
        // P_<DATA> (options)
        when (options[0]) {
            "P" -> GameManager.getGame(guild.idLong, game, UUID.fromString(options[1]))
                    ?.interact(options.subList(2, options.size), member, it)
            "R" -> {
                GoalManager.registerNewGame(game, true, member.idLong, guildID)
                it.message.delete().queue()
                GameManager.newGame(game, guild, listOf(options[1], options[2]), (it.channel as ThreadChannel).parentMessageChannel.idLong)
            }
            "YES" -> {
                if (options[2] != member.id)
                    it.reply(msgDiff(msg("commandCannotAccept", guildID))).setEphemeral(true).queue()
                else {
                    it.message.delete().queue()
                    GameManager.newGame(game, guild, listOf(options[1], options[2]), it.channel.idLong)
                }
            }
            "NO" -> {
                if (options[1] != member.id)
                    it.reply(msgDiff(msg("commandCannotDeny", guildID))).setEphemeral(true).queue()
                else it.editMessage(
                    "\uD83C\uDFAE || ${gameName.uppercase()}\n" +
                            "❌ ${member.asMention} ${msg("commandDeclineRequest", guildID)}"
                ).setActionRow(it.message.actionRows.first().buttons.map { it.asDisabled() }).queue()
            }
            "ACCEPT" -> if (options[1] == member.id)
                it.reply(msgDiff(msg("commandSelfPlay", guildID))).setEphemeral(true).queue()
            else {
                it.message.delete().queue()
                it.reply(msg("commandStartGame", guildID)).setEphemeral(true).queue()
                GameManager.newGame(game, guild, listOf(options[1], member.id), it.channel.idLong)
            }
            "CANCEL" -> if (options[1] != member.id)
                it.reply(msgDiff(msg("commandCannotDeny", guildID))).setEphemeral(true).queue()
            else {
                it.message.delete().queue()
                it.reply(msg("commandQueueLeave", guildID)).setEphemeral(true).queue()
            }
        }
    }
}