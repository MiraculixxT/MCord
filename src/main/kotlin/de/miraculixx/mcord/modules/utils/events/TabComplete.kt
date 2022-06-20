package de.miraculixx.mcord.modules.utils.events

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class TabComplete : ListenerAdapter() {

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        when (event.name) {
            "language" -> {
                event.replyChoiceStrings("german", "english").queue()
            }
            "admin" -> {
                event.replyChoiceStrings("status", "delete-threads", "idle-game").queue()
            }
            "key-update" -> {
                event.replyChoiceStrings("Booster", "Subscriber", "Unlimited", "Lite").queue()
            }
        }
    }
}