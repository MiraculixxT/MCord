package de.miraculixx.mcord.config

import java.io.File

object ConfigManager {
    private val configs: Map<Configs, Config>

    fun getConfig(type: Configs): Config {
        return configs[type] ?: configs[Configs.CORE]!!
    }

    //static values
    val apiKey: String

    init {
        var jarPath = File(this.javaClass.protectionDomain.codeSource.location.toURI()).path
        val jarName = jarPath.substring(jarPath.lastIndexOf("/") + 1)
        jarPath = jarPath.removeSuffix(jarName)

        val s = File.separator
        val configFolder = File("$jarPath${s}config")
        println(configFolder.path)
        if (!configFolder.exists() || !configFolder.isDirectory) configFolder.mkdirs()

        configs = mapOf(
            Configs.CORE to Config("${configFolder.path}${s}core.yml"),
            Configs.SETTINGS to Config("${configFolder.path}${s}config.yml"),
        )


        //Applying values
        apiKey = getConfig(Configs.CORE).getString("API_TOKEN")
    }
}