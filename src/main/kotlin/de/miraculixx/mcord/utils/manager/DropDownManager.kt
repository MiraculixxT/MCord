package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.suggest.DropdownNewSuggest
import de.miraculixx.mcord.modules.suggest.DropdownSuggest
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

object DropDownManager {
    private val dropdowns = mapOf(
        "vorschlag" to DropdownSuggest(),
        "SUGGEST" to DropdownNewSuggest()
    )

    fun startListen(jda: JDA) {
        jda.listener<SelectMenuInteractionEvent> {
            val id = it.selectMenu.id ?: return@listener
            val commandClass = when {
                id.startsWith("SUGGEST_") -> dropdowns["SUGGEST"]

                else -> dropdowns[id]
            }
            commandClass?.trigger(it)
        }
    }
}