package de.miraculixx.mcord.modules.utils.events

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

class TabComplete {
    fun startListen(jda: JDA) = jda.listener<CommandAutoCompleteInteractionEvent> {
        when (it.name) {
            "admin" -> {
                it.replyChoiceStrings("status", "delete-threads", "idle-game", "add-all-role", "test1", "test2").queue()
            }
        }
    }
}