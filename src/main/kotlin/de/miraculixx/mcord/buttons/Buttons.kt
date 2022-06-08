package de.miraculixx.mcord.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

interface Buttons {
    suspend fun trigger(it: ButtonInteractionEvent) {}
}