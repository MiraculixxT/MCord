package de.miraculixx.mcord.modules.games

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.modules.games.fourWins.FIARGame
import de.miraculixx.mcord.modules.games.tictactoe.TTTGame
import de.miraculixx.mcord.utils.entities.ButtonEvent
import de.miraculixx.mcord.utils.entities.DropDownEvent
import de.miraculixx.mcord.utils.log
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import java.util.*
import kotlin.collections.HashMap

object GameManager : ButtonEvent, DropDownEvent {
    //Running Games
    val tttGames = HashMap<UUID, TTTGame>()
    val fiarGames = HashMap<UUID, FIARGame>()

    val ticTacToeChannel: Long
    val fourWinsChannel: Long

    //
    // Listener
    //
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val id = it.button.id?.removePrefix("GAME_") ?: return
        val member = it.member ?: return
        val guild = it.guild

        // <Game NAME>_<ACTION>_<Game ID>_<additions>
        // eg. TTT_PLAY_12345_2 (0,1,2,3)
        val options = id.split('_')
        when (options[0]) {
            "TTT" -> when (options[1]) {
                "P" -> tttGames[UUID.fromString(options[2])]?.interaction(options[3], member, it)
                "R" -> {
                    it.message.delete().queue()
                    newGame(Games.TIC_TAC_TOE, guild, listOf(options[2], options[3]))
                }
                "YES" -> {
                    if (options[3] != member.id)
                        it.reply("```diff\n- Du kannst diese Herausforderung nicht annehmen!```").setEphemeral(true).queue()
                    else {
                        it.message.delete().queue()
                        newGame(Games.TIC_TAC_TOE, guild, listOf(options[2], options[3]))
                    }
                }
                "NO" -> {
                    if (options[2] != member.id)
                        it.reply("```diff\n- Du kannst diese Herausforderung nicht ablehnen!```").setEphemeral(true).queue()
                    else it.editMessage(
                        "\uD83C\uDFAE || TIC TAC TOE\n" +
                                "❌ ${member.asMention} hat die Einladung abgelehnt!"
                    ).setActionRow(it.message.actionRows.first().buttons.map { it.asDisabled() }).queue()
                }
            }
            "4G" -> when (options[1]) {
                "YES" -> {
                    if (options[3] != member.id)
                        it.reply("```diff\n- Du kannst diese Herausforderung nicht annehmen!```").setEphemeral(true).queue()
                    else {
                        it.message.delete().queue()
                        newGame(Games.FOUR_WINS, guild, listOf(options[2], options[3]))
                    }
                }
                "NO" -> {
                    if (options[2] != member.id)
                        it.reply("```diff\n- Du kannst diese Herausforderung nicht ablehnen!```").setEphemeral(true).queue()
                    else it.editMessage(
                        "\uD83C\uDFAE || 4 GEWINNT\n" +
                                "❌ ${member.asMention} hat die Einladung abgelehnt!"
                    ).setActionRow(it.message.actionRows.first().buttons.map { it.asDisabled() }).queue()
                }
            }
        }
    }

    override suspend fun trigger(it: SelectMenuInteractionEvent) {
        val ddID = it.selectMenu.id?.removePrefix("GAME_")?.split('_') ?: return
        val selectID = it.selectedOptions.first().value.split('-')
        val member = it.member ?: return

        when (ddID[0]) {
            "4G" -> when (ddID[1]) {
                "P" -> fiarGames[UUID.fromString(ddID[2])]?.interaction(selectID[0][0], selectID[1][0], member, it)
            }
        }
    }

    private fun newGame(game: Games, guild: Guild?, members: List<String>) {
        when (game) {
            Games.TIC_TAC_TOE -> {
                val uuid = UUID.randomUUID()
                tttGames[uuid] = TTTGame(
                    guild?.getMemberById(members[0]) ?: return,
                    guild.getMemberById(members[1]) ?: return, uuid
                )
            }
            Games.IDLE -> return
            Games.FOUR_WINS -> {
                val uuid = UUID.randomUUID()
                fiarGames[uuid] = FIARGame(
                    guild?.getMemberById(members[0]) ?: return,
                    guild.getMemberById(members[1]) ?: return, uuid
                )
            }
        }
    }

    fun shutdown() {
        tttGames.forEach { (_, game) ->
            game.setWinner(FieldsTwoPlayer.EMPTY)
        }
        fiarGames.forEach { (_, game) ->
            game.setWinner(FieldsTwoPlayer.EMPTY)
        }
        "Game Manager offline".log()
    }

    init {
        val conf = ConfigManager.getConfig(Configs.GAME_SETTINGS)
        ticTacToeChannel = conf.getLong("TicTacToe-Channel")
        fourWinsChannel = conf.getLong("FourInARow-Channel")
    }
}