package de.miraculixx.mcord.modules.games.chess

import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.entities.ModalEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import java.util.*
import java.util.regex.Pattern

class ChessModal: ModalEvent {
    override suspend fun trigger(it: ModalInteractionEvent) {
        val options = it.modalId.removePrefix("GAME_CHESS_").split('_')
        val id = it.getValue("FROM")?.asString ?: return
        it.deferReply(true).complete()
        val hook = it.hook
        val game = GameManager.getGame(it.guild?.idLong ?: return, Game.CHESS, UUID.fromString(options[1])) as ChessGame

        if (Pattern.matches("[a-hA-H][1-8]", id)) // A1
            game.interactTo(id[1].digitToInt() - 1 to id[0].uppercaseChar(), hook)
        else if (Pattern.matches("[1-8][a-hA-H]", id)) // 1A
            game.interactTo(id[0].digitToInt() - 1 to id[1].uppercaseChar(), hook)
        else hook.editOriginal("```diff\n- $id ist kein gültiges Feld!\n- Nutzung: A1, B6, ...```").queue()
    }
}