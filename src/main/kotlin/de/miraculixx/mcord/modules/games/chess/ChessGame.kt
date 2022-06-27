package de.miraculixx.mcord.modules.games.chess

import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.utils.SimpleGame
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import java.util.*
import kotlin.random.Random

class ChessGame(
    private val member1: Member, private val member2: Member, private val uuid: UUID, guild: Guild, channelID: Long
) : SimpleGame {

    // Who is playing the next step
    // True - P1 (white) || False - P2 (black)
    //private var bot: C4Bot? = null
    private var whoPlays = Random.nextBoolean()
    private var winner: FieldsTwoPlayer? = null
    private val message: Message
    private val threadMessage: Message
    private val thread: ThreadChannel
    private val fields = arrayOf( // row - column
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
        val id = if (whoPlays) member1.id else member2.id
        return listOf(
            ActionRow.of(
                Button.primary("GAME_CHESS_SEL_${uuid}_$id", "Figur Verschieben").withEmoji(Emoji.fromUnicode("♟️"))
            )
        )
    }

    private fun calcEmbed(): List<MessageEmbed> {
        val builder = EmbedBuilder().setTitle("<:gamespot:988131155159183420> || SCHACH").setDescription(
            "⬜ - Spieler 1 ${member1.asMention}\n" + "<:black:990247806436540428> - Spieler 2 ${member2.asMention}"
        )
        val field = EmbedBuilder()
            .setTitle("~~<                                                                  >~~")
        if (winner == null) {
            val mention = if (whoPlays) member1.asMention else member2.asMention
            builder.addField(
                "~~<                                                                            >~~", "> $mention ist am Zug", false
            ).setColor(0xb8800b)
            field.setColor(0xb8800b)
        } else {
            val msg = when (winner!!) {
                FieldsTwoPlayer.EMPTY -> "Unentschieden"
                FieldsTwoPlayer.PLAYER_1 -> "${member1.asMention} hat gewonnen"
                FieldsTwoPlayer.PLAYER_2 -> "${member2.asMention} hat gewonnen"
            }
            builder.addField(
                "~~<                                                                            >~~", "> $msg", false
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
                    val f = fields[row][column]
                    append(
                        if (((row + column) % 2) == 0) f.light else f.dark
                    )
                }
                appendLine()
            }
            append("<:blanc:784059217890770964><:blanc:784059217890770964><:AA:989888088429961276><:BB:989888374024327218><:CC:989888535286931476><:DD:989888642552061952><:EE:989888852380500029><:FF:989889099232071800><:GG:989889581329571890><:HH:989890374455676958>")
        }
        println(desc.length)
        field.setDescription(desc)

        return listOf(builder.build(), field.build())
    }

    /**
     * @param position Pair(row, column)
     */
    fun interactTo(position: Pair<Int, Char>, hook: InteractionHook) {
        val newPos = 7 - position.first to columnChars.lastIndexOf(position.second)
        val field = fields[newPos.first][newPos.second]
        println("$newPos - ${field.name}")
        if (field == FieldsChess.EMPTY || field.white != whoPlays) {
            val color = if (whoPlays) "weißen" else "schwarzen"
            hook.editOriginal("```diff\n- Auf dem ausgewählte Feld steht keine deiner Figuren!\n- Du bewegst die $color Figuren```").queue()
            return
        }

        val list = ChessMoveLogic.movePawn(whoPlays, newPos, fields)
        val msg = "Wähle ein Feld aus, auf welches du deine Figur setzen möchtest!"
        if (list.size > 5) {
            //Dropdown
            val dd = SelectMenu.create("GAME_CHESS_P_${uuid}")
            dd.placeholder = "Mögliche Züge"
            dd.maxValues = 1
            dd.minValues = 1
            list.forEach { p ->
                println("Button $p")
                dd.addOption("Feld ${columnChars[p.second]}${p.first + 1}", "${p.first}_${p.second}")
            }
            hook.editOriginal(msg).setActionRow(dd.build()).queue()
        } else {
            val buttons = buildList {
                list.forEach { p ->
                    println("Button $p")
                    add(Button.secondary("${p.first}_${p.second}", "${columnChars[p.second]}${p.first + 1}"))
                }
            }
            hook.editOriginal(msg).setActionRow(buttons).queue()
        }
    }

    override suspend fun interact(options: List<String>, interactor: Member, event: ButtonInteractionEvent) {

    }

    init {
        //Game Start
        val channel = guild.getTextChannelById(channelID)!!
        val selector = calcButtons()
        message = channel.sendMessageEmbeds(calcEmbed()).setActionRows(selector).complete()
        thread = message.createThreadChannel("4G - ${member1.nickname ?: member1.user.name} vs ${member2.nickname ?: member2.user.name}").complete()
        threadMessage = thread.sendMessage(" \u1CBC ").setActionRows(selector).complete()
        thread.addThreadMember(member1).complete()
        thread.addThreadMember(member2).complete()
        val mention = if (whoPlays) member1 else member2
        thread.sendMessage("${mention.asMention} du bist am Zug!").queue()
    }
}
