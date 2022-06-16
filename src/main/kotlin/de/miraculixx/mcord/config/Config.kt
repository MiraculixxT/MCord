package de.miraculixx.mcord.config

import dev.schlaubi.envconf.Config as EnvironmentConfig

object Config : EnvironmentConfig() {

    val DISCORD_TOKEN by environment
    val API_KEY by environment
}