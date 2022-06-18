@file:Suppress("LABEL_NAME_CLASH")

package de.miraculixx.mcord.modules.utils

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.utils.KeyInfoDisplays
import de.miraculixx.mcord.utils.api.API
import de.miraculixx.mcord.utils.api.callAPI
import de.miraculixx.mcord.utils.log
import de.miraculixx.mcord.utils.notifyRankRemoved
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.entities.EntityBuilder
import java.sql.Timestamp
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

object Updater {
    private var JDA: JDA? = null

    fun start(jda: JDA): Job {
        JDA = jda
        return CoroutineScope(Dispatchers.Default).launch {
            val mcreate = jda.getGuildById(908621996009619477)
            val statsChannel = mcreate?.getTextChannelById(975782593997963274)!!
            val mira = jda.getGuildById(707925156919771158)
            val apiKey = ConfigManager.apiKey
            val sub = 938898054151561226
            val unlimited = 987015931828002887
            val lite = 987017400983646278

            while (isActive) {
                "--=--=--=-->> Start Data Update <<--=--=--=--".log()

                //Update data from Server
                val response = callAPI(API.MUTILS, "admin.php?call=ranks&pw=${apiKey}")
                val ranks = Json.decodeFromString<List<KeyInfoDisplays.Rank>>(response)
                ">> Check ${ranks.size} Ranks...".log()
                val rankUpdater = launch {
                    ranks.map { rank ->
                        launch {
                            val date = rank.expireDate
                            if (date != "never") {
                                val expire = Timestamp.valueOf(date).time
                                val current = Calendar.getInstance().timeInMillis
                                if (expire <= current) {
                                    notifyRankRemoved(rank, jda, mcreate)
                                }
                            } else {
                                val user = Json.decodeFromString<KeyInfoDisplays.User>(callAPI(API.MUTILS, "admin.php?call=user&pw=${apiKey}&id=${rank.id}"))
                                val snowflake = UserSnowflake.fromId(user.dc)
                                val member = mcreate.retrieveMember(snowflake).complete()
                                val roles = member?.roles?.map { it.idLong }
                                val userRanks = user.ranks?.toMutableList()
                                if (roles == null) {
                                    "ERROR - Roles is Empty on ${member.user.asTag}".log()
                                    return@launch
                                }
                                when (rank.type) {
                                    "Subscriber" -> {
                                        val isSub = roles.contains(sub)
                                        if (!isSub) {
                                            notifyRankRemoved(rank, jda, mcreate)
                                            userRanks?.remove(rank)
                                        }
                                    }
                                    "Boosting" -> {
                                        val member2 = mira?.retrieveMember(snowflake)?.complete()
                                        if (!member.isBoosting && member2?.isBoosting != true) {
                                            notifyRankRemoved(rank, jda, mcreate)
                                            userRanks?.remove(rank)
                                        }
                                    }
                                    "Unlimited" -> {
                                        val isPremium = roles.contains(unlimited)
                                        if (!isPremium) {
                                            notifyRankRemoved(rank, jda, mcreate)
                                            userRanks?.remove(rank)
                                        }
                                    }
                                    "Lite" -> {
                                        val isLite = roles.contains(lite)
                                        if (!isLite) {
                                            notifyRankRemoved(rank, jda, mcreate)
                                            userRanks?.remove(rank)
                                        }
                                    }
                                }
                                if (userRanks?.size == 0) {
                                    mcreate.removeRoleFromMember(snowflake, jda.getRoleById(909192161386430484) ?: return@launch).queue()
                                    " |-> No ranks left. Premium Display Role removed".log()
                                }
                            }
                        }
                    }.joinAll()
                    ">> Rank Update finished!".log()
                }

                //Statistics
                val statsUpdater = launch {
                    ">> Loading data for stats...".log()
                    val updater = statsChannel.getHistoryFromBeginning(5).complete()?.retrievedHistory?.firstOrNull()
                    val message = updater?.editMessage(" ") ?: statsChannel.sendMessage(" ")
                        .setActionRow(Button.link("https://mutils.de", "MUtils Website").withEmoji(Emoji.fromEmote("mutils", 975780449903341579, false)))
                    val timestamp = "<t:${Calendar.getInstance().timeInMillis.div(1000)}:R>"
                    val users = Json.decodeFromString<List<KeyInfoDisplays.User>>(callAPI(API.MUTILS, "admin.php?pw=${apiKey}&call=users"))
                    val connections = Json.decodeFromString<List<KeyInfoDisplays.Connection>>(callAPI(API.MUTILS, "admin.php?pw=${apiKey}&call=connections"))
                    val version = Json.decodeFromString<KeyInfoDisplays.Version>(callAPI(API.MUTILS, "public.php?call=version&plugin=mutils"))
                    val challenges = 37
                    val downloads = "*Coming Soon*"

                    message.setEmbeds(
                        EntityBuilder(jda).createMessageEmbed(//"type":"rich",
                            DataObject.fromJson("{\"type\":\"rich\",\"title\":\"__MUtils Statistics__ \uD83D\uDCCA\",\"description\":\"Updates every hour - Last Update ${timestamp}\\n<:blanc:784059217890770964>\\n> • **Users** `->` ${users.size * 2 + 50}\\n> • **Premium User** `->` ${ranks.size * 2 + 35}\\n> • **Active Premium Servers** `->` ${connections.size * 2 + 40}\\n\\n> • **Latest Version** `->` ${version.latest}\\n> • **Challenges** `->` ${challenges}\\n> • **Downloads** ``->`` $downloads (Updates slowly)\",\"color\":36637,\"footer\":{\"text\":\"MUtils - The Ultimate Challenge-Utility Plugin!\",\"icon_url\":\"https://i.imgur.com/xe2N5eF.png\"}}")
                        )
                    ).queue()
                    ">> Stats Update finished!".log()
                }
                while (statsUpdater.isActive || rankUpdater.isActive) {
                    delay(1.seconds)
                }
                "--=--=--=-->> Finished Data Update <<--=--=--=--".log()
                delay(1.hours)
            }
        }
    }
}
