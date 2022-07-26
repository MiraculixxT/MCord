package de.miraculixx.mcord.modules.games.utils

import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

interface SimpleGame {
    suspend fun interact(options: List<String>, interactor: Member, event: GenericComponentInteractionCreateEvent?) = coroutineScope {}

    suspend fun setWinner(win: FieldsTwoPlayer)
}