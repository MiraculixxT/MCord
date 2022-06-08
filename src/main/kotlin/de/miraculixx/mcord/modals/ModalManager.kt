package de.miraculixx.mcord.modals

import de.miraculixx.mcord.modals.events.RenameModal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ModalManager : ListenerAdapter() {

    private val dropdowns = HashMap<String, Modals>()

    override fun onModalInteraction(it: ModalInteractionEvent) {
        val id = it.modalId
        val commandClass = when {
            id.startsWith("renameCon_") -> dropdowns["rename"] ?: return
            else -> dropdowns[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {
        dropdowns["rename"] = RenameModal()
    }
}