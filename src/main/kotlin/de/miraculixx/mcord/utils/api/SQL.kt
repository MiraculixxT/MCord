package de.miraculixx.mcord.utils.api

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.utils.log
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
            ">> Connection established to MariaDB".log()
        else ">> ERROR > MariaDB refused the connection".log()
        return con
    }

    fun call(statement: String): ResultSet {
        if (connection.isClosed) connection = connect()
        val query = connection.prepareStatement(statement)
        return query.executeQuery()
    }

    /*
    Interactions to the API
     */
    fun createUser(id: Long): UserData {
        // Generell User Account
        call("INSERT INTO userData VALUES ($id, 0)")

        // Create Empty Data Rows to simplify future calls
        call("INSERT INTO userEmotesActive VALUES ($id, '\uD83D\uDD34', '\uD83D\uDFE1')")
        call("INSERT INTO userWins VALUES ($id, 0, 0, 0, 0, 0, 0)")
        return UserData(
            id, 0,
            UserEmote(emptyMap(),"\uD83D\uDD34", "\uD83D\uDFE1"),
            UserWins(0, 0, 0, 0, 0, 0)
        )
    }

    fun getUser(id: Long, emotes: Boolean, wins: Boolean): UserData {
        val result = call("SELECT * FROM userData WHERE ID=$id")
        if (!result.next()) return createUser(id)
        return UserData(
            id,
            result.getInt("Coins"),
            if (emotes) {
                val allEmotes = call("SELECT * FROM userEmotes WHERE ID=$id")
                val activeEmotes = call("SELECT * FROM userEmotesActive WHERE ID=$id")
                activeEmotes.next()
                val emoteMap = buildMap {
                    while (allEmotes.next()) {
                        put(allEmotes.getString("Emote_ID"),
                            allEmotes.getString("Emote"))
                    }
                }

                UserEmote(
                    emoteMap,
                    activeEmotes.getString("C4_Primary"),
                    activeEmotes.getString("C4_Secondary")
                )
            } else null,
            if (wins) {
                val winData = call("SELECT * FROM userWins WHERE ID=$id")
                winData.next()
                UserWins(
                    winData.getInt("TTT"),
                    winData.getInt("TTT_Bot"),
                    winData.getInt("C4"),
                    winData.getInt("C4_Bot"),
                    winData.getInt("Chess"),
                    winData.getInt("Chess_Bot"),
                )
            } else null
        )
    }

    fun setUserCoins(id: Long, amount: Int): Boolean {
        return call("UPDATE userData SET Coins=$amount WHERE ID=$id").rowUpdated()
    }

    fun addEmote(id: Long, type: String, emote: String) {
        call("INSERT INTO userEmotes VALUES ($id, '$type', '$emote')")
    }
    fun setActiveEmote(id: Long, type: String, newEmote: String): Boolean {
        return call("UPDATE userEmotesActive SET $type='$newEmote' WHERE ID=$id").rowUpdated()
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

    /**
     * @param id Discord User ID
     * @param coins Amount of Coins
     * @param emotes All Emote Information
     */
    data class UserData(val id: Long, val coins: Int, val emotes: UserEmote?, val wins: UserWins?)

    init {
        connection = connect()
    }
}