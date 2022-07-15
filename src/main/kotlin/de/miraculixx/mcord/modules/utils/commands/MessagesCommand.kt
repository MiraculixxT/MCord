package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl

class MessagesCommand: SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val sub = it.subcommandName
        val channel = it.channel
        when (sub) {
            "add-link" -> {
                val message = channel.retrieveMessageById(it.getOption("msg-id")!!.asString).complete()
                val link = it.getOption("link")!!.asString
                val label = it.getOption("label")!!.asString
                val emote = it.getOption("emote")!!.asString
                message.editMessageComponents(ActionRow.of(
                    buildList {
                        addAll(message.buttons)
                        add(Button.link(link, label).withEmoji(Emoji.fromFormatted(emote)))
                    }
                ))
            }
            "create-embed" -> {
                val code = it.getOption("code")?.asString
                val embed = if (code != null) (it.jda as JDAImpl).entityBuilder.createMessageEmbed(DataObject.fromJson(code))!!
                else Embed { title = "A new Embed" }
                if (it.getOption("msg-id") != null) {
                    val message = channel.retrieveMessageById(it.getOption("msg-id")!!.asString).complete()
                    message.editMessageEmbeds(buildList {
                        addAll(message.embeds)
                        add(embed)
                    }).queue()
                } else channel.sendMessageEmbeds(embed).queue()
            }
        }
    }
}