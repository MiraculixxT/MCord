package de.miraculixx.mcord.modules.games.chess

import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.Games
import de.miraculixx.mcord.utils.entities.ButtonEvent
import de.miraculixx.mcord.utils.entities.DropDownEvent
import de.miraculixx.mcord.utils.entities.ModalEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import java.util.*

class ChessListener : ButtonEvent, ModalEvent, DropDownEvent {
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
            else -> GameTools("CHESS", "Schach", Games.CHESS).buttons(it)
        }
    }

    override suspend fun trigger(it: SelectMenuInteractionEvent) {
        val id = it.selectMenu.id?.removePrefix("GAME_CHESS_") ?: return
        val options = id.split('_')
        val data = it.selectedOptions.first().value.split('_')
        val member = it.member ?: return
        GameManager.getGame(Games.CHESS, UUID.fromString(options[1]))?.interact(data, member, it)
    }

    override suspend fun trigger(it: ModalInteractionEvent) {
        val options = it.modalId.removePrefix("GAME_CHESS_").split('_')
        val id = it.getValue("FROM")?.asString ?: return
        val game = GameManager.getGame(Games.CHESS, UUID.fromString(options[1])) as ChessGame
        it.deferReply(true).complete()
        val digit = id[1].digitToIntOrNull()
        if (digit == null) {
            it.hook.editOriginal("```diff\n- $id ist kein gültiges Feld!\n- Nutzung: A1, B6, ...```").queue()
            return
        }
        game.interactTo(id[1].digitToInt() - 1  to id[0].uppercaseChar(), it.hook)
    }
}