package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.suggest.ModalSuggest
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

object ModalManager {

    private val modals = mapOf(
        "vorschlag" to ModalSuggest(),
    )

    fun startListen(jda: JDA) {
        jda.listener<ModalInteractionEvent> {
            val id = it.modalId
            val commandClass = when {
                id.startsWith("vorschlag") -> modals["vorschlag"]
                id.startsWith("SUGGEST_") -> modals["SUGGEST"]

                else -> modals[id]
            }
            commandClass?.trigger(it)
        }
    }
}