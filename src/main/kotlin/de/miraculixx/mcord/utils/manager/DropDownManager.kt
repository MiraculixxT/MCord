package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.mutils.DropdownServer
import de.miraculixx.mcord.modules.suggest.DropdownSuggest
import de.miraculixx.mcord.utils.entities.DropDownEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DropDownManager : ListenerAdapter() {

    private val dropdowns = HashMap<String, DropDownEvent>()

    override fun onSelectMenuInteraction(it: SelectMenuInteractionEvent) {
        val id = it.selectMenu.id ?: return
        val commandClass = when {
            id.startsWith("editcons_") -> dropdowns["serverSelect"] ?: return

            else -> dropdowns[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {
        dropdowns["serverSelect"] = DropdownServer()
        dropdowns["vorschlag"] = DropdownSuggest()
    }
}