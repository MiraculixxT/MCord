package de.miraculixx.mcord.modules.games.tictactoe

import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.utils.guildMiraculixx
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.UUID
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class TTTGame(private val member1: Member, private val member2: Member, private val uuid: UUID) {

    // Who is playing the next step
    // True - P1 (red) || False - P2 (green)
    private var whoPlays = Random.nextBoolean()
    private var winner: TTTField? = null
    private val message: Message
    private val threadMessage: Message
    private val thread: ThreadChannel
    private val fields = Array(3) { arrayOf(TTTField.EMPTY, TTTField.EMPTY, TTTField.EMPTY) }

    private fun calcButtons(): List<ActionRow> {
        val rows = ArrayList<ActionRow>()
        val blancEmote = Emoji.fromMarkdown("<:blanc:784059217890770964>")
        val xEmote = Emoji.fromMarkdown("<:xx:988156472020066324>")
        val oEmote = Emoji.fromMarkdown("<:oo:988156473274163200>")

        var rowI = 0
        fields.forEach { row ->
            var columnI = 0
            val list = ArrayList<Button>()
            row.forEach { field ->
                val button = when (field) {
                    TTTField.EMPTY -> if (winner == null) Button.secondary("GAME_TTT_P_${uuid}_$rowI-$columnI", blancEmote)
                    else Button.secondary("GAME_TTT_$rowI-$columnI", blancEmote).asDisabled()
                    TTTField.PLAYER_1 -> Button.danger("GAME_TTT_$rowI-$columnI", xEmote).asDisabled()
                    TTTField.PLAYER_2 -> Button.success("GAME_TTT_$rowI-$columnI", oEmote).asDisabled()
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
            .setDescription("<:xx:988156472020066324> - Spieler Rot ${member1.asMention}\n" +
                    "<:oo:988156473274163200> - Spieler Grün ${member2.asMention}")
        if (winner != null) {
            val message = when (winner!!) {
                TTTField.EMPTY -> "Unentschieden"
                TTTField.PLAYER_1 -> "${member1.asMention} hat gewonnen!"
                TTTField.PLAYER_2 -> "${member2.asMention} hat gewonnen!"
            }
            builder.addField("~~                                                                            ~~",
            "> \uD83C\uDFC1 $message", false)
                .setColor(0x2f3136)
        } else if (whoPlays) {
            builder.addField("~~                                                                            ~~",
            "> ${member1.asMention} ist am Zug", false)
                .setColor(0xff0000)
        } else {
            builder.addField("~~                                                                            ~~",
                "> ${member2.asMention} ist am Zug", false)
                .setColor(0x1fff00)
        }
        return builder.build()
    }

    private fun checkWin() {
        winner = getWinner() ?: return
        val replayButton = Button.primary("GAME_TTT_R_${member1.id}_${member2.id}", "Revanche").withEmoji(Emoji.fromUnicode("\uD83D\uDD01"))
        val msg = "~~========================~~\n\n" +
                "**\uD83C\uDFC1 || Das Spiel wurde beendet!**\n" +
                when (winner ?: return) {
            TTTField.EMPTY -> "Niemand hat gewonnen - Unentschieden"
            TTTField.PLAYER_1 -> "<:xx:988156472020066324> ${member1.asMention} hat gewonnen!"
            TTTField.PLAYER_2 -> "<:oo:988156473274163200> ${member2.asMention} hat gewonnen!"
        }
        thread.sendMessage(msg)
            .setEmbeds(EmbedBuilder().setDescription("Der Spiel-Bereich löscht sich in **30s**").build())
            .setActionRow(replayButton).queue()
        GameManager.tttGames.remove(uuid)
    }

    private fun getWinner(): TTTField? {
        //Check rows
        repeat(3) { row ->
            val s = fields[row][0]
            if (s != TTTField.EMPTY && s == fields[row][1] && s == fields[row][2])
                return s
        }

        //Check columns
        repeat(3) { col ->
            val s = fields[0][col]
            if (s != TTTField.EMPTY && s == fields[1][col] && s == fields[2][col])
                return s
        }

        //Check diagonals
        val s = fields[1][1] //middle piece
        if (s != TTTField.EMPTY) {
            if (s == fields[0][0] && s == fields[2][2])
                return s
            if (s == fields[2][0] && s == fields[0][2])
                return s
        }

        //Check if tie
        fields.forEach { row ->
            if (row.contains(TTTField.EMPTY))
                return null //No tie
        }
        //If code reaches here - TIE
        return TTTField.EMPTY
    }

    suspend fun interaction(buttonID: String, interactor: Member, event: ButtonInteractionEvent) = coroutineScope {
        val memberID = interactor.idLong
        if (memberID != member1.idLong && memberID != member2.idLong) {
            event.reply("```diff\n- Du bist kein Teil dieser Partie!\nStarte eine eigene über /tictactoe <user>```").setEphemeral(true).queue()
            return@coroutineScope
        }
        val splits = buttonID.split('-')
        val row = splits[0].toInt()
        val column = splits[1].toInt()
        if (memberID == member1.idLong) {
            if (whoPlays) {
                fields[row][column] = TTTField.PLAYER_1
                whoPlays = false
                thread.sendMessage("${member1.asMention} hat <:xx:988156472020066324> auf Feld **$buttonID** gesetzt.\n" +
                        "> ${member2.asMention} du bist am Zug!").queue()
            } else {
                event.reply("```diff\n- Du bist gerade nicht am Zug!```").setEphemeral(true).queue()
                return@coroutineScope
            }
        } else {
            if (!whoPlays) {
                fields[row][column] = TTTField.PLAYER_2
                thread.sendMessage("${member2.asMention} hat <:oo:988156473274163200> auf Feld **$buttonID** gesetzt.\n" +
                        "> ${member1.asMention} du bist am Zug!").queue()
                whoPlays = true
            } else {
                event.reply("```diff\n- Du bist gerade nicht am Zug!```").setEphemeral(true).queue()
                return@coroutineScope
            }
        }
        checkWin()
        val buttons = calcButtons()
        message.editMessageEmbeds(calcEmbed()).setActionRows(buttons).complete()
        threadMessage.editMessageComponents(buttons).complete()
        event.editMessage(event.message.contentRaw).queue()
        if (winner != null) launch {
            delay(30.seconds)
            thread.delete().queue()
        }
    }

    fun setWinner(win: TTTField) {
        winner = win
        message.editMessageEmbeds(calcEmbed()).setActionRows(calcButtons()).queue()
        thread.delete().queue()
    }

    init {
        //Game Start
        val channel = guildMiraculixx.getTextChannelById(GameManager.ticTacToeChannel)!!
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