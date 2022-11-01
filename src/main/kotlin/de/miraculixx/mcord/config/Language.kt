package de.miraculixx.mcord.config

import de.miraculixx.mcord.utils.api.SQL
import java.io.File

private val languages = buildMap {
    val cl = this::class.java

    val configFolder = File("config/lang")
    if (!configFolder.exists() || !configFolder.isDirectory) configFolder.mkdirs()

    put("DE_DE", Config(cl.getResourceAsStream("/de_DE.yml"), "de_DE"))
    put("EN_US", Config(cl.getResourceAsStream("/en_US.yml"), "en_US"))
}

val guildCache = HashMap<Long, String>()

suspend fun msg(key: String?, guildSnowflake: Long): String {
    val guildLang = if (guildCache[guildSnowflake] == null) {
        val result = SQL.call("SELECT Lang FROM guildData WHERE Discord_ID=$guildSnowflake")
        result.next()
        result.getString("Lang")
    } else guildCache[guildSnowflake]
    val lang = languages[guildLang]

    return lang?.getString(key ?: "error") ?: "*$key*"
}

fun msgDiff(error: String): String {
    return "```diff\n$error```"
}