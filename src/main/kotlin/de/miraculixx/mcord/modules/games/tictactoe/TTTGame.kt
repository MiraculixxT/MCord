package de.miraculixx.mcord.modules.games.tictactoe

import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.utils.Game
import de.miraculixx.mcord.modules.games.utils.SimpleGame
import de.miraculixx.mcord.utils.api.SQL
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class TTTGame(
    private val member1: Member, private val member2: Member,
    private val uuid: UUID,
    channelID: Long,
    guild: Guild
) : SimpleGame {

    // Who is playing the next step
    // True - P1 (red) || False - P2 (green)
    private var guildID: Long
    private var whoPlays = Random.nextBoolean()
    private var winner: FieldsTwoPlayer? = null
    private val message: Message
    private val threadMessage: Message
    private val thread: ThreadChannel
    private val fields = Array(3) {
        (1..3).map { FieldsTwoPlayer.EMPTY }.toTypedArray()
    }

    private fun calcButtons(): List<ActionRow> {
        val rows = ArrayList<ActionRow>()
        val blancEmote = Emoji.fromFormatted("<:blanc:784059217890770964>")
        val xEmote = Emoji.fromFormatted("<:xx:988156472020066324>")
        val oEmote = Emoji.fromFormatted("<:oo:988156473274163200>")

        var rowI = 0
        fields.forEach { row ->
            var columnI = 0
            val list = ArrayList<Button>()
            row.forEach { field ->
                val button = when (field) {
                    FieldsTwoPlayer.EMPTY -> if (winner == null) Button.secondary("GAME_TTT_P_${uuid}_${rowI}_$columnI", blancEmote)
                    else Button.secondary("GAME_TTT_$rowI-$columnI", blancEmote).asDisabled()
                    FieldsTwoPlayer.PLAYER_1 -> Button.danger("GAME_TTT_${rowI}_$columnI", xEmote).asDisabled()
                    FieldsTwoPlayer.PLAYER_2 -> Button.success("GAME_TTT_${rowI}_$columnI", oEmote).asDisabled()
                }
                list.add(button)
                columnI++
            }
            rows.add(ActionRow.of(list))
            rowI++
        }
        return rows
    }

    private fun calcEmbed(): MessageEmbed {
        val builder = EmbedBuilder()
            .setTitle("<:gamespot:988131155159183420> || TIC TAC TOE")
            .setDescription(
                "<:xx:988156472020066324> - Spieler Rot ${member1.asMention}\n" +
                        "<:oo:988156473274163200> - Spieler Grün ${member2.asMention}"
            )
        if (winner != null) {
            val message = when (winner!!) {
                FieldsTwoPlayer.EMPTY -> "Unentschieden"
                FieldsTwoPlayer.PLAYER_1 -> "${member1.asMention} hat gewonnen!"
                FieldsTwoPlayer.PLAYER_2 -> "${member2.asMention} hat gewonnen!"
            }
            builder.addField(
                "~~<                                                                            >~~",
                "> \uD83C\uDFC1 $message", false
            )
                .setColor(0x2f3136)
        } else if (whoPlays) {
            builder.addField(
                "~~<                                                                            >~~",
                "> ${member1.asMention} ist am Zug", false
            )
                .setColor(0xff0000)
        } else {
            builder.addField(
                "~~<                                                                            >~~",
                "> ${member2.asMention} ist am Zug", false
            )
                .setColor(0x1fff00)
        }
        return builder.build()
    }

    private suspend fun checkWin() {
        winner = getWinner() ?: return
        val replayButton = Button.primary("GAME_TTT_R_${member1.id}_${member2.id}", "Revanche").withEmoji(Emoji.fromUnicode("\uD83D\uDD01"))
        val msg = "~~========================~~\n\n" +
                "**\uD83C\uDFC1 || Das Spiel wurde beendet!**\n" +
                when (winner ?: return) {
                    FieldsTwoPlayer.EMPTY -> "Niemand hat gewonnen - Unentschieden"
                    FieldsTwoPlayer.PLAYER_1 -> "<:xx:988156472020066324> ${member1.asMention} hat gewonnen!"
                    FieldsTwoPlayer.PLAYER_2 -> "<:oo:988156473274163200> ${member2.asMention} hat gewonnen!"
                }
        thread.sendMessage(msg)
            .setEmbeds(EmbedBuilder().setDescription("Der Spiel-Bereich löscht sich in **30s**").build())
            .setActionRow(replayButton).queue()
        GameManager.removeGame(guildID, Game.TIC_TAC_TOE, uuid)

        //val ex = if (bot == null) "" else "_Bot"
        if (winner == FieldsTwoPlayer.PLAYER_1)
            SQL.addWin(member1.idLong, guildID, "TTT")
        else if (winner == FieldsTwoPlayer.PLAYER_2)
            SQL.addWin(member2.idLong, guildID, "TTT")
    }

    private fun getWinner(): FieldsTwoPlayer? {
        //Check rows
        repeat(3) { row ->
            val s = fields[row][0]
            if (s != FieldsTwoPlayer.EMPTY && s == fields[row][1] && s == fields[row][2])
                return s
        }

        //Check columns
        repeat(3) { col ->
            val s = fields[0][col]
            if (s != FieldsTwoPlayer.EMPTY && s == fields[1][col] && s == fields[2][col])
                return s
        }

        //Check diagonals
        val s = fields[1][1] //middle piece
        if (s != FieldsTwoPlayer.EMPTY) {
            if (s == fields[0][0] && s == fields[2][2])
                return s
            if (s == fields[2][0] && s == fields[0][2])
                return s
        }

        //Check if tie
        fields.forEach { row ->
            if (row.contains(FieldsTwoPlayer.EMPTY))
                return null //No tie
        }
        //If code reaches here - TIE
        return FieldsTwoPlayer.EMPTY
    }

    override suspend fun interact(options: List<String>, interactor: Member, event: GenericComponentInteractionCreateEvent) {
        val memberID = interactor.idLong
        if (memberID != member1.idLong && memberID != member2.idLong) {
            event.reply("```diff\n- Du bist kein Teil dieser Partie!\nStarte eine eigene über /tictactoe <user>```").setEphemeral(true).queue()
            return
        }
        val row = options[0].toInt()
        val column = options[1].toInt()
        if (memberID == member1.idLong) {
            if (whoPlays) {
                fields[row][column] = FieldsTwoPlayer.PLAYER_1
                whoPlays = false
                thread.sendMessage(
                    "${member1.asMention} hat <:xx:988156472020066324> auf Feld **$options** gesetzt.\n" +
                            "> ${member2.asMention} du bist am Zug!"
                ).queue()
            } else {
                event.reply("```diff\n- Du bist gerade nicht am Zug!```").setEphemeral(true).queue()
                return
            }
        } else {
            if (!whoPlays) {
                fields[row][column] = FieldsTwoPlayer.PLAYER_2
                thread.sendMessage(
                    "${member2.asMention} hat <:oo:988156473274163200> auf Feld **$options** gesetzt.\n" +
                            "> ${member1.asMention} du bist am Zug!"
                ).queue()
                whoPlays = true
            } else {
                event.reply("```diff\n- Du bist gerade nicht am Zug!```").setEphemeral(true).queue()
                return
            }
        }
        checkWin()
        val buttons = calcButtons()
        message.editMessageEmbeds(calcEmbed()).setActionRows(buttons).complete()
        threadMessage.editMessageComponents(buttons).complete()
        event.editMessage(event.message.contentRaw).queue()
        if (winner != null) {
            delay(30.seconds)
            thread.delete().queue()
        }
    }

    override fun setWinner(win: FieldsTwoPlayer) {
        winner = win
        message.editMessageEmbeds(calcEmbed()).setActionRows(calcButtons()).queue()
        thread.delete().queue()
    }

    init {
        //Game Start
        guildID = guild.idLong

        val channel = guild.getTextChannelById(channelID)!!
        message = channel.sendMessageEmbeds(calcEmbed())
            .setActionRows(calcButtons()).complete()
        thread = message.createThreadChannel("TTT - ${member1.user.name} vs ${member2.user.name}").complete()
        threadMessage = thread.sendMessage(" \u1CBC ").setActionRows(calcButtons()).complete()
        thread.addThreadMember(member1).complete()
        thread.addThreadMember(member2).complete()
        val mention = if (whoPlays) member1.asMention else member2.asMention
        thread.sendMessage("$mention du bist am Zug!").queue()
    }
}