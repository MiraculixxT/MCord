package de.miraculixx.mcord.modules.games.fourWins

import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.Games
import de.miraculixx.mcord.utils.entities.ButtonEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class FIARListener : ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val tools = GameTools("4G", "4 Gewinnt")
        tools.buttons(it, Games.FOUR_WINS)
    }
}