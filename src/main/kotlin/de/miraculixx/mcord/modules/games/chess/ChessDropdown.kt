package de.miraculixx.mcord.modules.games.chess

import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.entities.DropDownEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import java.util.*

class ChessDropdown: DropDownEvent {
    override suspend fun trigger(it: SelectMenuInteractionEvent) {
        val id = it.selectMenu.id?.removePrefix("GAME_CHESS_") ?: return
        val options = id.split('_')
        val data = it.selectedOptions.first().value.split('_')
        val member = it.member ?: return
        GameManager.getGame(it.guild?.idLong ?: return, Game.CHESS, UUID.fromString(options[1]))?.interact(data, member, it)
    }
}