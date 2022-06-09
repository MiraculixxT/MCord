package de.miraculixx.mcord.dropdowns

import de.miraculixx.mcord.dropdowns.events.ServerManagerMenu
import de.miraculixx.mcord.dropdowns.events.VorschlagSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DropDownManager : ListenerAdapter() {

    private val dropdowns = HashMap<String, DropDowns>()

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
        dropdowns["serverSelect"] = ServerManagerMenu()
        dropdowns["vorschlag"] = VorschlagSystem()
    }
}