package de.miraculixx.mcord.modules.games.fourWins

import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

class FIARCommand: SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val member = it.member ?: return
        val opponent = it.options.first().asMember ?: return
        if (opponent.id == member.id) {
            it.reply("```diff\n- Du kannst dich nicht selbst herausfordern!```")
                .setEphemeral(true).queue()
            return
        }
        it.reply(
            "\uD83C\uDFAE **|| 4 GEWINNT**\n" +
                    "${opponent.asMention} - Du wurdest herausgefordert zu einem 4 Gewinnt Match von ${member.asMention}!\n" +
                    "> Bist du bereit für das Spiel? Klicke auf `Annehmen`"
        ).addActionRow(
            Button.success("GAME_4G_YES_${member.id}_${opponent.id}", "Annehmen").withEmoji(Emoji.fromUnicode("✔️")),
            Button.danger("GAME_4G_NO_${opponent.id}", "Ablehnen").withEmoji(Emoji.fromUnicode("✖️"))
        ).queue()
    }
}