package de.miraculixx.mcord.modules.suggest

import de.miraculixx.mcord.utils.entities.ButtonEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonNewSuggest: ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        if (it.button.id?.contains(it.member?.id ?: return) == true)
            it.message.delete().queue()
    }
}