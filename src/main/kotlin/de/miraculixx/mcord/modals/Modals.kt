package de.miraculixx.mcord.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

interface Modals {
    suspend fun trigger(it: ModalInteractionEvent) {}
}