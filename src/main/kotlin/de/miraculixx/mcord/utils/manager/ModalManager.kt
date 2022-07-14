package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.games.chess.ChessListener
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object ModalManager {
    private val modals = mapOf(
        "chess" to ChessListener()
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