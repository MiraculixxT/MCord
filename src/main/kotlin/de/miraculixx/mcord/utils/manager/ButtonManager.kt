package de.miraculixx.mcord.utils.manager

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

            else -> buttons[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {

    }
}