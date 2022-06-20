@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.miraculixx.mcord.config

import de.miraculixx.mcord.utils.log
import org.yaml.snakeyaml.Yaml
import java.io.File

class Config(path: String) {
    private val yaml: Yaml = Yaml()
    private val configMap: Map<String, Any>
    private val name: String


    fun getString(name: String): String {
        return configMap[name].toString()
    }

    fun getInt(name: String): Int {
        return getString(name).toIntOrNull() ?: 0
    }

    fun getLong(name: String): Long {
        return getString(name).toLongOrNull() ?: 0
    }

    fun getBoolean(name: String): Boolean {
        return getString(name).lowercase() == "true"
    }


    private fun loadConfig(file: File) {
        ">> Create new Config File - $name".log()
        val classLoader = this.javaClass.classLoader
        if (!file.exists()) {
            file.createNewFile()
            val stream = classLoader.getResourceAsStream(name)
            file.writeBytes(stream.readAllBytes())
        }
    }

    init {
        name = path.substring(path.lastIndexOf('/') + 1)
        ">> Load Config - $name".log()
        val file = File(path)
        if (!file.exists()) loadConfig(file)

        configMap = try {
            yaml.load(File(path).inputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            "ERROR - Failed to load Configuration File. ^^ Reason above ^^".log()
            "ERROR - Config Path -> $path".log()
            emptyMap()
        }
    }
}