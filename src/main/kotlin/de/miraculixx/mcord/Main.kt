package de.miraculixx.mcord

import de.miraculixx.mcord.config.Config
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

fun main() {
    Main()
}

class Main {

    companion object {
        lateinit var INSTANCE: Main
        lateinit var KTOR: HttpClient
    }

    private val updater: Job
    var jda: JDA? = null

    init {
        INSTANCE = this
        KTOR = HttpClient(CIO)

        val builder = JDABuilder.createDefault(Config.botToken)
        //builder.disableCache(CacheFlag.VOICE_STATE)
        builder.setActivity(Activity.listening("Miraculixx's complains"))
        builder.setStatus(OnlineStatus.IDLE)
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
        val lateInits = listOf(SlashCommandManager())
        builder.addEventListeners(lateInits[0], ButtonManager(), TabComplete(), DropDownManager(), ModalManager(), MessageReactor())

        jda = builder.build()
        jda!!.awaitReady()

        //LateInit Setup
        lateInits.forEach {
            it.setup()
        }

        updater = Updater.start(jda!!)
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
                        jda?.shardManager?.setStatus(OnlineStatus.OFFLINE)
                        jda?.shutdown()
                        KTOR.close()
                        updater.cancel()
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