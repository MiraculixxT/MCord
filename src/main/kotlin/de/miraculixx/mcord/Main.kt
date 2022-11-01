package de.miraculixx.mcord

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.games.UpdaterGame
import de.miraculixx.mcord.modules.utils.events.TabComplete
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.log
import de.miraculixx.mcord.utils.manager.ButtonManager
import de.miraculixx.mcord.utils.manager.DropDownManager
import de.miraculixx.mcord.utils.manager.ModalManager
import de.miraculixx.mcord.utils.manager.SlashCommandManager
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.*

fun main() {
    Main()
}

class Main {

    companion object {
        lateinit var INSTANCE: Main
        lateinit var KTOR: HttpClient
    }

    private val updater: Job?
    val jda: JDA

    init {
        INSTANCE = this
        KTOR = HttpClient(CIO)

        val coreConf = ConfigManager.getConfig(Configs.CORE)
        val settingsConf = ConfigManager.getConfig(Configs.SETTINGS)

        jda = default(coreConf.getString("DISCORD_TOKEN")) {
            disableCache(CacheFlag.VOICE_STATE)
            setActivity(Activity.competing("Chess Games"))
            setStatus(OnlineStatus.DO_NOT_DISTURB)
            intents += listOf(GatewayIntent.GUILD_MEMBERS)
        }
        jda.awaitReady()

        ButtonManager.startListen(jda)
        DropDownManager.startListen(jda)
        ModalManager.startListen(jda)
        SlashCommandManager.startListen(jda)
        TabComplete.startListen(jda)

        updater = if (settingsConf.getBoolean("Updater"))
            UpdaterGame.start(jda) else null
        //SQL
        "MKord is now online!".log()

        shutdown()
    }

    private fun shutdown() {
        runBlocking {
            var online = true
            while (online) {
                val scanner = Scanner(System.`in`)
                when (val out = scanner.nextLine()) {
                    "exit" -> {
                        KTOR.close()
                        updater?.cancel()
                        GameManager.shutdown()
                        jda.shardManager?.setStatus(OnlineStatus.OFFLINE)
                        jda.shutdown()
                        println("MKord is now offline!")
                        online = false
                    }

                    else -> {
                        println("Command $out not found!\nCurrent Commands -> 'exit'")
                    }
                }
            }
        }
    }
}