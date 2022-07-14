package de.miraculixx.mcord.modules.games.chess

import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.utils.Game
import de.miraculixx.mcord.utils.entities.ModalEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import java.util.*

class ChessModal: ModalEvent {
    override suspend fun trigger(it: ModalInteractionEvent) {
        val options = it.modalId.removePrefix("GAME_CHESS_").split('_')
        val id = it.getValue("FROM")?.asString ?: return
        val game = GameManager.getGame(it.guild?.idLong ?: return, Game.CHESS, UUID.fromString(options[1])) as ChessGame
        it.deferReply(true).complete()
        val digit = id[1].digitToIntOrNull()
        if (digit == null) {
            it.hook.editOriginal("```diff\n- $id ist kein gültiges Feld!\n- Nutzung: A1, B6, ...```").queue()
            return
        }
        game.interactTo(id[1].digitToInt() - 1 to id[0].uppercaseChar(), it.hook)
    }
}