package de.miraculixx.mcord.modules.games.chess

import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.entities.DropDownEvent
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import java.util.*

class ChessDropdown : DropDownEvent {
    override suspend fun trigger(it: GenericSelectMenuInteractionEvent<String, StringSelectMenu>) {
        val id = it.selectMenu.id?.removePrefix("GAME_CHESS_") ?: return
        val options = id.split('_')
        val data = it.values.first().split('_')
        val member = it.member ?: return
        GameManager.getGame(it.guild?.idLong ?: return, Game.CHESS, UUID.fromString(options[1]))?.interact(data, member, it)
    }
}