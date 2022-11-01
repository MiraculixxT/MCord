package de.miraculixx.mcord.modules.games.tictactoe

import de.miraculixx.mcord.config.msg
import de.miraculixx.mcord.config.msgDiff
import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.GoalManager
import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.utils.SimpleGame
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.log
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.messages.Embed
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

class TTTGame(
    private val member1: Member, private val member2: Member,
    private val uuid: UUID,
    channelID: Long,
    guild: Guild,
    botLevel: Int
) : SimpleGame {

    // Who is playing the next step
    // True - P1 (red) || False - P2 (green)
    private val bot: TTTBot?
    private var guildID: Long
    private var whoPlays = Random.nextBoolean()
    private var winner: FieldsTwoPlayer? = null
    private lateinit var message: Message
    private lateinit var threadMessage: Message
    private lateinit var thread: ThreadChannel
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

    private suspend fun calcEmbed(): MessageEmbed {
        return Embed {
            title = "<:gamespot:988131155159183420> || TIC TAC TOE"
            description = "<:xx:988156472020066324> - Player Red ${member1.asMention}\n" +
                    "<:oo:988156473274163200> - Player Green " +
                    if (bot != null)
                        "`Bot Level ${bot.level}`"
                    else member2.asMention
            if (winner != null) {
                val message = when (winner!!) {
                    FieldsTwoPlayer.EMPTY -> msg("draw", guildID)
                    FieldsTwoPlayer.PLAYER_1 -> "${member1.asMention} ${msg("win", guildID)}"
                    FieldsTwoPlayer.PLAYER_2 -> "${member2.asMention} ${msg("win", guildID)}"
                }
                field {
                    name = "~~<                                                                            >~~"
                    value = "> \uD83C\uDFC1 $message"
                    inline = false
                }
                color = 0x2f3136
            } else if (whoPlays) {
                field {
                    name = "~~<                                                                            >~~"
                    value = "> ${member1.asMention} ${msg("onMove", guildID)}"
                    inline = false
                }
                color = 0xff0000
            } else {
                field {
                    name = "~~<                                                                            >~~"
                    value = "> ${member2.asMention} ${msg("onMove", guildID)}"
                    inline = false
                }
                color = 0x1fff00
            }
        }
    }

    private suspend fun checkWin() {
        winner = getWinner() ?: return
        val replayButton = Button.primary("GAME_TTT_R_${member1.id}_${member2.id}", "Replay").withEmoji(Emoji.fromUnicode("\uD83D\uDD01"))
        val msg = "~~========================~~\n\n" +
                "**\uD83C\uDFC1 || ${msg("gameEnd", guildID)}**\n" +
                when (winner ?: return) {
                    FieldsTwoPlayer.EMPTY -> {
                        GoalManager.registerDraw(Game.TIC_TAC_TOE, member1.idLong, guildID)
                        GoalManager.registerDraw(Game.TIC_TAC_TOE, member2.idLong, guildID)
                        msg("draw", guildID)
                    }
                    FieldsTwoPlayer.PLAYER_1 -> {
                        GoalManager.registerWin(Game.TIC_TAC_TOE, bot != null, member1.idLong, guildID)
                        "<:xx:988156472020066324> ${member1.asMention} ${msg("win", guildID)}"
                    }
                    FieldsTwoPlayer.PLAYER_2 -> {
                        GoalManager.registerWin(Game.TIC_TAC_TOE, bot != null, member2.idLong, guildID)
                        "<:oo:988156473274163200> ${member2.asMention} ${msg("win", guildID)}!"
                    }
                }
        if (bot != null) thread.sendMessage(msg)
            .setEmbeds(EmbedBuilder().setDescription(msg("selfDelete", guildID)).build()).queue()
        else thread.sendMessage(msg)
            .setEmbeds(EmbedBuilder().setDescription(msg("selfDelete", guildID)).build())
            .setActionRow(replayButton).queue()

        GameManager.removeGame(guildID, Game.TIC_TAC_TOE, uuid)
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

    override suspend fun interact(options: List<String>, interactor: Member, event: GenericComponentInteractionCreateEvent?) {
        val memberID = interactor.idLong
        if (memberID != member1.idLong && memberID != member2.idLong) {
            event?.reply(msgDiff(msg("notPartOfMatch", guildID).replace("%GAME%", "tictactoe")))?.setEphemeral(true)?.queue()
            return
        }
        val row = options[0].toInt()
        val column = options[1].toInt()
        if (memberID == member1.idLong) {
            if (whoPlays) {
                fields[row][column] = FieldsTwoPlayer.PLAYER_1
                whoPlays = false
                thread.sendMessage(
                    "<:xx:988156472020066324> -> **${row+1}${column+1}**\n" +
                            "> ${member2.asMention} ${msg("onMove", guildID)}"
                ).queue()
            } else {
                event?.reply(msgDiff(msg("notYourMove", guildID)))?.setEphemeral(true)?.queue()
                return
            }
        } else {
            if (!whoPlays) {
                fields[row][column] = FieldsTwoPlayer.PLAYER_2
                thread.sendMessage(
                    "<:oo:988156473274163200> -> **${row+1}${column+1}**\n" +
                            "> ${member1.asMention} ${msg("onMove", guildID)}"
                ).queue()
                whoPlays = true
            } else {
                event?.reply(msgDiff(msg("notYourMove", guildID)))?.setEphemeral(true)?.queue()
                return
            }
        }
        checkWin()
        val buttons = calcButtons()
        message.editMessageEmbeds(calcEmbed()).setComponents(buttons).complete()
        threadMessage.editMessageComponents(buttons).complete()
        event?.editMessage(event.message.contentRaw)?.queue()
        if (winner != null) {
            delay(30.seconds)
            thread.delete().queue()
        } else if (bot != null && !whoPlays) botMove()
    }

    private suspend fun botMove() {
        delay(1.seconds)
        val pos = bot?.getMove(fields)!!
        fields[pos.first][pos.second] = FieldsTwoPlayer.PLAYER_2
        interact(listOf(pos.first.toString(), pos.second.toString()), member2, null)
    }

    override suspend fun setWinner(win: FieldsTwoPlayer) {
        winner = win
        message.editMessageEmbeds(calcEmbed()).setComponents(calcButtons()).queue()
        thread.delete().queue()
    }

    init {
        bot = if (member2.user.isBot) {
            "GAME > Start TTT Bot Game".log()
            TTTBot(botLevel, FieldsTwoPlayer.PLAYER_2)
        } else null
        guildID = guild.idLong

        getDefaultScope().launch {
            val channel = guild.getTextChannelById(channelID)!!
            message = channel.sendMessageEmbeds(calcEmbed())
                .setComponents(calcButtons()).complete()
            thread = message.createThreadChannel("TTT - ${member1.user.name} vs ${member2.user.name}").complete()
            threadMessage = thread.sendMessage(" \u1CBC ").setComponents(calcButtons()).complete()
            thread.addThreadMember(member1).complete()
            thread.addThreadMember(member2).complete()
            val mention = if (whoPlays) member1.asMention else member2.asMention
            thread.sendMessage("$mention ${msg("onMove", guildID)}").queue()
            if (bot != null && !whoPlays)
                botMove()
        }
    }
}