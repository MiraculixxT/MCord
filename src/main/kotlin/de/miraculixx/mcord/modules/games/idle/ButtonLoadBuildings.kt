package de.miraculixx.mcord.modules.games.idle

import de.miraculixx.mcord.utils.entities.ButtonEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonLoadBuildings : ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        it.deferReply(true).queue()
    }
}