package de.miraculixx.mcord.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface SlashCommands {
    suspend fun trigger(it: SlashCommandInteractionEvent) {}
}