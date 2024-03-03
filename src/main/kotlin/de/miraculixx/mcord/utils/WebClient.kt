package de.miraculixx.mcord.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*

object WebClient {
    val client = HttpClient(CIO) {

    }
}