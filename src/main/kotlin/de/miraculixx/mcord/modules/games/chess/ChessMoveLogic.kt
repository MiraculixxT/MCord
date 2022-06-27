package de.miraculixx.mcord.modules.games.chess

// Pair<Row, Column>
object ChessMoveLogic {

    fun movePawn(white: Boolean, postion: Pair<Int, Int>, fields: Array<Array<FieldsChess>>): List<Pair<Int, Int>> {
        return buildList {
            val (row, column) = postion
            val sourcePostion = (!white && row == 1) || (white && row == 6)
            val direction = if (white) -1 else 1

            val front = row + direction to column
            if (getField(front, fields) == FieldsChess.EMPTY) {
                println("+ add front")
                add(front)
                if (sourcePostion) {
                    val dFront = row + direction * 2 to column
                    if (getField(dFront, fields) == FieldsChess.EMPTY)
                        println("+ add first move")
                        add(dFront)
                }
            }


            val left = row + direction to column + 1
            if (validPosition(left) && getField(left, fields) != FieldsChess.EMPTY && getField(left, fields).white != white)
                println("+ add schlag +1")
                add(left)
            val right = row + direction to column - 1
            if (validPosition(right) && getField(right, fields) != FieldsChess.EMPTY && getField(right, fields).white != white)
                println("+ add schlag -1")
                add(right)
        }
    }


    private fun validPosition(postion: Pair<Int, Int>): Boolean {
        val (row, column) = postion
        println("--- Position $row $column ist ${row in 0..7 && column in 0..7}")
        return row in 0..7 && column in 0..7
    }
    private fun getField(postion: Pair<Int, Int>, fields: Array<Array<FieldsChess>>): FieldsChess {
        println("--- Field ${postion.first} ${postion.second} ist ${fields[postion.first][postion.second].name}")
        return fields[postion.first][postion.second]
    }
}