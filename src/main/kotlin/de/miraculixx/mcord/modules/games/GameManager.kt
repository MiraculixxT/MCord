package de.miraculixx.mcord.modules.games

import de.miraculixx.mcord.modules.games.fourWins.FIARGame
import de.miraculixx.mcord.modules.games.tictactoe.TTTGame
import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.utils.Games
import de.miraculixx.mcord.modules.games.utils.SimpleGame
import de.miraculixx.mcord.utils.log
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.*

object GameManager {
    //Running Games
    val tttGames = HashMap<UUID, TTTGame>()
    val fiarGames = HashMap<UUID, FIARGame>()


    fun searchGame(hook: InteractionHook, member: Member, gameTag: String, gameName: String) {
        hook.editOriginal(
            "\uD83C\uDFAE **|| ${gameName.uppercase()}**\n" +
                    "${member.asMention} sucht nach einem Gegner zum $gameName spielen!\n" +
                    "> Bist du bereit für ein Spiel? Klicke auf `Accept`"
        ).setActionRow(
            Button.success("GAME_${gameTag}_ACCEPT_${member.id}", "Accept").withEmoji(Emoji.fromUnicode("✔️")),
            Button.danger("GAME_${gameTag}_CANCEL_${member.id}", "Cancel").withEmoji(Emoji.fromUnicode("✖️"))
        ).queue()
    }

    fun requestGame(hook: InteractionHook, member: Member, opponent: Member, gameTag: String, gameName: String) {
        hook.editOriginal(
            "\uD83C\uDFAE **|| ${gameName.uppercase()}**\n" +
                    "${opponent.asMention} - Du wurdest herausgefordert zu einem $gameName match von ${member.asMention}!\n" +
                    "> Bist du bereit für das Spiel? Klicke auf `Accept`"
        ).setActionRow(
            Button.success("GAME_${gameTag}_YES_${member.id}_${opponent.id}", "Accept").withEmoji(Emoji.fromUnicode("✔️")),
            Button.danger("GAME_${gameTag}_NO_${opponent.id}", "Deny").withEmoji(Emoji.fromUnicode("✖️"))
        ).queue()
    }

    fun newGame(game: Games, guild: Guild?, members: List<String>, channelID: Long) {
        when (game) {
            Games.TIC_TAC_TOE -> {
                val uuid = UUID.randomUUID()
                tttGames[uuid] = TTTGame(
                    guild?.getMemberById(members[0]) ?: return,
                    guild.getMemberById(members[1]) ?: return,
                    uuid,
                    channelID,
                    guild
                )
            }
            Games.IDLE -> return
            Games.FOUR_WINS -> {
                val uuid = UUID.randomUUID()
                fiarGames[uuid] = FIARGame(
                    guild?.getMemberById(members[0]) ?: return,
                    guild.getMemberById(members[1]) ?: return,
                    uuid,
                    guild,
                    channelID
                )
            }
        }
    }

    fun getGame(games: Games, uuid: UUID): SimpleGame? {
        return when (games) {
            Games.TIC_TAC_TOE -> tttGames[uuid]
            Games.FOUR_WINS -> fiarGames[uuid]
            Games.IDLE -> null
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
}