package de.miraculixx.mcord.modules.games.utils

enum class FieldsTwoPlayer {
    EMPTY,
    PLAYER_1,
    PLAYER_2;
}

fun FieldsTwoPlayer.getOpponent(): FieldsTwoPlayer {
    return when (this) {
        FieldsTwoPlayer.EMPTY -> FieldsTwoPlayer.EMPTY
        FieldsTwoPlayer.PLAYER_1 -> FieldsTwoPlayer.PLAYER_2
        FieldsTwoPlayer.PLAYER_2 -> FieldsTwoPlayer.PLAYER_1
    }
}