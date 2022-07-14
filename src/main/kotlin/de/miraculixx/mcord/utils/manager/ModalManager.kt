package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.games.chess.ChessModal
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

object ModalManager {
    private val modals = mapOf(
        "chess" to ChessModal()
    )

    fun startListen(jda: JDA) = jda.listener<ModalInteractionEvent> {
        val id = it.modalId
        val commandClass = when {
            id.startsWith("GAME_CHESS_") -> modals["chess"]
            else -> modals[id]
        }
        commandClass?.trigger(it)
    }
}