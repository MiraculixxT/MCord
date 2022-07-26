package de.miraculixx.mcord.modules.utils.events

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

object TabComplete {
    fun startListen(jda: JDA) = jda.listener<CommandAutoCompleteInteractionEvent> {
        when ("${it.name}:${it.subcommandName}") {
            "connect-4:bot" -> it.replyChoiceStrings("Hard", "Medium", "Easy").queue()
            "tictactoe:bot" -> it.replyChoiceStrings("Hard", "Medium", "Easy").queue()
            "setup:language" -> it.replyChoiceStrings("German", "English").queue()
        }
    }
}