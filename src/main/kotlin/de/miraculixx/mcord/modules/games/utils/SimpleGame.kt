package de.miraculixx.mcord.modules.games.utils

import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

interface SimpleGame {
    suspend fun interact(options: List<String>, interactor: Member, event: ButtonInteractionEvent) = coroutineScope {}
}