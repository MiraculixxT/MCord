package de.miraculixx.mcord.modules.games.connectFour

import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.utils.api.callCustomAPI

// a = 2 | b = 1
class C4Bot(val level: Int) {
    private val depth = when (level) {
        3 -> 5
        2 -> 3
        else -> 2
    }

    // Returns Column
    suspend fun getNextMove(array: Array<Array<FieldsTwoPlayer>>): Int {
        var data = ""
        array.forEach { row ->
            row.forEach { field ->
                data += when (field) {
                    FieldsTwoPlayer.EMPTY -> 0
                    FieldsTwoPlayer.PLAYER_1 -> 1
                    FieldsTwoPlayer.PLAYER_2 -> 2
                }
            }
        }

        val url = "https://miraculixx.de/mcord/api/c4?board_data=$data&player=2&depth=$depth"
        val response = callCustomAPI(url)
        return response.toIntOrNull() ?: 0
    }
}