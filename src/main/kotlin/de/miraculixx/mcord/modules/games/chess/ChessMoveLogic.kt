package de.miraculixx.mcord.modules.games.chess

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

// Pair<Row, Column>
object ChessMoveLogic {

    /*
    Move Logic - Generell
     */
    fun movePawn(white: Boolean, postion: Pair<Int, Int>, fields: Array<Array<FieldsChess>>): List<Pair<Int, Int>> {
        return buildList {
            val (row, column) = postion
            val sourcePostion = (!white && row == 1) || (white && row == 6)
            val direction = if (white) -1 else 1

            val front = row + direction to column
            if (front.getField(fields) == FieldsChess.EMPTY) {
                add(front)
                if (sourcePostion) {
                    val dFront = row + direction * 2 to column
                    if (dFront.getField(fields) == FieldsChess.EMPTY)
                        add(dFront)
                }
            }


            val left = row + direction to column + 1
            if (left.validPosition() && left.getField(fields) != FieldsChess.EMPTY && left.getField(fields).white != white)
                add(left)
            val right = row + direction to column - 1
            if (right.validPosition() && right.getField(fields) != FieldsChess.EMPTY && right.getField(fields).white != white)
                add(right)
        }
    }

    fun moveRook(white: Boolean, postion: Pair<Int, Int>, fields: Array<Array<FieldsChess>>): List<Pair<Int, Int>> {
        return moveStraight(postion, fields, white, false)
    }

    fun moveBishop(white: Boolean, postion: Pair<Int, Int>, fields: Array<Array<FieldsChess>>): List<Pair<Int, Int>> {
        return moveStraight(postion, fields, white, true)
    }

    fun moveKnight(white: Boolean, postion: Pair<Int, Int>, fields: Array<Array<FieldsChess>>): List<Pair<Int, Int>> {
        return buildList {
            val (x1, y1) = postion
            val distance = sqrt(5.0)
            buildList {
                (-2..2).forEach { x ->
                    (-2..2).forEach { y ->
                        add(postion.add(Pair(x, y)))
                    }
                }
            }.forEach {
                val (x2, y2) = it
                if (it.validPosition()) {
                    if (abs(sqrt(((x2.toFloat() - x1).pow(2) + (y2.toFloat() - y1).pow(2))) - distance) < 0.005) {
                        if (it.getField(fields) == FieldsChess.EMPTY) add(it)
                        else if (it.getField(fields).white != white) add(it)
                    }
                }
            }
        }
    }

    fun moveQueen(white: Boolean, postion: Pair<Int, Int>, fields: Array<Array<FieldsChess>>): List<Pair<Int, Int>> {
        return buildList {
            addAll(moveStraight(postion, fields, white, true))
            addAll(moveStraight(postion, fields, white, false))
        }
    }

    fun moveKing(white: Boolean, postion: Pair<Int, Int>, fields: Array<Array<FieldsChess>>, checkKing: Boolean): List<Pair<Int, Int>> {
        return buildList {
            (-1..1).forEach { x ->
                (-1..1).forEach { y ->
                    val p = postion.add(Pair(x, y))
                    if (p.validPosition()) {
                        val field = p.getField(fields)
                        if (field == FieldsChess.EMPTY) add(p)
                        else if (field.white != white) {
                            add(p)
                        }
                    }
                }
            }
            val moves = getAllMoves(!white, fields, checkKing)
            removeAll(moves)
        }
    }

    /*
    Check, if the King is in checkmate.
    State White - State Black
    State -> Danger - Checkmate
     */
    fun checkMate(white: Boolean, fields: Array<Array<FieldsChess>>): Pair<Boolean, Boolean> {
        var player = false to false

        var x = 0
        fields.forEach { row ->
            var y = 0
            row.forEach { field ->
                val pos = x to y
                if (field == FieldsChess.KING_WHITE || field == FieldsChess.KING_BLACK) {
                    if (white == field.white) {
                        println("\n\n\n\n\n> Is ${field.white} King")
                        val opponentMoves = getAllMoves(!field.white, fields, false)

                        // Check if King is in danger
                        if (opponentMoves.contains(pos)) {
                            println("> King is in Danger!!!")
                            val kingMoves = moveKing(field.white, pos, fields, false)
                            player = true to kingMoves.isEmpty()
                        }
                        println("> King Pos - $pos")
                        println(opponentMoves)
                    }
                }
                y++
            }
            x++
        }
        return player
    }

    /*
    Move Logic Extensions
     */
    private fun moveStraight(startPosition: Pair<Int, Int>, fields: Array<Array<FieldsChess>>, white: Boolean, diagonal: Boolean): List<Pair<Int, Int>> {
        return buildList {
            repeat(4) {
                val direction = if (diagonal) when (it) {
                    0 -> Pair(1, 1)
                    1 -> Pair(-1, 1)
                    2 -> Pair(1, -1)
                    3 -> Pair(-1, -1)
                    else -> return emptyList()
                } else when (it) {
                    0 -> Pair(1, 0)
                    1 -> Pair(-1, 0)
                    2 -> Pair(0, 1)
                    3 -> Pair(0, -1)
                    else -> return emptyList()
                }
                var multiplier = 1
                while (true) {
                    val p = startPosition.add(direction.multiply(multiplier))
                    if (!p.validPosition()) break
                    val field = p.getField(fields)
                    if (field == FieldsChess.EMPTY) {
                        add(p)
                        multiplier++
                    } else if (field.white == white) break
                    else {
                        add(p)
                        break
                    }
                }
            }
        }
    }

    private fun getAllMoves(white: Boolean, fields: Array<Array<FieldsChess>>, checkKing: Boolean): List<Pair<Int, Int>> {
        return buildList {
            var rowCounter = 0
            fields.forEach { row ->
                var columnCounter = 0
                row.forEach { field ->
                    if (field != FieldsChess.EMPTY) {
                        if (field.white == white) {
                            val p = Pair(rowCounter, columnCounter)
                            val moves = when (field) {
                                FieldsChess.PAWN_WHITE, FieldsChess.PAWN_BLACK -> movePawn(white, p, fields)
                                FieldsChess.KNIGHT_WHITE, FieldsChess.KNIGHT_BLACK -> moveKnight(white, p, fields)
                                FieldsChess.BISHOP_WHITE, FieldsChess.BISHOP_BLACK -> moveBishop(white, p, fields)
                                FieldsChess.ROOK_WHITE, FieldsChess.ROOK_BLACK -> moveRook(white, p, fields)
                                FieldsChess.QUEEN_WHITE, FieldsChess.QUEEN_BLACK -> if (checkKing) moveKing(white, p, fields, false)
                                else emptyList()
                                else -> emptyList()
                            }
                            addAll(moves)
                        }
                    }
                    columnCounter++
                }
                rowCounter++
            }
        }.distinct()
    }

    /*
    Utilities
     */
    private fun Pair<Int, Int>.validPosition(): Boolean {
        val (row, column) = this
        return row in 0..7 && column in 0..7
    }

    private fun Pair<Int, Int>.getField(fields: Array<Array<FieldsChess>>): FieldsChess {
        return fields[this.first][this.second]
    }

    private fun Pair<Int, Int>.add(p: Pair<Int, Int>): Pair<Int, Int> {
        return Pair(this.first + p.first, this.second + p.second)
    }

    private fun Pair<Int, Int>.multiply(multiplier: Int): Pair<Int, Int> {
        return Pair(this.first * multiplier, this.second * multiplier)
    }
}