package de.miraculixx.mcord.utils.api

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.utils.log
import java.sql.DriverManager
import java.sql.ResultSet

object SQL {
    private val connection = DriverManager.getConnection(
        "jdbc:mariadb://localhost:3306/mcord",
            "mcord",
        ConfigManager.getConfig(Configs.CORE).getString("SQL_TOKEN")
    )

    private fun call(statement: String): ResultSet {
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


    // Models
    data class UserEmote(val id: Long, val emote: String, val emote2: String)

    init {
        if (connection.isValid(0))
            ">> Connection established to MariaDB".log()
        else ">> ERROR > MariaDB refused the connection".log()
    }
}