package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.events.awaitMessage
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import kotlin.time.Duration.Companion.minutes

class MessagesCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val sub = it.subcommandName
        val channel = it.channel
        when (sub) {
            "add-link" -> {
                val message = getMessage(channel, it) ?: return
                val link = it.getOption("link")!!.asString
                val label = it.getOption("label")!!.asString
                val emote = it.getOption("emote")!!.asString
                message.editMessageComponents(ActionRow.of(
                    buildList {
                        addAll(message.buttons)
                        add(Button.link(link, label).withEmoji(Emoji.fromFormatted(emote)))
                    }
                )).queue()
            }
            "remove-buttons" -> {
                val message = getMessage(channel, it) ?: return
                message.editMessageComponents().queue()
            }
            "embed-create" -> {
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
            "embed-edit" -> {
                val titleO = it.getOption("title")?.asString
                val descriptionO = it.getOption("description")?.asString
                val urlO = it.getOption("title-url")?.asString
                val colorO = it.getOption("color")?.asInt
                val footerO = it.getOption("footer")?.asString
                val footerIconO = it.getOption("footer-icon")?.asString

                val message = getMessage(channel, it) ?: return
                val embeds = message.embeds
                if (embeds.isEmpty()) {
                    it.reply("```diff\n- This message has no Embeds```").setEphemeral(true).queue()
                    return
                }
                val code = it.getOption("code")?.asString
                if (code != null) {
                    try {
                        val embed = (it.jda as JDAImpl).entityBuilder.createMessageEmbed(DataObject.fromJson(code))!!
                        message.editMessageEmbeds().queue()
                        message.editMessageEmbeds(embed).queue()
                    } catch (_: Exception) {
                        it.reply("```diff\n- Invalid JSON Code```").setEphemeral(true).queue()
                    }
                    return
                }

                val embed = embeds.first()
                val newEmbed = Embed {
                    title = titleO ?: embed.title
                    description = descriptionO ?: embed.description
                    url = urlO ?: embed.url
                    color = colorO ?: embed.colorRaw
                    if (embed.footer?.text != null || footerO != null) footer {
                        this.name = footerO ?: embed.footer?.text ?: "⠀"
                        this.iconUrl = footerIconO ?: embed.footer?.iconUrl
                    }
                    embed.fields.forEach { field ->
                        field {
                            this.name = field.name ?: "⠀"
                            this.value = field.value ?: "⠀"
                            this.inline = field.isInline
                        }
                    }
                }
                message.editMessageEmbeds(newEmbed).queue()
            }
            "send" -> {
                val ping = it.getOption("ping")?.asRole
                it.reply_("Send a message to resend from bot", ephemeral = true).queue()
                withTimeoutOrNull(5.minutes) {
                    val message = it.channel.awaitMessage()
                    it.channel.sendMessage("${message.contentRaw}${if (ping != null) "\n${ping.asMention}" else ""}").queue()
                    message.delete().queue()
                    it.hook.editOriginal("Message Send").queue()
                } ?: it.hook.editOriginal("Timed out").queue()
            }
        }
        it.reply("Done").setEphemeral(true).queue()
    }

    private fun getMessage(channel: MessageChannel, it: SlashCommandInteractionEvent): Message? {
        val message = channel.retrieveMessageById(it.getOption("msg-id")?.asString ?: "123").complete()
        if (message == null || message.author != it.jda.selfUser) {
            it.reply("```diff\n- Not a valid message ID```").setEphemeral(true).queue()
        }
        return message
    }
}