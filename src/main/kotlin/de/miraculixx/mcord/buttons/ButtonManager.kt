package de.miraculixx.mcord.buttons

import de.miraculixx.mcord.buttons.events.DeleteUserButton
import de.miraculixx.mcord.buttons.events.ServerEditButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ButtonManager : ListenerAdapter() {

    private val buttons = HashMap<String, Buttons>()

    override fun onButtonInteraction(it: ButtonInteractionEvent) {
        val id = it.button.id ?: return
        val commandClass = when {
            id.startsWith("deleteuser_") -> buttons["deleteUser"] ?: return
            id.startsWith("conButton") -> buttons["connectionManager"] ?: return
            else -> buttons[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {
        buttons["deleteUser"] = DeleteUserButton()
        buttons["connectionManager"] = ServerEditButton()
    }
}