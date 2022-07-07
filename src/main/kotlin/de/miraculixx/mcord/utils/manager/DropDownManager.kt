package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.games.chess.ChessListener
import de.miraculixx.mcord.modules.games.idle.DropDownHelp
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
            id.startsWith("GAME_CHESS_") -> dropdowns["GAME_CHESS"] ?: return

            else -> dropdowns[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {
        dropdowns["GIdle_Info"] = DropDownHelp()
        dropdowns["GAME_CHESS"] = ChessListener()
    }
}