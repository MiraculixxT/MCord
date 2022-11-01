package de.miraculixx.mcord.modules.games.chess

import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.entities.ButtonEvent
import de.miraculixx.mcord.utils.entities.DropDownEvent
import de.miraculixx.mcord.utils.entities.ModalEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

class ChessButton : ButtonEvent, ModalEvent, DropDownEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val member = it.member ?: return
        val id = it.button.id?.removePrefix("GAME_CHESS_") ?: return
        val options = id.split('_')
        when (options[0]) {
            "SEL" -> {
                if (member.id != options[2]) {
                    it.reply("```diff\n- Du bist gerade nicht am Zug!```").setEphemeral(true).queue()
                    return
                }
                val input = TextInput.create("FROM", "Welche Figur möchtest du bewegen?", TextInputStyle.SHORT)
                input.placeholder = "Feld ID (zb. A1, G4, ...)"
                input.isRequired = true
                input.minLength = 2
                input.maxLength = 2
                it.replyModal(
                    Modal.create("GAME_CHESS_TO_${options[1]}", "♟️ || CHESS")
                        .addActionRow(input.build())
                        .build()
                ).queue()
            }

            else -> GameTools("CHESS", "Schach", Game.CHESS).buttons(it)
        }
    }
}