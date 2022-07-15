package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class LetMeGoogleCommand: SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val link = it.getOption("search")!!.asString.replace(' ', '+')
        val mention = if (it.getOption("ping") != null)
            it.getOption("ping")!!.asUser.asMention else ""
        it.reply(" $mention\n<https://letmegooglethat.com/?q=$link>").queue()
    }
}