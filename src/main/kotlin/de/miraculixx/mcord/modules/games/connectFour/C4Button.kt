@file:Suppress("BooleanLiteralArgument")

package de.miraculixx.mcord.modules.games.connectFour

import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.utils.entities.ButtonEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class C4Button : ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val tools = GameTools("4G", "4 Gewinnt", Game.FOUR_WINS)
        tools.buttons(it)
    }
}