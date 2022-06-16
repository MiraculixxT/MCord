package de.miraculixx.mcord.modules.utils

import de.miraculixx.mcord.config.Config
import de.miraculixx.mcord.utils.KeyInfoDisplays
import de.miraculixx.mcord.utils.api.API
import de.miraculixx.mcord.utils.api.callAPI
import de.miraculixx.mcord.utils.log
import java.sql.Timestamp
import java.util.Calendar
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.entities.EntityBuilder

object Updater {
    private var JDA: JDA? = null

    fun start(jda: JDA): Job {
        JDA = jda
        return CoroutineScope(Dispatchers.Default).launch {
            val mcreate = jda.getGuildById(908621996009619477)
            val statsChannel = mcreate?.getTextChannelById(975782593997963274)!!
            val mira = jda.getGuildById(707925156919771158)

            while (isActive) {
                "--=--=--=-->> Start Data Update <<--=--=--=--".log()

                //Update data from Server
                val response = callAPI(API.MUTILS, "admin.php?call=ranks&pw=${Config.API_KEY}")
                val ranks = Json.decodeFromString<List<KeyInfoDisplays.Rank>>(response)
                val buttons = listOf(
                    Button.link("https://mutils.de/dc", "MCreate").withEmoji(Emoji.fromEmote("mutils", 975780449903341579, false)),
                    Button.link("https://mutils.de/m/shop", "Slots Shop").withEmoji(Emoji.fromUnicode("\uD83D\uDED2"))
                )
                ">> Check ${ranks.size} Ranks...".log()
                val rankUpdater = launch {
                    ranks.map { rank ->
                        launch {
                            val date = rank.expireDate
                            if (date != "never") {
                                val expire = Timestamp.valueOf(date).time
                                val current = Calendar.getInstance().timeInMillis
                                if (expire <= current) {
                                    removeRank(rank, buttons, mcreate)
                                }
                            } else {
                                val user = Json.decodeFromString<KeyInfoDisplays.User>(callAPI(API.MUTILS, "admin.php?call=user&pw=${Config.API_KEY}&id=${rank.id}"))
                                when (rank.type) {
                                    "Subscriber" -> {
                                        val member = mcreate.retrieveMember(UserSnowflake.fromId(user.dc)).complete()
                                        val isSub = member?.roles?.contains(mcreate.getRoleById(938898054151561226)) ?: false
                                        if (!isSub) {
                                            removeRank(rank, buttons, mcreate)
                                        }
                                    }
                                    "Boosting" -> {
                                        val member = mcreate.retrieveMember(UserSnowflake.fromId(user.dc)).complete()
                                        val member2 = mira?.retrieveMember(UserSnowflake.fromId(user.dc))?.complete()
                                        if (member?.isBoosting != true && member2?.isBoosting != true) {
                                            removeRank(rank, buttons, mcreate)
                                        }
                                    }
                                    "Unlimited" -> {
                                        val member = mcreate.retrieveMember(UserSnowflake.fromId(user.dc)).complete()
                                        val isPremium = member?.roles?.contains(mcreate.getRoleById(909192161386430484)) ?: false
                                        if (!isPremium) {
                                            removeRank(rank, buttons, mcreate)
                                        }
                                    }
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
                    val users = Json.decodeFromString<List<KeyInfoDisplays.User>>(callAPI(API.MUTILS, "admin.php?pw=${Config.API_KEY}&call=users"))
                    val connections = Json.decodeFromString<List<KeyInfoDisplays.Connection>>(callAPI(API.MUTILS, "admin.php?pw=${Config.API_KEY}&call=connections"))
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

    private suspend fun removeRank(rank: KeyInfoDisplays.Rank, buttons: List<Button>, mcreate: Guild?) {
        ">> Rank Remover -> ${rank.id} ${rank.type}".log()

        val id = callAPI(API.MUTILS, "admin.php?call=removerank&pw=${Config.API_KEY}&type=${rank.type}&id=${rank.id}")
        val user = JDA!!.retrieveUserById(id).complete()
        val expireDate = rank.expireDate
        val timestamp = if (expireDate == "never") "<t:${System.currentTimeMillis().div(1000)}:F>"
        else "<t:${Timestamp.valueOf(rank.expireDate).time.div(1000)}:F>"
        try {
            user.openPrivateChannel().complete()
                ?.sendMessage(" ")?.setEmbeds(
                    EntityBuilder(JDA).createMessageEmbed(
                        DataObject.fromJson("{\"type\":\"rich\",\"title\":\"MUtils Account Update\",\"description\":\"Leider ist ein Rang von dir gerade abgelaufen... Damit wurde deine maximale Slot Anzahl verringert und möglicherweise Serververbindungen gelöscht. Für mehr Informationen gebe auf dem [MCreate Discord](https://mutils.de/dc) `/key-info` ein.\",\"color\":36637,\"fields\":[{\"name\":\"Abgelaufener Rang\",\"value\":\"> Name `->` ${rank.type}\\n> Slots `->` ${rank.slots}\\n> Abgelaufen am`->` ${timestamp}\",\"inline\":true}],\"footer\":{\"text\":\"MUtils - The Ultimate Challenge-Utility Plugin!\",\"icon_url\":\"https://i.imgur.com/xe2N5eF.png\"}}")
                    )
                )?.setActionRow(buttons)?.queue()
        } catch (e: Exception) {
            mcreate?.getTextChannelById(908839500527050804)
                ?.sendMessage("${user.asMention} Du hast eine private Benachrichtigung erhalten, jedoch sind deine Direktnachrichten deaktiviert! Du kannst dies in den Privatsphäre Einstellungen umstellen")
            " |-> User ${user.asTag} has disabled DM's".log()
        }

    }
}
