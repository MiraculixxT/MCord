package de.miraculixx.mcord.modules.trivia

enum class TriviaCategory(val id: Int, val title: String) {
    RANDOM(0, "Random Category"),
    GENERAL_KNOWLEDGE(9, "General Knowledge"),
    BOOKS(10, "Entertainment - Books"),
    FILMS(11, "Entertainment - Film"),
    MUSIC(12,"Entertainment - Music"),
    THEATRE(13, "Entertainment - Musicals & Theatres"),
    TELEVISION(14, "Entertainment - Television"),
    VIDEO_GAMES(15, "Entertainment - Video Games"),
    BOARD_GAMES(16, "Entertainment - Board Games"),
    ANIME_AND_MANGA(31, "Entertainment - Japanese Anime & Manga"),
    CARTOONS_AND_ANIMATION(32, "Entertainment - Cartoon & Animations"),
    COMICS(29, "Entertainment - Comics"),
    NATURE(17, "Science - Nature"),
    COMPUTERS(18, "Science - Computers"),
    MATHEMATICS(19, "Science - Mathematics"),
    GADGETS(30, "Science - Gadgets"),
    ANIMALS(27, "Animals"),
    MYTHOLOGY(20, "Mythology"),
    SPORTS(21, "Sports"),
    GEOGRAPHY(22, "Geography"),
    HISTORY(23, "History"),
    POLITICS(24, "Politics"),
    ART(25, "Art"),
    CELEBRITIES(26, "Celebrities"),
    VEHICLES(28, "Vehicles");

    companion object {
        fun getByTitle(title: String): TriviaCategory {
            return values().firstOrNull { it.title == title } ?: RANDOM
        }
    }
}