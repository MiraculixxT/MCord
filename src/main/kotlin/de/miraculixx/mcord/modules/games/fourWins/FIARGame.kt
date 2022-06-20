@file:Suppress("JoinDeclarationAndAssignment")

package de.miraculixx.mcord.modules.games.fourWins

import de.miraculixx.mcord.modules.games.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.guildMiraculixx
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.ThreadChannel
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

// FIAR -> Four in a Row
class FIARGame(private val member1: Member, private val member2: Member, private val uuid: UUID) {

    private val member1Emote: String
    private val member2Emote: String

    // Who is playing the next step
    // True - P1 (red) || False - P2 (green)
    private var whoPlays = Random.nextBoolean()
    private var winner: FieldsTwoPlayer? = null
    private val message: Message
    private val threadMessage: Message
    private val thread: ThreadChannel
    private val fields = Array(5) {
        (1..7).map { FieldsTwoPlayer.EMPTY }.toTypedArray()
    }

    private fun calcEmbed(): MessageEmbed {
        val builder = EmbedBuilder()
            .setTitle("<:gamespot:988131155159183420> || 4 GEWINNT")
            .setDescription(
                "$member1Emote - Spieler 1 ${member1.asMention}\n" +
                        "$member2Emote - Spieler 2 ${member2.asMention}"
            )
        val mention = if (whoPlays) member1.asMention else member2.asMention
        builder.addField(
            "~~<                                                                            >~~",
            "> $mention ist am Zug", false
        )

        //Game field
        val stringBuilder = StringBuilder()
        var rowI = 1
        fields.forEach { row ->
            stringBuilder.append("\n> `$rowI` ")
            row.forEach { stringBuilder.append(fieldToEmote(it)) }
            rowI++
        }
        stringBuilder.append("\n> ` ` `A\u1CBC\u1CBCB\u1CBC\u1CBCC\u1CBC\u1CBCD\u1CBC\u1CBCE\u1CBC\u1CBCF\u1CBC\u1CBCG`")
        builder.addField(
            "~~<                                                                            >~~",
            stringBuilder.toString(), false
        )
        builder.setColor(0xb8800b)
        return builder.build()
    }

    private fun calcSelector(): SelectMenu {
        val dd = SelectMenu.create("GAME_4G_P_$uuid")
        dd.maxValues = 1
        dd.minValues = 1
        dd.placeholder = "Wähle einen Slot aus"

        var columnI = 1
        val columns = (0..6).map { i -> (0..4).map { j -> fields[j][i] } }
        columns.forEach { column ->
            val playableSlot = column.lastIndexOf(FieldsTwoPlayer.EMPTY)
            if (playableSlot != -1) {
                val slot = numberToChar(columnI)
                dd.addOption("Spalte $slot - Reihe ${playableSlot.plus(1)} ($slot${playableSlot.plus(1)})",
                    "${columnI.minus(1)}-$playableSlot")
            }
            columnI++
        }
        return dd.build()
    }

    private fun numberToChar(i: Int): Char {
        return when (i) {
            1 -> 'A'
            2 -> 'B'
            3 -> 'C'
            4 -> 'D'
            5 -> 'E'
            6 -> 'F'
            7 -> 'G'
            else -> 'Z'
        }
    }

    private fun fieldToEmote(field: FieldsTwoPlayer): String {
        return when (field) {
            FieldsTwoPlayer.EMPTY -> "⚪"
            FieldsTwoPlayer.PLAYER_1 -> member1Emote
            FieldsTwoPlayer.PLAYER_2 -> member2Emote
        }
    }

    private fun checkWinner(player: FieldsTwoPlayer): Boolean {
        val high = fields.size
        val width = fields[0].size

        // Algorithm joinkt from https://stackoverflow.com/questions/32770321/connect-4-check-for-a-win-algorithm
        // horizontal Check
        for (j in 0 until high - 3) {
            for (i in 0 until width) {
                if (fields[i][j] == player && fields[i][j+1] == player && fields[i][j+2] == player && fields[i][j+3] == player) {
                    return true
                }
            }
        }
        // vertical Check
        for (i in 0 until width - 3) {
            for (j in 0 until high) {
                if (fields[i][j] == player && fields[i+1][j] == player && fields[i+2][j] == player && fields[i+3][j] == player)
                    return true
            }
        }
        // ascending Diagonal Check
        for (i in 3 until width) {
            for (j in 0 until high - 3) {
                if (fields[i][j] == player && fields[i-1][j+1] == player && fields[i-2][j+2] == player && fields[i-3][j+3] == player)
                    return true
            }
        }
        // descending Diagonal Check
        for (i in 3 until width) {
            for (j in 3 until high) {
                if (fields[i][j] == player && fields[i-1][j-1] == player && fields[i-2][j-2] == player && fields[i-3][j-3] == player)
                    return true
            }
        }
        return false
    }

    suspend fun interaction(column: Char, row: Char, interactor: Member, event: SelectMenuInteractionEvent) = coroutineScope {
        val memberID = interactor.idLong
        if (memberID != member1.idLong && memberID != member2.idLong) {
            event.reply("```diff\n- Du bist kein Teil dieser Partie!\nStarte eine eigene über /vier-gewinnt <user>```").setEphemeral(true).queue()
            return@coroutineScope
        }
        if (whoPlays && memberID != member1.idLong || !whoPlays && memberID != member2.idLong) {
            event.reply("```diff\n- Du bist gerade nicht am Zug!```").setEphemeral(true).queue()
            return@coroutineScope
        }
        val who = if (whoPlays) FieldsTwoPlayer.PLAYER_1 else FieldsTwoPlayer.PLAYER_2
        val emote = if (whoPlays) member1Emote else member2Emote
        val opponent = if (whoPlays) member2 else member1
        fields[row.digitToInt()][column.digitToInt()] = who
        thread.sendMessage("${interactor.asMention} hat $emote auf Feld **$column$row** gesetzt.\n" +
                "> ${opponent.asMention} du bist am Zug!").queue()
        whoPlays = !whoPlays

        if (checkWinner(who)) {

        }
        val selector = calcSelector()
        message.editMessageEmbeds(calcEmbed()).setActionRow(selector).complete()
        threadMessage.editMessageComponents(ActionRow.of(selector)).complete()
        event.editMessage(event.message.contentRaw).queue()
        if (winner != null) launch {
            delay(30.seconds)
            thread.delete().queue()
        }
    }


    init {
        //Get Emotes
        member1Emote = SQL.getUserEmote(member1.idLong)?.emote ?: ":yellow_circle:"
        val emote = SQL.getUserEmote(member2.idLong)
        member2Emote = if (emote?.emote == member1Emote) emote.emote2 else emote?.emote ?: ":red_circle:"

        //Game Start
        val channel = guildMiraculixx.getTextChannelById(GameManager.fourWinsChannel)!!
        val selector = calcSelector()
        message = channel.sendMessageEmbeds(calcEmbed())
            .setActionRow(selector).complete()
        thread = message.createThreadChannel("4G - ${member1.user.name} vs ${member2.user.name}").complete()
        threadMessage = thread.sendMessage(" \u1CBC ").setActionRow(selector).complete()
        thread.addThreadMember(member1).complete()
        thread.addThreadMember(member2).complete()
        val mention = if (whoPlays) member1.asMention else member2.asMention
        thread.sendMessage("$mention du bist am Zug!").queue()
    }
}