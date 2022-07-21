package de.miraculixx.mcord.modules.games.utils.enums

enum class DailyChallenge(val reward: Int, val bonus: Boolean) {
    WIN_C4_BOT(50, false),
    WIN_C4_USER(42, false),
    WIN_TTT_BOT(29, false),
    WIN_TTT_USER(37, false),
    WIN_CHESS_USER(47, false),
    TIMEOUT_CHESS(32, false),
    REPLAY(35, false),

    //Bonus
    CHANGE_C4_SKIN(20, true),
    DRAW_C4(32, true),
    DRAW_TTT(21, true),
}