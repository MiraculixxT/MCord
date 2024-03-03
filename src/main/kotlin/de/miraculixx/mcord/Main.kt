package de.miraculixx.mcord

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.modules.utils.events.MessageReactor
import de.miraculixx.mcord.modules.utils.events.RoleRevokeEvent
import de.miraculixx.mcord.modules.utils.events.TabComplete
import de.miraculixx.mcord.utils.WebClient
import de.miraculixx.mcord.utils.log.log
import de.miraculixx.mcord.utils.manager.ButtonManager
import de.miraculixx.mcord.utils.manager.DropDownManager
import de.miraculixx.mcord.utils.manager.ModalManager
import de.miraculixx.mcord.utils.manager.SlashCommandManager
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.jdabuilder.default
import dev.minn.jda.ktx.jdabuilder.intents
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.*

fun main() {
    Main()
}

lateinit var INSTANCE: Main

class Main {
    lateinit var jda: JDA

    init {
        INSTANCE = this

        getDefaultScope().launch {
            val coreConf = ConfigManager.getConfig(Configs.CORE)
            jda = default("OTU4NDM0OTIzMTAwOTEzODA0.GG-i3c.6irF1BAdgAd8NayHcJ3Rm2ecPFQEh51SbkZmoo") {
                disableCache(CacheFlag.VOICE_STATE)
                setActivity(Activity.listening("Miraculixx's complains"))
                setStatus(OnlineStatus.IDLE)
                intents += listOf(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                setMemberCachePolicy(MemberCachePolicy.ALL)
                setChunkingFilter(ChunkingFilter.include(908621996009619477, 707925156919771158))
            }
            jda.awaitReady()

            ButtonManager.startListen(jda)
            DropDownManager.startListen(jda)
            ModalManager.startListen(jda)
            SlashCommandManager.startListen(jda)
            MessageReactor.startListen(jda)
            TabComplete().startListen(jda)
            WebClient
            val roles = RoleRevokeEvent()
            roles.startListen(jda)
            roles.startListen2(jda)

            //val logger = LogManager.getRootLogger() as Logger
            //LogManager.getRootLogger()
            //logger.addAppender(LogAppender(guildMCreate.getChannel(909188184691339264) ?: return@launch))

            "MKord is now online!".log()
        }

        shutdown()
    }

    private fun shutdown() {
        runBlocking {
            var online = true
            while (online) {
                val scanner = Scanner(System.`in`)
                when (val out = scanner.nextLine()) {
                    "exit" -> {
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