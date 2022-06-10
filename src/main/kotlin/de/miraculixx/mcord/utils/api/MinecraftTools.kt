package de.miraculixx.mcord.utils.api

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MinecraftTools {
    @kotlinx.serialization.Serializable
    data class User(
        val uuid: String,
        val username: String,
        val creation_date: String?
    )

    @kotlinx.serialization.Serializable
    data class NameHistory(
        val name: String,
        val changedToAt: Long
    )

    suspend fun getUserData(uuid: String): User {
        val json = callAPI(API.MINECRAFT_TOOLS, uuid)
        val final = json.split("\"username_history\":")[0]
            .plus("\"creation_date\"")
            .plus(json.split("\"created_at\"")[1])
        println(final)
        return Json.decodeFromString(final)
    }

    suspend fun nameHistory(uuid: String): List<NameHistory> {
        val json = callAPI(API.MINECRAFT, "$uuid/names")
        if (json.isEmpty()) return listOf(NameHistory("Invalid UUID", 0L))
        println(json)
        val final = if (json.length <= 30) {
            "[{\"name\":\"".plus(json.split('"')[3])
                .plus("\",\"changedToAt\":0}]")
        } else {
            "[{\"name\":\"".plus(json.split('"')[3])
                .plus("\",\"changedToAt\":0},")
                .plus(json.split("\"},")[1])
        }

        println(final)
        return Json.decodeFromString(final)
    }
}