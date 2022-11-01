package de.miraculixx.mcord.modules.trivia

import de.miraculixx.mcord.utils.api.callCustomAPI
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import de.miraculixx.mcord.utils.extensions.enumOf
import de.miraculixx.mcord.utils.serializer.json
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import org.apache.commons.text.StringEscapeUtils

class TriviaCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val difficulty = it.getOption("difficulty")?.asString ?: "RANDOM"
        val diffEnum = enumOf(difficulty) ?: TriviaDifficulty.RANDOM
        val category = it.getOption("category")?.asString ?: "RANDOM"
        val catEnum = enumOf(category) ?: TriviaCategory.RANDOM
        val user = it.user
        val userID = user.id

        it.deferReply().queue()
        val gen = generateQuestion(catEnum, diffEnum, userID)

        it.hook.editOriginalEmbeds(listOf(
            gen.first
        )).setComponents(
            listOf(
                gen.second
            )
        ).queue()
    }

    @Serializable
    data class TriviaOutput(val results: List<TriviaQuestion>)

    @Serializable
    data class TriviaQuestion(
        val category: String,
        val type: String,
        val difficulty: String,
        val question: String,
        val correct_answer: String,
        val incorrect_answers: List<String>
    )
}

suspend fun generateQuestion(category: TriviaCategory, difficulty: TriviaDifficulty, userID: String): Pair<MessageEmbed, ActionRow> {
    val url = buildString {
        append("https://opentdb.com/api.php?amount=1")
        if (category != TriviaCategory.RANDOM) append("&category=${category.id}")
        if (difficulty != TriviaDifficulty.RANDOM) append("&difficulty=${difficulty.name.lowercase()}")
    }
    val response = callCustomAPI(url)
    val trivia = json.decodeFromString<TriviaCommand.TriviaOutput>(response).results.first()

    return Embed {
        val question = StringEscapeUtils.unescapeHtml4(trivia.question)
        val diff = enumOf<TriviaDifficulty>(trivia.difficulty.uppercase())
        val cat = TriviaCategory.getByTitle(trivia.category.replace(":", " -"))
        title = "\uD83E\uDDE9  || **Trivia Quiz**"
        description = "> Difficulty -> ``${diff?.title}``\n" +
                "> Category -> ``${cat.title}``\n" +
                "\n" +
                "```fix\n" +
                "${question}```"
        color = 0xc29011
    } to ActionRow.of(
        buildList {
            println(trivia.correct_answer)
            if (trivia.type == "multiple") {
                add(button("TRIVIA:$userID:1", trivia.correct_answer))
                add(button("TRIVIA:$userID:2", trivia.incorrect_answers[0]))
                add(button("TRIVIA:$userID:3", trivia.incorrect_answers[1]))
                add(button("TRIVIA:$userID:4", trivia.incorrect_answers[2]))
            } else {
                add(button("TRIVIA:$userID:1", "True", Emoji.fromFormatted("<:yes:998195646467145751>")))
                add(button("TRIVIA:$userID:2", "False", Emoji.fromFormatted("<:no:998195603324551323>")))
            }
        }.shuffled()
    )
}