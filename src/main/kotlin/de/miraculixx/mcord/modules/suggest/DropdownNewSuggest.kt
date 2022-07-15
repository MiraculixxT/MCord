package de.miraculixx.mcord.modules.suggest

import de.miraculixx.mcord.utils.entities.DropDownEvent
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import kotlin.time.Duration.Companion.seconds

class DropdownNewSuggest: DropDownEvent {
    override suspend fun trigger(it: SelectMenuInteractionEvent) {
        val data = it.selectMenu.id?.removePrefix("SUGGEST_")?.split('_') ?: return
        val member = it.member ?: return
        if (data[1] != member.id) {
            it.reply("```diff\n- This is not your suggestion!\n- Create your own one by typing anything in this channel```").setEphemeral(true).queue()
            return
        }
        when (data[0]) {
            "YES" -> {
                val user = it.jda.retrieveUserById(member.id).complete()
                it.channel.sendTyping().queue()
                val content = it.message.embeds[0].fields[0].value
                val option = it.selectedOptions[0]
                it.message.delete().queue()
                delay(1.seconds)
                it.channel.sendMessageEmbeds(
                    Embed {
                        title = "${option.emoji?.formatted} >> ${option.label}"
                        description = content
                        color = 0x1CE721
                        footer {
                            this.iconUrl = user.avatarUrl
                            this.name = "Suggestion by ${user.name} · ${member.id}"
                        }
                    }
                ).queue()
            }
        }
    }
}