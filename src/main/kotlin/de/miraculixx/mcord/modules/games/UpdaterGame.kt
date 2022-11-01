package de.miraculixx.mcord.modules.games

import de.miraculixx.mcord.modules.games.utils.enums.DailyGoals
import de.miraculixx.mcord.utils.Color
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.dailyGoals
import de.miraculixx.mcord.utils.log
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.sql.ResultSet
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


object UpdaterGame {
    private var JDA: JDA? = null

    fun start(jda: JDA): Job {
        JDA = jda
        return CoroutineScope(Dispatchers.Default).launch {
            delay(10.seconds) // Let the system slowly starts
            launch {
                val result = SQL.call("SELECT * FROM globalDaily")
                result.next()
                dailyGoals = buildList {
                    (1..3).forEach {
                        add(DailyGoals.valueOf(result.getString("Task_$it")))
                    }
                    add(DailyGoals.valueOf(result.getString("Task_Bonus")))
                }
                while (true) {
                    val current = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    if (current.hour == 1) {
                        updateDailyChallenges()
                        GameManager.shutdown()
                        delay(23.hours)
                    }
                    delay(1.minutes)
                }
            }
            launch {
                while (true) {
                    updateLeaderboards()
                    delay(1.hours)
                }
            }
        }
    }

    suspend fun updateDailyChallenges() {
        "---=---> DAILY UPDATE <---=---".log(Color.YELLOW)
        val list = DailyGoals.values().filter { !it.bonus }.toMutableList()
        val bonus = DailyGoals.values().filter { it.bonus }

        dailyGoals = buildList {
            repeat(3) {
                val newGoal = list.random()
                add(newGoal)
                list.remove(newGoal)
            }
            add(bonus.random())
        }

        SQL.updateDailyChallenges(dailyGoals!!.map { it.name })
    }

    private suspend fun updateLeaderboards() = runBlocking {
        "---=---> STATS UPDATE <---=---".log(Color.YELLOW)
        val call = SQL.call("SELECT Stats_Channel, Discord_ID FROM guildData WHERE Premium=1 && Stats_Channel!=0")

        var counter = 0
        while (call.next()) {
            counter++
            val guildID = call.getLong("Discord_ID")
            val statsChannelID = call.getLong("Stats_Channel")
            launch {
                val guild = JDA!!.getGuildById(guildID) ?: return@launch
                val channel = guild.getTextChannelById(statsChannelID)
                updateLeaderboardGuild(guild, channel)
            }
        }

        "Finished checking $counter Guilds".log(Color.YELLOW)
        "---=---=---=---=---=---=---=---".log(Color.YELLOW)
    }

    suspend fun updateLeaderboardGuild(guild: Guild, statsChannel: MessageChannel?) {
        val guildID = guild.idLong
        if (statsChannel == null) {
            SQL.call("UPDATE guildData WHERE Discord_ID=$guildID SET Stats_Channel=0")
            " - GUILD REMOVE > $guildID deleted their stats channel".log(Color.YELLOW)
            return
        }
        val resp = SQL.call("SELECT Discord_ID, Coins FROM userData WHERE Guild_ID=$guildID ORDER BY Coins DESC LIMIT 10")

        //Creating Embeds
        try {
            val embed = listOf(
                Embed {
                    color = 0xc29113
                    title = "\uD83D\uDC51  || LEADERBOARD"
                    description = "Updates <t:${Clock.System.now().plus(1.hours).epochSeconds}:R>\n```fix\nWer ist der beste Zocker hier?```"
                    field {
                        name = "Coins :coin:"
                        value = buildField(resp, false, "Coins")
                    }
                    field {
                        name = "Top 10"
                        value = buildField(resp, true, "Coins")
                    }
                },
                Embed {
                    color = 0xc29113
                    field {
                        val resp2 = SQL.call("SELECT Discord_ID, TTT, TTT_Bot FROM userWins, userData WHERE Guild_ID=$guildID && userData.ID=userWins.ID ORDER BY TTT DESC LIMIT 5")
                        name = "TTT Wins"
                        value = buildField(resp2, false, "TTT", "TTT_Bot")
                    }
                    field {
                        val resp2 = SQL.call("SELECT Discord_ID, C4, C4_Bot FROM userWins, userData WHERE Guild_ID=$guildID && userData.ID=userWins.ID ORDER BY C4 DESC LIMIT 5")
                        name = "C4 Wins"
                        value = buildField(resp2, true, "C4", "C4_Bot")
                    }
                    field {
                        val resp2 = SQL.call("SELECT Discord_ID, Chess, Chess_Bot FROM userWins, userData WHERE Guild_ID=$guildID && userData.ID=userWins.ID ORDER BY Chess DESC LIMIT 5")
                        name = "Chess Wins"
                        value = buildField(resp2, true, "Chess", "Chess_Bot")
                    }
                }
            )

            //Sending information to Discord
            statsChannel.getHistoryFromBeginning(10).queue { history: MessageHistory ->
                if (history.isEmpty || history.retrievedHistory[0].author.id != JDA!!.selfUser.id)
                    statsChannel.send(embeds = embed).queue()
                else history.retrievedHistory[0].edit(embeds = embed).queue()
            }
        } catch (e: InsufficientPermissionException) {
            " - NO PERMISSION > Guild $guildID".log(Color.YELLOW)
        }
    }

    private fun buildField(response: ResultSet, inline: Boolean, key: String, key2: String? = null): String {
        return buildString {
            val m = if (inline) "> " else ""
            repeat(5) {
                if (response.next()) {
                    val addon = if (key2 != null) " | ${response.getString(key2)}" else ""
                    append("$m<@${response.getString("Discord_ID")}> - ${response.getString(key)}$addon\n")
                } else append("$m*Empty*\n")
            }
        }
    }
}
