package de.miraculixx.mcord.modules.trivia

import de.miraculixx.mcord.utils.entities.ButtonEvent
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.editMessage_
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class TriviaButton : ButtonEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val id = it.button.id ?: return
        val split = id.split(':')
        if (split.firstOrNull() != "TRIVIA") return
        val userID = it.user.id
        if (split[1] != userID) {
            it.reply_("```diff\n- This is not your Question!\n- Generate one with /trivia```", ephemeral = true).queue()
            return
        }
        if (split[2] == "REPLAY") {
            it.editButton(it.button.asDisabled()).queue()
            val message = it.message
            val gen = generateQuestion(TriviaCategory.RANDOM, TriviaDifficulty.RANDOM, userID)
            message.editMessageEmbeds(listOf(gen.first)).setComponents(listOf(gen.second)).queue()
            return
        }
        val isFalse = split[2] != "1"
        val message = it.message
        val components = message.components.first()
        val embed = message.embeds.first()
        val replay = ActionRow.of(button("TRIVIA:$userID:REPLAY", "Replay", Emoji.fromUnicode("\uD83D\uDD01"), ButtonStyle.PRIMARY))
        if (isFalse) {

            it.editMessage_(null, listOf(Embed {
                title = embed.title
                description = embed.description?.replace("```fix\n", "```diff\n- ")
                color = 0xc21111
            }), listOf(ActionRow.of(buildList {
                components.forEach { com ->
                    com as Button
                    if (id == com.id) add(com.asDisabled().withStyle(ButtonStyle.DANGER))
                    else if (com.id!!.endsWith('1')) add(com.asDisabled().withStyle(ButtonStyle.PRIMARY))
                    else add(com.asDisabled())
                }
            }), replay
            )).queue()

        } else {

            it.editMessage_(null, listOf(Embed {
                title = embed.title
                description = embed.description?.replace("```fix\n", "```diff\n+ ")
                color = 0x00800f
            }), listOf(ActionRow.of(buildList {
                components.forEach { com ->
                    com as Button
                    if (id == com.id) add(com.asDisabled().withStyle(ButtonStyle.SUCCESS))
                    else add(com.asDisabled())
                }
            }), replay
            )).queue()

        }
    }
}