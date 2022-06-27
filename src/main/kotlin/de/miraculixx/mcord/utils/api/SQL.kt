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

    private fun call(statement: String): ResultSet {
        if (connection.isClosed) connection = connect()
        val query = connection.prepareStatement(statement)
        return query.executeQuery()
    }

    fun getUserEmote(id: Long): UserEmote? {
        val result = call("SELECT * FROM fourWinsEmotes WHERE ID=$id")
        return if (result.next())
            UserEmote(result.getLong("ID"),
            result.getString("Emote"),
            result.getString("EmoteSec"))
        else null
    }

    fun setUserEmote(userEmote: UserEmote) {
        call("INSERT INTO fourWinsEmotes VALUES (${userEmote.id}, '${userEmote.emote}', '${userEmote.emote2}')" +
                "ON DUPLICATE KEY UPDATE Emote='${userEmote.emote}', Emote2='${userEmote.emote2}'")
    }


    // Models
    data class UserEmote(val id: Long, val emote: String, val emote2: String)

    init {
        connection = connect()
    }
}