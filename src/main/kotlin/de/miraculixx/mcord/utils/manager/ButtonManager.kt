package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.idle.ButtonLoadBuildings
import de.miraculixx.mcord.modules.games.idle.ButtonLoadUpgrades
import de.miraculixx.mcord.modules.mutils.ButtonDeleteUser
import de.miraculixx.mcord.modules.mutils.ButtonServer
import de.miraculixx.mcord.utils.entities.ButtonEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ButtonManager : ListenerAdapter() {

    private val buttons = HashMap<String, ButtonEvent>()

    override fun onButtonInteraction(it: ButtonInteractionEvent) {
        val id = it.button.id ?: return
        val commandClass = when {
            id.startsWith("deleteuser_") -> buttons["deleteUser"] ?: return
            id.startsWith("conButton") -> buttons["connectionManager"] ?: return
            id.startsWith("GAME_TTT") -> GameManager
            else -> buttons[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {
        buttons["deleteUser"] = ButtonDeleteUser()
        buttons["connectionManager"] = ButtonServer()
        buttons["GIdle_LoadUpgrades"] = ButtonLoadUpgrades()
        buttons["GIdle_LoadBuildings"] = ButtonLoadBuildings()
    }
}