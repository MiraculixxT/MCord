package de.miraculixx.mcord.config

import java.io.File

object ConfigManager {
    private val configs: Map<Configs, Config>

    fun getConfig(type: Configs): Config {
        return configs[type] ?: configs[Configs.CORE]!!
    }

    init {
        var jarPath = File(javaClass.protectionDomain.codeSource.location.toURI()).path
        val jarName = jarPath.substring(jarPath.lastIndexOf("/") + 1)
        jarPath = jarPath.removeSuffix(jarName)

        val s = File.separator
        val configFolder = File("$jarPath${s}config")
        println(configFolder.path)
        if (!configFolder.exists() || !configFolder.isDirectory) configFolder.mkdirs()
        File("$jarPath${s}config${s}lang").mkdirs()

        configs = mapOf(
            Configs.CORE to Config("${configFolder.path}${s}core.yml", "core.yml"),
            Configs.SETTINGS to Config("${configFolder.path}${s}config.yml", "config.yml"),
            Configs.GAME_SETTINGS to Config("${configFolder.path}${s}gamesettings.yml", "gamesettings.yml")
        )
    }
}