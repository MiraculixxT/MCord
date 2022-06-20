package de.miraculixx.mcord.modules.games.tictactoe

import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

class TTTCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val member = it.member ?: return
        val opponent = it.options.first().asMember ?: return
        if (opponent.id == member.id) {
            it.reply("```diff\n- Du kannst dich nicht selbst herausfordern!```")
                .setEphemeral(true).queue()
            return
        }
        it.reply(
            "\uD83C\uDFAE **|| TIC TAC TOE**\n" +
                    "${opponent.asMention} - Du wurdest herausgefordert zu einem Tic-Tac-Toe match von ${member.asMention}!\n" +
                    "> Bist du bereit für das Spiel? Klicke auf `Annehmen`"
        ).addActionRow(
            Button.success("GAME_TTT_YES_${member.id}_${opponent.id}", "Annehmen").withEmoji(Emoji.fromUnicode("✔️")),
            Button.danger("GAME_TTT_NO_${opponent.id}", "Ablehnen").withEmoji(Emoji.fromUnicode("✖️"))
        ).queue()
    }
}