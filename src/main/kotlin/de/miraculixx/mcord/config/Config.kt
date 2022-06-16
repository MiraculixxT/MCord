package de.miraculixx.mcord.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader

object Config {
    @kotlinx.serialization.Serializable
    data class ConfigData(val API_KEY: String, val DISCORD_TOKEN: String)

    val apiKey: String
    val botToken: String

    init {
        val inputStream = javaClass.classLoader.getResourceAsStream("config.json")
        val fileContent = inputStream.bufferedReader().use(BufferedReader::readText)
        val configData = Json.decodeFromString<ConfigData>(fileContent)
        apiKey = configData.API_KEY
        botToken = configData.DISCORD_TOKEN
    }
}