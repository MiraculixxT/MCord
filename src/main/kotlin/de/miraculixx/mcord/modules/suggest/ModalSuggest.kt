package de.miraculixx.mcord.modules.suggest

import de.miraculixx.mcord.utils.entities.ModalEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

class ModalSuggest : ModalEvent {
    override suspend fun trigger(it: ModalInteractionEvent) {
        val id = it.modalId
        val jda = it.jda
        val splitter = id.split('_')
        if (splitter.size < 2) return
        val user = jda.retrieveUserById(splitter[1]).complete()
        val channel = when (splitter[0]) {
            "vorschlag1" -> jda.getTextChannelById(902227899157991434)
            "vorschlag2" -> jda.getTextChannelById(851036810560798750)
            "vorschlag3" -> jda.getTextChannelById(746752229821644862)
            else -> return
        }
        val embed = EmbedBuilder()
            .setTitle(it.getValue("name")?.asString)
            .setDescription(it.getValue("desc")?.asString)
            .setFooter("Vorschlag von ${user?.name} · ${user?.id}", user?.avatarUrl)
            .setColor(0x1CE721)
        it.reply("Vielen Dank für deinen Vorschlag!\nDu findest ihn hier -> ${channel?.asMention}").setEphemeral(true).queue()
        channel?.sendTyping()?.queue()
        val message = channel?.sendMessage(" ")?.setEmbeds(embed.build())?.complete()
        message?.addReaction(Emoji.fromUnicode("\uD83D\uDD3B"))?.queue()
        message?.addReaction(Emoji.fromUnicode("\uD83D\uDD3A"))?.queue()
    }
}