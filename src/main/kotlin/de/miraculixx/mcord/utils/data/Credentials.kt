package de.miraculixx.mcord.utils.data

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val botToken: String = "<token>")
