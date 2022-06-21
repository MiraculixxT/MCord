package de.miraculixx.mcord.modules.games.fourWins

import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class FIARCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val tools = GameTools("4G", "4 Gewinnt")
        tools.command(it)
    }
}