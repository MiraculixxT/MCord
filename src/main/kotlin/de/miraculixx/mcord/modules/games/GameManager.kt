package de.miraculixx.mcord.modules.games

import de.miraculixx.mcord.modules.games.chess.ChessGame
import de.miraculixx.mcord.modules.games.connectFour.C4Game
import de.miraculixx.mcord.modules.games.tictactoe.TTTGame
import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.utils.Game
import de.miraculixx.mcord.modules.games.utils.SimpleGame
import de.miraculixx.mcord.utils.Color
import de.miraculixx.mcord.utils.log
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.*
import kotlin.collections.HashMap

object GameManager {
    //Running Games
    // HashMap<GuildID, Map<GameType, HashMap<GameID, GameInstance>>>
    private val guilds = HashMap<Long, Map<Game, HashMap<UUID, SimpleGame>>>()

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

    fun newGame(game: Game, guild: Guild, members: List<String>, channelID: Long) {
        if (guilds[guild.idLong] == null)
            guilds[guild.idLong] = mapOf(Game.TIC_TAC_TOE to hashMapOf(), Game.FOUR_WINS to hashMapOf(), Game.CHESS to hashMapOf())
        val uuid = UUID.randomUUID()
        guilds[guild.idLong]!![game]!![uuid] = when (game) {
            Game.TIC_TAC_TOE -> TTTGame(
                guild.retrieveMemberById(members[0]).complete() ?: return,
                guild.retrieveMemberById(members[1]).complete() ?: return,
                uuid,
                channelID,
                guild
            )
            Game.FOUR_WINS -> C4Game(
                guild.retrieveMemberById(members[0]).complete() ?: return,
                guild.retrieveMemberById(members[1]).complete() ?: return,
                uuid,
                guild,
                channelID
            )
            Game.CHESS -> ChessGame(
                guild.retrieveMemberById(members[0]).complete() ?: return,
                guild.retrieveMemberById(members[1]).complete() ?: return,
                uuid,
                guild,
                channelID
            )
            else -> return
        }
    }

    fun getGame(guildID: Long, type: Game, uuid: UUID): SimpleGame? {
        return guilds[guildID]?.get(type)?.get(uuid)
    }

    fun removeGame(guildID: Long, type: Game, uuid: UUID): Boolean {
        return guilds[guildID]?.get(type)?.remove(uuid) != null
    }

    fun shutdown() {
        "---=---> GAME MANAGER <---=---".log(Color.YELLOW)
        guilds.forEach { (guild, data) ->
            data.forEach { (type, games) ->
                games.forEach { (uuid, instance) ->
                    instance.setWinner(FieldsTwoPlayer.EMPTY)
                    removeGame(guild, type, uuid)
                }
            }
            " - Guild $guild offline".log(Color.YELLOW)
        }
        "---=---=---=---=---=---=---=---".log(Color.YELLOW)
    }
}