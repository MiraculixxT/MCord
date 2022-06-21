package de.miraculixx.mcord.modules.games.tictactoe

import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class TTTCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val tools = GameTools("TTT", "Tic Tac Toe")
        tools.command(it)
    }
}