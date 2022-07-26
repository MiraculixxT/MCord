package de.miraculixx.mcord.config

import de.miraculixx.mcord.utils.api.SQL
import java.io.File

private val languages = buildMap {
    var jarPath = File(javaClass.protectionDomain.codeSource.location.toURI()).path
    val jarName = jarPath.substring(jarPath.lastIndexOf("/") + 1)
    jarPath = jarPath.removeSuffix(jarName)
    val s = File.separator
    val langFolder = File("$jarPath${s}config${s}lang")
    langFolder.mkdirs()

    put("DE_DE", Config("${langFolder.path}${s}de_DE.yml", "lang/de_DE.yml"))
    put("EN_US", Config("${langFolder.path}${s}en_US.yml", "lang/en_US.yml"))
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