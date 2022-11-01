package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.games.chess.ChessButton
import de.miraculixx.mcord.modules.games.connectFour.C4Button
import de.miraculixx.mcord.modules.games.idle.ButtonLoadBuildings
import de.miraculixx.mcord.modules.games.idle.ButtonLoadUpgrades
import de.miraculixx.mcord.modules.games.tictactoe.TTTListener
import de.miraculixx.mcord.modules.trivia.TriviaButton
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object ButtonManager {
    private val buttons = mapOf(
        "GIdle_LoadUpgrades" to ButtonLoadUpgrades(),
        "GIdle_LoadBuildings" to ButtonLoadBuildings(),
        "GAME_TTT" to TTTListener(),
        "GAME_4G" to C4Button(),
        "GAME_CHESS" to ChessButton(),
        "TRIVIA" to TriviaButton()
    )

    fun startListen(jda: JDA) = jda.listener<ButtonInteractionEvent> {
        val id = it.button.id ?: return@listener
        val commandClass = when {
            id.startsWith("GAME_TTT_") -> buttons["GAME_TTT"]
            id.startsWith("GAME_4G_") -> buttons["GAME_4G"]
            id.startsWith("GAME_CHESS_") -> buttons["GAME_CHESS"]
            id.startsWith("TRIVIA:") -> buttons["TRIVIA"]
            else -> buttons[id]
        }
        commandClass?.trigger(it)
    }
}