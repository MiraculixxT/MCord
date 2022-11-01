package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.games.chess.ChessDropdown
import de.miraculixx.mcord.modules.games.connectFour.C4DropDown
import de.miraculixx.mcord.modules.games.idle.DropDownHelp
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

object DropDownManager {
    private val dropdowns = mapOf(
        "GIdle_Info" to DropDownHelp(),
        "GAME_CHESS" to ChessDropdown(),
        "GAME_C4" to C4DropDown()
    )

    fun startListen(jda: JDA) = jda.listener<GenericSelectMenuInteractionEvent<String, StringSelectMenu>> {
        val id = it.selectMenu.id ?: return@listener
        val commandClass = when {
            id.startsWith("GAME_CHESS_") -> dropdowns["GAME_CHESS"]
            id.startsWith("GAME_C4_") -> dropdowns["GAME_C4"]

            else -> dropdowns[id]
        }
        commandClass?.trigger(it)
    }
}