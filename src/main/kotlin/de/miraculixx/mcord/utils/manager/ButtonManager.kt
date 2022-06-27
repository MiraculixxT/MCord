package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.games.chess.ChessListener
import de.miraculixx.mcord.modules.games.fourWins.C4Listener
import de.miraculixx.mcord.modules.games.idle.ButtonLoadBuildings
import de.miraculixx.mcord.modules.games.idle.ButtonLoadUpgrades
import de.miraculixx.mcord.modules.games.tictactoe.TTTListener
import de.miraculixx.mcord.modules.mutils.ButtonDeleteUser
import de.miraculixx.mcord.modules.mutils.ButtonServer
import de.miraculixx.mcord.utils.entities.ButtonEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ButtonManager : ListenerAdapter() {

    private val buttons = HashMap<String, ButtonEvent>()

    override fun onButtonInteraction(it: ButtonInteractionEvent) {
        val id = it.button.id ?: return
        val commandClass = when {
            id.startsWith("deleteuser_") -> buttons["deleteUser"] ?: return
            id.startsWith("conButton") -> buttons["connectionManager"] ?: return
            id.startsWith("GAME_TTT_") -> buttons["GAME_TTT"] ?: return
            id.startsWith("GAME_4G_") -> buttons["GAME_4G"] ?: return
            id.startsWith("GAME_CHESS_") -> buttons["GAME_CHESS"] ?: return
            else -> buttons[id] ?: return
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    init {
        buttons["deleteUser"] = ButtonDeleteUser()
        buttons["connectionManager"] = ButtonServer()
        buttons["GIdle_LoadUpgrades"] = ButtonLoadUpgrades()
        buttons["GIdle_LoadBuildings"] = ButtonLoadBuildings()
        buttons["GAME_TTT"] = TTTListener()
        buttons["GAME_4G"] = C4Listener()
        buttons["GAME_CHESS"] = ChessListener()
    }
}