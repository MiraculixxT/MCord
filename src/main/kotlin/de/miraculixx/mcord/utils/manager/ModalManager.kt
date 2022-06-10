package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.mutils.RenameModal
import de.miraculixx.mcord.modules.suggest.VorschlagModal
import de.miraculixx.mcord.utils.entities.Modals
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
            id.startsWith("vorschlag") -> dropdowns["vorschlag"] ?: return
            else -> dropdowns[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {
        dropdowns["rename"] = RenameModal()
        dropdowns["vorschlag"] = VorschlagModal()
    }
}