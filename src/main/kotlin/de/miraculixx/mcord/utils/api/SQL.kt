package de.miraculixx.mcord.utils.api

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.utils.Color
import de.miraculixx.mcord.utils.error
import de.miraculixx.mcord.utils.log
import kotlinx.coroutines.delay
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object SQL {
    private var connection: Connection

    private fun connect(): Connection {
        val con = DriverManager.getConnection(
            "jdbc:mariadb://localhost:3306/mcord",
            "mcord",
            ConfigManager.getConfig(Configs.CORE).getString("SQL_TOKEN")
        )
        if (con.isValid(0))
            ">> Connection established to MariaDB".log(Color.GREEN)
        else ">> ERROR > MariaDB refused the connection".error()
        return con
    }

    suspend fun call(statement: String, resultSet: Int? = null): ResultSet {
        while (!connection.isValid(1)) {
            "ERROR >> SQL - No valid connection!".error()
            connection = connect()
            delay(1000)
        }

        val query = if (resultSet != null) connection.prepareStatement(statement, resultSet)
        else connection.prepareStatement(statement)
        return query.executeQuery()
    }

    /*
    Interactions to the API
     */
    private suspend fun createUser(userSnowflake: Long, guildSnowflake: Long): UserData {
        // Generell User Account
        call("INSERT INTO userData VALUES (default, $guildSnowflake, $userSnowflake, 0)")
        val userData = call("SELECT * FROM userData WHERE Guild_ID=$guildSnowflake && Discord_ID=$userSnowflake")
        userData.next()
        val userID = userData.getInt("ID")

        // Create Empty Data Rows to simplify future calls
        call("INSERT INTO userEmotesActive VALUES ($userID, '\uD83D\uDD34', '\uD83D\uDFE1')")
        call("INSERT INTO userWins VALUES ($userID, 0, 0, 0, 0, 0, 0)")
        call("INSERT INTO userDaily VALUES ($userID, false, false, false, false)")
        return UserData(
            userSnowflake, 0,
            UserEmote(emptyMap(), "\uD83D\uDD34", "\uD83D\uDFE1"),
            UserWins(0, 0, 0, 0, 0, 0),
            UserDailyChallenges(false, false, false, false)
        )
    }

    private suspend fun createGuild(guildSnowflake: Long): GuildData {
        call("INSERT INTO guildData VALUES (default, $guildSnowflake, false, 0, 'EN_US')")
        return GuildData(guildSnowflake, false, 0)
    }

    private suspend fun getUserID(userSnowflake: Long, guildSnowflake: Long): Int {
        val id = call("SELECT ID FROM userData WHERE Discord_ID=$userSnowflake && Guild_ID=$guildSnowflake")
        return if (id.next()) id.getInt("ID")
        else 0
    }

    suspend fun getUser(userSnowflake: Long, guildSnowflake: Long, emotes: Boolean = false, wins: Boolean = false, daily: Boolean = false): UserData {
        val result = call("SELECT * FROM userData WHERE Guild_ID=$guildSnowflake && Discord_ID=$userSnowflake")
        if (!result.next()) return createUser(userSnowflake, guildSnowflake)
        return UserData(
            userSnowflake,
            result.getInt("Coins"),
            if (emotes) {
                val allEmotes = call("SELECT Emote_Type, Emote FROM userEmotes, userData WHERE Guild_ID=$guildSnowflake && Discord_ID=$userSnowflake && userEmotes.ID=userData.ID")
                val activeEmotes = call("SELECT * FROM userEmotesActive, userData WHERE Guild_ID=$guildSnowflake && Discord_ID=$userSnowflake && userEmotesActive.ID=userData.ID")
                activeEmotes.next()
                val emoteMap = buildMap {
                    while (allEmotes.next()) {
                        try {
                            put(
                                allEmotes.getString("Emote_Type"),
                                allEmotes.getString("Emote")
                            )
                        } catch (e: Exception) {
                            put("1", "2")
                        }
                    }
                }
                UserEmote(
                    emoteMap,
                    activeEmotes.getString("C4_P"),
                    activeEmotes.getString("C4_S")
                )
            } else null,
            if (wins) {
                val winData = call("SELECT * FROM userWins, userData WHERE Guild_ID=$guildSnowflake && Discord_ID=$userSnowflake && userWins.ID=userData.ID")
                winData.next()
                UserWins(
                    winData.getInt("TTT"),
                    winData.getInt("TTT_Bot"),
                    winData.getInt("C4"),
                    winData.getInt("C4_Bot"),
                    winData.getInt("Chess"),
                    winData.getInt("Chess_Bot"),
                )
            } else null,
            if (daily) {
                val dailyData = call("SELECT * FROM userDaily, userData WHERE Guild_ID=$guildSnowflake && Discord_ID=$userSnowflake && userDaily.ID=userData.ID")
                dailyData.next()
                UserDailyChallenges(
                    dailyData.getBoolean("Task_1"),
                    dailyData.getBoolean("Task_2"),
                    dailyData.getBoolean("Task_3"),
                    dailyData.getBoolean("Task_Bonus")
                )
            } else null
        )
    }

    suspend fun getGuild(guildSnowflake: Long): GuildData {
        val result = call("SELECT * FROM guildData WHERE Discord_ID=$guildSnowflake")
        if (!result.next()) return createGuild(guildSnowflake)
        return GuildData(
            guildSnowflake,
            result.getBoolean("Premium"),
            result.getLong("Stats_Channel")
        )
    }

    suspend fun setUserCoins(userSnowflake: Long, guildSnowflake: Long, amount: Int) {
        call("UPDATE userData SET Coins=$amount WHERE Discord_ID=$userSnowflake && Guild_ID=$guildSnowflake")
    }

    suspend fun addEmote(userSnowflake: Long, guildSnowflake: Long, type: String, emote: String) {
        val id = getUserID(userSnowflake, guildSnowflake)
        call("INSERT INTO userEmotes VALUES ($id, '$type', '$emote')")
    }

    suspend fun setActiveEmote(userSnowflake: Long, guildSnowflake: Long, type: String, newEmote: String) {
        val id = getUserID(userSnowflake, guildSnowflake)
        call("UPDATE userEmotesActive SET $type='$newEmote' WHERE ID=$id")
    }

    suspend fun updateDailyChallenges(challenges: List<String>) {
        if (challenges.size != 4) {
            "ERROR > Invalid Daily Challenges Update".error()
            return
        }
        call("UPDATE globalDaily SET Task_1='${challenges[0]}', Task_2='${challenges[1]}', Task_3='${challenges[2]}', Task_Bonus='${challenges[3]}'")
    }

    suspend fun addWin(userSnowflake: Long, guildSnowflake: Long, type: String) {
        var id = getUserID(userSnowflake, guildSnowflake)
        if (id == 0) {
            createUser(userSnowflake, guildSnowflake)
            id = getUserID(userSnowflake, guildSnowflake)
        }
        call("UPDATE userWins SET $type=$type+1 WHERE ID=$id")
    }


    /**
     * @param owned All bought Emotes -> Emote_ID - Emote
     * @param c4 Connect 4 Primary Emote
     * @param c42 Connect 4 Secondary Emote
     */
    data class UserEmote(val owned: Map<String, String>, val c4: String, val c42: String)

    /**
     * @param ttt Wins in TicTacToe - User
     * @param tttBot Wins in TicTacToe - Bot
     * @param c4 Wins in Connect 4 - User
     * @param c4Bot Wins in Connect 4 - Bot
     * @param chess Wins in Chess - User
     * @param chessBot Wins in Chess - Bot
     */
    data class UserWins(val ttt: Int, val tttBot: Int, val c4: Int, val c4Bot: Int, val chess: Int, val chessBot: Int)

    data class UserDailyChallenges(val c1: Boolean, val c2: Boolean, val c3: Boolean, val bonus: Boolean)

    /**
     * @param id Discord User ID
     * @param coins Amount of Coins
     * @param emotes All Emote Information
     * @param wins All Wins Information
     */
    data class UserData(val id: Long, val coins: Int, val emotes: UserEmote?, val wins: UserWins?, val daily: UserDailyChallenges?)

    /**
     * @param id Discord Guild ID
     * @param premium Does this Guild own Premium?
     * @param statsChannel Discord Channel ID (Statistics Channel)
     */
    data class GuildData(val id: Long, val premium: Boolean, val statsChannel: Long)

    init {
        connection = connect()
    }
}