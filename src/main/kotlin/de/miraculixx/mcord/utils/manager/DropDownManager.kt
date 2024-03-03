package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.suggest.DropdownNewSuggest
import de.miraculixx.mcord.modules.suggest.DropdownSuggest
import de.miraculixx.mcord.modules.utils.commands.AdminCommand
import de.miraculixx.mcord.utils.log.log
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

object DropDownManager {
    private val dropdowns = mapOf(
        "vorschlag" to DropdownSuggest(),
        "SUGGEST" to DropdownNewSuggest(),
        "test2" to AdminCommand()
    )

    fun startListen(jda: JDA) = jda.listener<StringSelectInteractionEvent> {
        val id = it.selectMenu.id ?: return@listener
        println("-> Menu Interaction: $id")
        val commandClass = when {
            id.startsWith("SUGGEST_") -> dropdowns["SUGGEST"]

            else -> dropdowns[id]
        }
        commandClass?.trigger(it)
    }
}