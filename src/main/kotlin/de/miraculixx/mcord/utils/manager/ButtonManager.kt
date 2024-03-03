package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.suggest.ButtonNewSuggest
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object ButtonManager {
    private val buttons = mapOf(
        "SUGGEST" to ButtonNewSuggest(),
    )

    fun startListen(jda: JDA) = jda.listener<ButtonInteractionEvent> {
        val id = it.button.id ?: return@listener
        println("-> Button Interaction: $id")
        val commandClass = when {
            id.startsWith("SUGGEST_") -> buttons["SUGGEST"]
            id.startsWith("NOTIFY:") -> buttons["NOTIFY"]
            id.startsWith("RULES:") -> buttons["RULES"]

            else -> buttons[id]
        }
        commandClass?.trigger(it)
    }
}