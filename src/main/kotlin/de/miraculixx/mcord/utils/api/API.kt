package de.miraculixx.mcord.utils.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.net.URL

private val client = HttpClient(CIO)

suspend fun callAPI(api: API, action: String): String {
    val target = "https://" + when (api) {
        API.MUTILS -> "mutils.de/m/apiv2/"
        API.MINECRAFT_TOOLS -> "api.ashcon.app/mojang/v2/user/"
        API.MINECRAFT -> "api.mojang.com/users/profiles/"
    } + action
    val url = URL(target)

    val response: HttpResponse = client.get(url)
    return response.bodyAsText()
}

suspend fun callCustomAPI(url: String): String {
    val response: HttpResponse = client.get(url)
    return response.bodyAsText()
}

enum class API {
    MUTILS,
    MINECRAFT,
    MINECRAFT_TOOLS,
}