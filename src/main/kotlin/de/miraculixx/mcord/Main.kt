package de.miraculixx.mcord

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.modules.games.GameManager
import de.miraculixx.mcord.modules.mutils.EventRoleReceive
import de.miraculixx.mcord.modules.utils.Updater
import de.miraculixx.mcord.modules.utils.events.MessageReactor
import de.miraculixx.mcord.modules.utils.events.TabComplete
import de.miraculixx.mcord.utils.log
import de.miraculixx.mcord.utils.manager.ButtonManager
import de.miraculixx.mcord.utils.manager.DropDownManager
import de.miraculixx.mcord.utils.manager.ModalManager
import de.miraculixx.mcord.utils.manager.SlashCommandManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag

fun main() {
    Main()
}

class Main {

    companion object {
        lateinit var INSTANCE: Main
        lateinit var KTOR: HttpClient
    }

    private val updater: Job?
    var jda: JDA? = null

    init {
        INSTANCE = this
        KTOR = HttpClient(CIO)

        val coreConf = ConfigManager.getConfig(Configs.CORE)
        val settingsConf = ConfigManager.getConfig(Configs.SETTINGS)
        val builder = JDABuilder.createDefault(coreConf.getString("DISCORD_TOKEN"))
        builder.disableCache(CacheFlag.VOICE_STATE)
        builder.setActivity(Activity.listening("Miraculixx's complains"))
        builder.setStatus(OnlineStatus.IDLE)
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
        builder.setMemberCachePolicy(MemberCachePolicy.ALL)
        val lateInits = listOf(SlashCommandManager())
        builder.addEventListeners(ButtonManager(), TabComplete(), DropDownManager(), ModalManager(), MessageReactor(), EventRoleReceive())
        lateInits.forEach { builder.addEventListeners(it) }

        builder.setChunkingFilter(ChunkingFilter.include(908621996009619477))

        jda = builder.build()
        jda!!.awaitReady()

        //LateInit Setup
        lateInits.forEach {
            it.setup()
        }

        updater = if (settingsConf.getBoolean("Updater"))
             Updater.start(jda!!) else null
        "MKord is now online!".log()

        shutdown()
    }

    private fun shutdown() {
        runBlocking {
            val reader = BufferedReader(InputStreamReader(System.`in`))
            var line = withContext(Dispatchers.IO) {
                reader.readLine()
            }?.lowercase()
            while (line != null) {
                when (line) {
                    "exit" -> {
                        KTOR.close()
                        updater?.cancel()
                        GameManager.shutdown()
                        jda?.shardManager?.setStatus(OnlineStatus.OFFLINE)
                        jda?.shutdown()
                        println("MKord is now offline!")
                        return@runBlocking
                    }
                    else -> {
                        println("Command $line not found!\nCurrent Commands -> 'exit'")
                        line = null
                    }
                }
            }
        }
    }
}