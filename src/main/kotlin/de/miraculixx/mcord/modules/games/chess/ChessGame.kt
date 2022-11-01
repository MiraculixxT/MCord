package de.miraculixx.mcord.modules.games.chess

import de.miraculixx.mcord.config.msg
import de.miraculixx.mcord.config.msgDiff
import de.miraculixx.mcord.modules.games.GoalManager
import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.utils.SimpleGame
import de.miraculixx.mcord.modules.games.utils.enums.Game
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.*
import kotlin.random.Random

class ChessGame(
    member1: Member, member2: Member, private val uuid: UUID, guild: Guild, channelID: Long
) : SimpleGame {

    private val player1 = if (Random.nextBoolean()) member1 else member2
    private val player2 = if (member1 == player1) member2 else member1
    private val guildID: Long

    // Who is playing the next step
    // True - P1 (white) || False - P2 (black)
    private var whoPlays = true
    private var winner: FieldsTwoPlayer? = null
    private lateinit var message: Message
    private lateinit var threadMessage: Message
    private lateinit var thread: ThreadChannel
    private val board = arrayOf( // row - column
        arrayOf(
            FieldsChess.ROOK_BLACK,
            FieldsChess.KNIGHT_BLACK,
            FieldsChess.BISHOP_BLACK,
            FieldsChess.QUEEN_BLACK,
            FieldsChess.KING_BLACK,
            FieldsChess.BISHOP_BLACK,
            FieldsChess.KNIGHT_BLACK,
            FieldsChess.ROOK_BLACK
        ),
        (1..8).map { FieldsChess.PAWN_BLACK }.toTypedArray(),
        (1..8).map { FieldsChess.EMPTY }.toTypedArray(),
        (1..8).map { FieldsChess.EMPTY }.toTypedArray(),
        (1..8).map { FieldsChess.EMPTY }.toTypedArray(),
        (1..8).map { FieldsChess.EMPTY }.toTypedArray(),
        (1..8).map { FieldsChess.PAWN_WHITE }.toTypedArray(),
        arrayOf(
            FieldsChess.ROOK_WHITE,
            FieldsChess.KNIGHT_WHITE,
            FieldsChess.BISHOP_WHITE,
            FieldsChess.QUEEN_WHITE,
            FieldsChess.KING_WHITE,
            FieldsChess.BISHOP_WHITE,
            FieldsChess.KNIGHT_WHITE,
            FieldsChess.ROOK_WHITE
        )
    )

    private val columnChars = listOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H')

    private fun calcButtons(): List<ActionRow> {
        val id = if (whoPlays) player1.id else player2.id
        return listOf(
            ActionRow.of(
                Button.primary("GAME_CHESS_SEL_${uuid}_$id", "Figur Verschieben").withEmoji(Emoji.fromUnicode("♟️"))
            )
        )
    }

    private suspend fun calcEmbed(): List<MessageEmbed> {
        val builder = EmbedBuilder().setTitle("<:gamespot:988131155159183420> || CHESS").setDescription(
            "⬜ - Player 1 ${player1.asMention}\n" + "<:black:990247806436540428> - Player 2 ${player2.asMention}"
        )
        val field = EmbedBuilder()
            .setTitle("~~<                                                                  >~~")
        if (winner == null) {
            val mention = if (whoPlays) player1.asMention else player2.asMention
            builder.addField(
                "~~<                                                                            >~~",
                "> $mention ${msg("onMove", guildID)}", false
            ).setColor(0xb8800b)
            field.setColor(0xb8800b)
        } else {
            val msg = when (winner!!) {
                FieldsTwoPlayer.EMPTY -> msg("draw", guildID)
                FieldsTwoPlayer.PLAYER_1 -> "${player1.asMention} ${msg("win", guildID)}"
                FieldsTwoPlayer.PLAYER_2 -> "${player2.asMention} ${msg("win", guildID)}"
            }
            builder.addField(
                "~~<                                                                            >~~", "> \uD83C\uDFC1 $msg", false
            ).setColor(0x2f3136)
            field.setColor(0x2f3136)
        }

        //Game Filed
        val numberEmotes = listOf(
            "<:88:989887358533009428>",
            "<:77:989887121449975818>",
            "<:66:989886928197402635>",
            "<:55:989886723792203828>",
            "<:44:989886595970777148>",
            "<:33:989886474679881749>",
            "<:22:989886289803374714>",
            "<:11:989885132418711564>"
        )
        val desc = buildString {
            repeat(8) { row ->
                append("> ${numberEmotes[row]} ")
                repeat(8) { column ->
                    val f = board[row][column]
                    append(
                        if (((row + column) % 2) == 0) f.light else f.dark
                    )
                }
                appendLine()
            }
            append("<:blanc:784059217890770964><:blanc:784059217890770964><:AA:989888088429961276><:BB:989888374024327218><:CC:989888535286931476><:DD:989888642552061952><:EE:989888852380500029><:FF:989889099232071800><:GG:989889581329571890><:HH:989890374455676958>")
        }
        field.setDescription(desc)

        return listOf(builder.build(), field.build())
    }

    /**
     * @param position Pair(row, column)
     */
    suspend fun interactTo(position: Pair<Int, Char>, hook: InteractionHook) {
        val newPos = 7 - position.first to columnChars.lastIndexOf(position.second)
        val field = board[newPos.first][newPos.second]
        println("$newPos - ${field.name}")
        if (field == FieldsChess.EMPTY || field.white != whoPlays) {
            val color = if (whoPlays) msg("chessWhite", guildID) else msg("chessBlack", guildID)
            hook.editOriginal(msgDiff(msg("chessEmptyField", guildID).replace("%COLOR%", color))).queue()
            return
        }

        val list = when (field) {
            FieldsChess.PAWN_WHITE, FieldsChess.PAWN_BLACK -> ChessMoveLogic.movePawn(whoPlays, newPos, board, false)
            FieldsChess.KNIGHT_WHITE, FieldsChess.KNIGHT_BLACK -> ChessMoveLogic.moveKnight(whoPlays, newPos, board)
            FieldsChess.BISHOP_WHITE, FieldsChess.BISHOP_BLACK -> ChessMoveLogic.moveBishop(whoPlays, newPos, board)
            FieldsChess.ROOK_WHITE, FieldsChess.ROOK_BLACK -> ChessMoveLogic.moveRook(whoPlays, newPos, board)
            FieldsChess.QUEEN_WHITE, FieldsChess.QUEEN_BLACK -> ChessMoveLogic.moveQueen(whoPlays, newPos, board)
            FieldsChess.KING_WHITE, FieldsChess.KING_BLACK -> ChessMoveLogic.moveKing(whoPlays, newPos, board, true)
            else -> return
        }
        val msg = msg("chessChooseField", guildID)
        if (list.size > 5) {
            //Dropdown
            val dd = StringSelectMenu("GAME_CHESS_P_${uuid}") {
                placeholder = msg("chessMoves", guildID)
                maxValues = 1
                minValues = 1
                list.forEach { d ->
                    addOption("Field ${columnChars[d.second]}${(7 - d.first) + 1}", "${newPos.first}_${newPos.second}_${d.first}_${d.second}")
                }
            }
            hook.editOriginal(msg).setActionRow(dd).queue()
        } else if (list.isNotEmpty()) {
            val buttons = buildList {
                list.forEach { d ->
                    add(Button.secondary("GAME_CHESS_P_${uuid}_${newPos.first}_${newPos.second}_${d.first}_${d.second}", "${columnChars[d.second]}${(7 - d.first) + 1}"))
                }
            }
            hook.editOriginal(msg).setActionRow(buttons).queue()
        } else hook.editOriginal(msgDiff(msg("chessNoMoves", guildID))).queue()
    }

    override suspend fun interact(options: List<String>, interactor: Member, event: GenericComponentInteractionCreateEvent?) {
        val who = if (whoPlays) player1 else player2
        //Check if correct person is playing
        if (who.id != player1.id && who.id != player2.id) {
            event?.reply(msgDiff(msg("notPartOfMatch", guildID).replace("%GAME%", "tictactoe")))?.setEphemeral(true)?.queue()
            return
        }
        if (who.id != interactor.id) {
            event?.reply(msgDiff(msg("notYourMove", guildID)))?.setEphemeral(true)?.queue()
            return
        }

        //Calculate Move
        val from = options[0].toInt() to options[1].toInt()
        val to = options[2].toInt() to options[3].toInt()
        val prevFigure = board[to.first][to.second]
        val figure = board[from.first][from.second]

        //Check if Move is valid
        //(The Player isn't allowed to get him self into check/danger)
        val newBoard = calcMove(from, to, false)
        val isValid = ChessMoveLogic.checkMate(whoPlays, newBoard)
        if (isValid.first || isValid.second) {
            event?.reply(msgDiff(msg("chessInvalidMove", guildID)))?.setEphemeral(true)?.queue()
            return
        }

        //Apply move to board
        calcMove(from, to, true)

        //Announce Move
        whoPlays = !whoPlays
        val newField = "${columnChars[to.second]}${7 - to.first + 1}"
        val oldField = "${columnChars[from.second]}${7 - from.first + 1}"
        val addon = if (prevFigure == FieldsChess.EMPTY) "" else " (takes -> ${prevFigure.light})"
        val log = "${figure.light} $oldField -> $newField $addon"
        thread.sendMessage("${who.asMention} | $log").queue()
        event?.editMessage(log)?.setActionRow()?.queue()

        //Check if something win related happened
        checkWin()

        //Switch to next Player
        if (winner != null) {
            threadMessage.delete().queue()
            message.editMessageEmbeds(calcEmbed()).setActionRow().queue()
        } else {
            val buttons = calcButtons()
            message.editMessageEmbeds(calcEmbed()).setComponents(buttons).queue()
            threadMessage.editMessageComponents(buttons).queue()
        }
    }

    private fun calcMove(from: Pair<Int, Int>, to: Pair<Int, Int>, sync: Boolean): Array<Array<FieldsChess>> {
        val figure = board[from.first][from.second]
        return if (sync) {
            board[to.first][to.second] = figure
            board[from.first][from.second] = FieldsChess.EMPTY
            board
        } else {
            val dummyBoard = buildList {
                board.forEach { row ->
                    add(buildList {
                        row.forEach { field ->
                            add(field)
                        }
                    }.toTypedArray())
                }
            }.toTypedArray()
            dummyBoard[to.first][to.second] = figure
            dummyBoard[from.first][from.second] = FieldsChess.EMPTY
            dummyBoard
        }
    }

    override suspend fun setWinner(win: FieldsTwoPlayer) {
        winner = win
        message.editMessageEmbeds(calcEmbed()).setComponents().queue()
        thread.delete().queue()
    }

    private suspend fun checkWin() {
        val white = ChessMoveLogic.checkMate(true, board)
        val black = ChessMoveLogic.checkMate(false, board)
        if (white.second) {
            winner = FieldsTwoPlayer.PLAYER_2
            GoalManager.registerWin(Game.CHESS, false, player1.idLong, guildID)
            return
        } else if (black.second) {
            winner = FieldsTwoPlayer.PLAYER_1
            GoalManager.registerWin(Game.CHESS, false, player2.idLong, guildID)
            return
        }
        val warning = if (white.first) "❗ ${player1.asMention} ${FieldsChess.KING_WHITE} ${msg("chessKingCheck", guildID)} ❗"
        else if (black.first) "❗ ${player2.asMention} ${FieldsChess.KING_BLACK} ${msg("chessKingCheck", guildID)} ❗" else null
        if (warning != null)
            thread.sendMessage(warning).queue()
    }

    init {
        //Game Start
        guildID = guild.idLong

        getDefaultScope().launch {
            val channel = guild.getTextChannelById(channelID)!!
            val selector = calcButtons()
            message = channel.sendMessageEmbeds(calcEmbed()).setComponents(selector).complete()
            thread = message.createThreadChannel("4G - ${member1.nickname ?: member1.user.name} vs ${member2.nickname ?: member2.user.name}").complete()
            threadMessage = thread.sendMessage(" \u1CBC ").setComponents(selector).complete()
            thread.addThreadMember(member1).complete()
            thread.addThreadMember(member2).complete()
            val mention = if (whoPlays) player1 else player2
            thread.sendMessage("${mention.asMention} ${msg("onMove", guildID)}").queue()
        }
    }
}
