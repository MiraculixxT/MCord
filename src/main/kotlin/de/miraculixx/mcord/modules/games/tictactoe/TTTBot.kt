package de.miraculixx.mcord.modules.games.tictactoe

import de.miraculixx.mcord.modules.games.utils.FieldsTwoPlayer
import de.miraculixx.mcord.modules.games.utils.getOpponent
import de.miraculixx.mcord.utils.Color
import de.miraculixx.mcord.utils.log
import kotlin.random.Random
import kotlin.random.nextInt

class TTTBot(val level: Int, val player: FieldsTwoPlayer) {
    fun getMove(board: Array<Array<FieldsTwoPlayer>>): Pair<Int, Int> {
        "Level $level - Bot Move".log()
        return when (level) {
            1 -> getRandomMove(board)
            2 -> getDangerPosition(board) ?: getRandomMove(board)
            3 -> getSmartMove(board, getMovesCount(board))
            else -> 0 to 0
        }
    }

    private fun getRandomMove(board: Array<Array<FieldsTwoPlayer>>): Pair<Int, Int> {
        return buildList {
            var r = 0
            board.forEach { row ->
                var c = 0
                row.forEach { field ->
                    if (field == FieldsTwoPlayer.EMPTY)
                        add(r to c)
                    c++
                }
                r++
            }
        }.random()
    }

    private fun getSmartMove(board: Array<Array<FieldsTwoPlayer>>, moves: Int): Pair<Int, Int> {
        return when (moves) {
            0 -> when (Random.nextInt(1..4)) { //Place in Corner
                2 -> 2 to 0
                3 -> 0 to 2
                4 -> 2 to 2
                else -> 0 to 0
            }
            1 -> if (board[1][1] != FieldsTwoPlayer.EMPTY) getSmartMove(board, 0) //When middle go to Corner
            else 1 to 1
            else -> getDangerPosition(board) ?: getRandomMove(board)
        }
    }

    private fun getMovesCount(board: Array<Array<FieldsTwoPlayer>>): Int {
        var counter = 0
        board.forEach { row ->
            row.forEach { field ->
                if (field != FieldsTwoPlayer.EMPTY)
                    counter++
            }
        }
        return counter
    }

    private fun getDangerPosition(board: Array<Array<FieldsTwoPlayer>>): Pair<Int, Int>? {
        // ROWS -
        repeat(3) {
            if ((countEntries(board[it], player.getOpponent()) == 2) && (countEntries(board[it], player) == 0)) {
                "BOT - Row Danger ${board[it].lastIndexOf(FieldsTwoPlayer.EMPTY)}".log()
                return it to board[it].lastIndexOf(FieldsTwoPlayer.EMPTY)
            }
        }

        // COLUMNS |
        val turned = (0..2).map { i -> (0..2).map { j -> board[j][i] }.toTypedArray() }
        repeat(3) {
            if ((countEntries(turned[it], player.getOpponent()) == 2) && (countEntries(turned[it], player) == 0)) {
                "BOT - Column Danger ${turned[it].lastIndexOf(FieldsTwoPlayer.EMPTY)}".log()
                return turned[it].lastIndexOf(FieldsTwoPlayer.EMPTY) to it
            }
        }

        // DIAGONALS /\
        val d1 = arrayOf(board[0][0], board[1][1], board[2][2])
        if ((countEntries(d1, player.getOpponent()) == 2) && (countEntries(d1, player) == 0)) {
            "BOT - Diagonal 1 Danger ${d1.lastIndexOf(FieldsTwoPlayer.EMPTY) to d1.lastIndexOf(FieldsTwoPlayer.EMPTY)}".log()
            return d1.lastIndexOf(FieldsTwoPlayer.EMPTY) to d1.lastIndexOf(FieldsTwoPlayer.EMPTY)
        }
        val d2 = arrayOf(board[0][2], board[1][1], board[2][0])
        if ((countEntries(d1, player.getOpponent()) == 2) && (countEntries(d1, player) == 0)) {
            "BOT - Diagonal 2 Danger".log()
            return when (FieldsTwoPlayer.EMPTY) {
                d2[0] -> 0 to 2
                d2[1] -> 1 to 1
                else -> 2 to 0
            }
        }
        "no danger".log(Color.RED)
        // NO DANGER DETECTED
        return null
    }

    private fun countEntries(array: Array<FieldsTwoPlayer>, entry: FieldsTwoPlayer): Int {
        var counter = 0
        array.forEach {
            if (it == entry)
                counter++
        }
        return counter
    }
}