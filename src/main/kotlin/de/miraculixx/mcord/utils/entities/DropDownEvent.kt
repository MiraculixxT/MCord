package de.miraculixx.mcord.utils.entities

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

interface DropDownEvent {
    suspend fun trigger(it: StringSelectInteractionEvent) {}
}