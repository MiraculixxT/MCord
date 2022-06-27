package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.games.chess.ChessListener
import de.miraculixx.mcord.modules.mutils.ModalRename
import de.miraculixx.mcord.modules.suggest.ModalSuggest
import de.miraculixx.mcord.utils.entities.ModalEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ModalManager : ListenerAdapter() {

    private val modals = HashMap<String, ModalEvent>()

    override fun onModalInteraction(it: ModalInteractionEvent) {
        val id = it.modalId
        val commandClass = when {
            id.startsWith("renameCon_") -> modals["rename"] ?: return
            id.startsWith("vorschlag") -> modals["vorschlag"] ?: return
            id.startsWith("GAME_CHESS_") -> modals["chess"] ?: return
            else -> modals[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {
        modals["rename"] = ModalRename()
        modals["vorschlag"] = ModalSuggest()
        modals["chess"] = ChessListener()
    }
}