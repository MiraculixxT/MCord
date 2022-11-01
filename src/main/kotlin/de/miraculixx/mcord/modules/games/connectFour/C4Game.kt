@file:Suppress("JoinDeclarationAndAssignment")

package de.miraculixx.mcord.modules.games.connectFour

import de.miraculixx.mcord.config.msg
import de.miraculixx.mcord.config.msgDiff
import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.GoalManager
import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.utils.SimpleGame
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.log
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

// FIAR -> Four in a Row
class C4Game(
    private val member1: Member,
    private val member2: Member,
    private val uuid: UUID,
    guild: Guild,
    channelID: Long,
    botLevel: Int
) : SimpleGame {

    private lateinit var member1Emote: String
    private lateinit var member2Emote: String
    private val guildID: Long

    // Who is playing the next step
    // True - P1 (red) || False - P2 (green)
    private var bot: C4Bot? = null
    private var whoPlays = Random.nextBoolean()
    private var winner: FieldsTwoPlayer? = null
    private lateinit var message: Message
    private lateinit var threadMessage: Message
    private lateinit var thread: ThreadChannel
    private val fields = Array(6) {
        (1..7).map { FieldsTwoPlayer.EMPTY }.toTypedArray()
    }

    private suspend fun calcEmbed(): MessageEmbed {
        return Embed {
            title = "<:gamespot:988131155159183420> || CONNECT 4"
            description = "$member1Emote - Player 1 ${member1.asMention}\n" +
                    "$member2Emote - Player 2 " +
                    if (bot != null)
                        "`Bot Level ${bot!!.level}`"
                    else member2.asMention
            if (winner == null) {
                val mention = if (whoPlays) member1.asMention else member2.asMention
                field(
                    "~~<                                                                            >~~",
                    "> $mention ${msg("onMove", guildID)}", false
                )
                color = 0xb8800b
            } else {
                val msg = when (winner!!) {
                    FieldsTwoPlayer.EMPTY -> msg("draw", guildID)
                    FieldsTwoPlayer.PLAYER_1 -> "${member1.asMention} ${msg("win", guildID)}"
                    FieldsTwoPlayer.PLAYER_2 -> "${member2.asMention} ${msg("win", guildID)}"
                }
                field(
                    "~~<                                                                            >~~",
                    "> \uD83C\uDFC1 $msg", false
                )
                color = 0x2f3136
            }

            //Game field
            val stringBuilder = StringBuilder()
            var rowI = 1
            fields.forEach { row ->
                stringBuilder.append("\n> **║** ")
                row.forEach { stringBuilder.append(fieldToEmote(it)) }
                stringBuilder.append(" **║**")
                rowI++
            }
            stringBuilder.append("\n> **║** <:11:989885132418711564><:22:989886289803374714><:33:989886474679881749><:44:989886595970777148><:55:989886723792203828><:66:989886928197402635><:77:989887121449975818> **║**")
            field(
                "~~<                                                                            >~~",
                stringBuilder.toString(), false
            )
        }
    }

    private fun calcButtons(): List<ActionRow> {
        val buttons = ArrayList<Button>()

        return if (winner == null) {
            var columnI = 1
            val columns = (0..6).map { i -> (0..5).map { j -> fields[j][i] } }
            columns.forEach { column ->
                val playableSlot = column.lastIndexOf(FieldsTwoPlayer.EMPTY)
                if (playableSlot != -1) {
                    buttons.add(Button.success("GAME_4G_P_${uuid}_${columnI.minus(1)}_$playableSlot", "$columnI"))
                } else buttons.add(Button.danger("Baum$columnI", "$columnI").asDisabled())
                columnI++
            }
            val blanc = Emoji.fromFormatted("<:blanc:784059217890770964>")
            val row1 = ActionRow.of(buttons[1], buttons[2], buttons[3], buttons[4], buttons[5])
            val row2 = ActionRow.of(
                buttons[0], Button.secondary("BLANC1", blanc).asDisabled(),
                Button.secondary("BLANC2", blanc).asDisabled(), Button.secondary("BLANC3", blanc).asDisabled(), buttons[6]
            )
            listOf(row1, row2)
        } else listOf()
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
        val board = (0..6).map { i -> (0..5).map { j -> fields[j][i] } }

        // Algorithm joinkt from https://stackoverflow.com/questions/32770321/connect-4-check-for-a-win-algorithm
        // horizontal Check
        for (j in 0 until high - 3) {
            for (i in 0 until width) {
                if (board[i][j] == player && board[i][j + 1] == player && board[i][j + 2] == player && board[i][j + 3] == player) {
                    return true
                }
            }
        }
        // vertical Check
        for (i in 0 until width - 3) {
            for (j in 0 until high) {
                if (board[i][j] == player && board[i + 1][j] == player && board[i + 2][j] == player && board[i + 3][j] == player)
                    return true
            }
        }
        // ascending Diagonal Check
        for (i in 3 until width) {
            for (j in 0 until high - 3) {
                if (board[i][j] == player && board[i - 1][j + 1] == player && board[i - 2][j + 2] == player && board[i - 3][j + 3] == player)
                    return true
            }
        }
        // descending Diagonal Check
        for (i in 3 until width) {
            for (j in 3 until high) {
                if (board[i][j] == player && board[i - 1][j - 1] == player && board[i - 2][j - 2] == player && board[i - 3][j - 3] == player)
                    return true
            }
        }
        return false
    }

    override suspend fun interact(options: List<String>, interactor: Member, event: GenericComponentInteractionCreateEvent?) {
        val column = options[0][0]
        val row = options[1][0]
        val memberID = interactor.idLong
        if (memberID != member1.idLong && memberID != member2.idLong) {
            event?.reply(msgDiff(msg("notPartOfMatch", guildID).replace("%GAME%", "connect-4")))?.setEphemeral(true)?.queue()
            return
        }
        if ((whoPlays && memberID != member1.idLong) || (!whoPlays && memberID != member2.idLong)) {
            event?.reply(msgDiff(msg("notYourMove", guildID)))?.setEphemeral(true)?.queue()
            return
        }
        event?.editMessage(message.contentRaw + " ")?.complete()
        sendUpdate(row.digitToInt(), column.digitToInt(), interactor)
    }

    override suspend fun setWinner(win: FieldsTwoPlayer) {
        winner = win
        message.editMessageEmbeds(calcEmbed()).setComponents(calcButtons()).queue()
        thread.delete().queue()
    }

    private suspend fun botMove() {
        val nextColumn = bot?.getNextMove(fields) ?: return
        val columns = (0..6).map { i -> (0..5).map { j -> fields[j][i] } }
        val column = columns[nextColumn]

        val row = column.lastIndexOf(FieldsTwoPlayer.EMPTY)
        fields[row][nextColumn] = FieldsTwoPlayer.PLAYER_2
        sendUpdate(row, nextColumn, member2)
    }

    private suspend fun sendUpdate(row: Int, column: Int, interactor: Member) {
        val who = if (whoPlays) FieldsTwoPlayer.PLAYER_1 else FieldsTwoPlayer.PLAYER_2
        val emote = if (whoPlays) member1Emote else member2Emote
        val opponent = if (whoPlays) member2 else member1
        fields[row][column] = who
        thread.sendMessage(
            "$emote -> ${msg("column", guildID)} **${column.plus(1)}**\n" +
                    "> ${opponent.asMention} ${msg("onMove", guildID)}"
        ).queue()
        whoPlays = !whoPlays

        var full = true
        fields.forEach { r ->
            if (r.contains(FieldsTwoPlayer.EMPTY)) {
                full = false
                return@forEach
            }
        }
        if (checkWinner(who) || full) {
            winner = if (!full) who else FieldsTwoPlayer.EMPTY
            val replayButton = Button.primary("GAME_4G_R_${member1.id}_${member2.id}", "Replay").withEmoji(Emoji.fromUnicode("\uD83D\uDD01"))
            val msg = "~~========================~~\n\n" +
                    "**\uD83C\uDFC1 || ${msg("gameEnd", guildID)}**\n" +
                    when (winner ?: FieldsTwoPlayer.EMPTY) {
                        FieldsTwoPlayer.EMPTY -> {
                            GoalManager.registerDraw(Game.CONNECT_4, member1.idLong, guildID)
                            GoalManager.registerDraw(Game.CONNECT_4, member2.idLong, guildID)
                            msg("draw", guildID)
                        }

                        FieldsTwoPlayer.PLAYER_1 -> {
                            GoalManager.registerWin(Game.CONNECT_4, bot != null, member1.idLong, guildID)
                            "$member1Emote ${member1.asMention} ${msg("win", guildID)}"
                        }

                        FieldsTwoPlayer.PLAYER_2 -> {
                            GoalManager.registerWin(Game.CONNECT_4, bot != null, member2.idLong, guildID)
                            "$member2Emote ${member2.asMention} ${msg("win", guildID)}"
                        }
                    }
            if (bot != null)
                thread.sendMessage(msg)
                    .setEmbeds(EmbedBuilder().setDescription(msg("selfDelete", guildID)).build()).queue()
            else thread.sendMessage(msg)
                .setEmbeds(EmbedBuilder().setDescription(msg("selfDelete", guildID)).build())
                .setActionRow(replayButton).queue()
            GameManager.removeGame(guildID, Game.CONNECT_4, uuid)
        }
        val selector = calcButtons()
        message.editMessageEmbeds(calcEmbed()).setComponents(selector).complete()
        threadMessage.editMessageComponents(selector).complete()

        if (winner != null) {
            delay(30.seconds)
            thread.delete().queue()
        } else if (bot != null && !whoPlays) {
            delay(1.seconds)
            botMove()
        }
    }


    init {
        if (member2.user.isBot) {
            "GAME > Start Bot Game".log()
            bot = C4Bot(botLevel)
        }
        guildID = guild.idLong

        CoroutineScope(Dispatchers.Default).launch {
            //Get Emotes
            member1Emote = SQL.getUser(member1.idLong, guildID, emotes = true).emotes!!.c4
            val emote = SQL.getUser(member2.idLong, guildID, emotes = true).emotes!!
            member2Emote = if (bot != null) "\uD83E\uDD16" else
                if (emote.c4 == member1Emote) emote.c42 else emote.c4

            //Game Start
            val channel = guild.getTextChannelById(channelID)!!
            val selector = calcButtons()
            message = channel.sendMessageEmbeds(calcEmbed())
                .setComponents(selector).complete()
            thread = message.createThreadChannel("4G - ${member1.nickname ?: member1.user.name} vs ${member2.nickname ?: member2.user.name}").complete()
            threadMessage = thread.sendMessage(" \u1CBC ").setComponents(selector).complete()
            thread.addThreadMember(member1).complete()
            thread.addThreadMember(member2).complete()
            val mention = if (whoPlays) member1 else member2
            thread.sendMessage("${mention.asMention} ${msg("onMove", guildID)}").queue()

            if (bot != null && mention.id == member2.id)
                CoroutineScope(Dispatchers.Default).launch {
                    botMove()
                }
        }
    }
}