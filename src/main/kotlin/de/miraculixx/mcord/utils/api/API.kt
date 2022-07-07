package de.miraculixx.mcord.utils.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

private val client = HttpClient(CIO)

suspend fun callCustomAPI(url: String): String {
    val response: HttpResponse = client.get(url)
    return response.bodyAsText()
}