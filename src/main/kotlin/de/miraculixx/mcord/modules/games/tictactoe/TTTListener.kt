package de.miraculixx.mcord.modules.games.tictactoe

import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.entities.ButtonEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class TTTListener : ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val tools = GameTools("TTT", "Tic Tac Toe", Game.TIC_TAC_TOE)
        tools.buttons(it)
    }
}