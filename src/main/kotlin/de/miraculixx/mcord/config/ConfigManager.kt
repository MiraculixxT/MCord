package de.miraculixx.mcord.config

import java.io.File

object ConfigManager {
    private val configs: Map<Configs, Config>

    fun getConfig(type: Configs): Config {
        return configs[type] ?: configs[Configs.CORE]!!
    }

    init {
        val cl = this::class.java

        val configFolder = File("config")
        if (!configFolder.exists() || !configFolder.isDirectory) configFolder.mkdirs()

        configs = mapOf(
            Configs.CORE to Config(cl.getResourceAsStream("/core.yml"), "core"),
            Configs.SETTINGS to Config(cl.getResourceAsStream("/config.yml"), "config"),
            Configs.GAME_SETTINGS to Config(cl.getResourceAsStream("/gamesettings.yml"), "gamesettings"),
        )
    }
}