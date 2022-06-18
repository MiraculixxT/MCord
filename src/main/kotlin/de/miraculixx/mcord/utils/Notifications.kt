package de.miraculixx.mcord.utils

import de.miraculixx.mcord.config.ConfigManager.apiKey
import de.miraculixx.mcord.utils.api.API
import de.miraculixx.mcord.utils.api.callAPI
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.entities.EntityBuilder
import java.sql.Timestamp

suspend fun notifyRankRemoved(rank: KeyInfoDisplays.Rank, jda: JDA, mcreate: Guild?) {
    ">> Rank Remover -> ${rank.id} ${rank.type}".log()

    val buttons = listOf(
        Button.link("https://mutils.de/dc", "MCreate").withEmoji(Emoji.fromEmote("mutils", 975780449903341579, false)),
        Button.link("https://mutils.de/m/shop", "Slots Shop").withEmoji(Emoji.fromUnicode("\uD83D\uDED2"))
    )
    val id = callAPI(API.MUTILS, "admin.php?call=removerank&pw=$apiKey&type=${rank.type}&id=${rank.id}")
    val user = jda.retrieveUserById(id).complete()
    val expireDate = rank.expireDate
    val timestamp = if (expireDate == "never") "<t:${System.currentTimeMillis().div(1000)}:F>"
    else "<t:${Timestamp.valueOf(rank.expireDate).time.div(1000)}:F>"
    try {
        user.openPrivateChannel().complete()
            ?.sendMessage(" ")?.setEmbeds(
                EntityBuilder(jda).createMessageEmbed(
                    DataObject.fromJson("{\"type\":\"rich\",\"title\":\"MUtils Account Update\",\"description\":\"Leider ist ein Rang von dir gerade abgelaufen... Damit wurde deine maximale Slot Anzahl verringert und möglicherweise Serververbindungen gelöscht. Für mehr Informationen gebe auf dem [MCreate Discord](https://mutils.de/dc) `/key-info` ein.\",\"color\":36637,\"fields\":[{\"name\":\"Abgelaufener Rang\",\"value\":\"> Name `->` ${rank.type}\\n> Slots `->` ${rank.slots}\\n> Abgelaufen am`->` ${timestamp}\",\"inline\":true}],\"footer\":{\"text\":\"MUtils - The Ultimate Challenge-Utility Plugin!\",\"icon_url\":\"https://i.imgur.com/xe2N5eF.png\"}}")
                )
            )?.setActionRow(buttons)?.queue()
    } catch (e: Exception) {
        mcreate?.getTextChannelById(908839500527050804)
            ?.sendMessage("${user.asMention} Du hast eine private Benachrichtigung erhalten, jedoch sind deine Direktnachrichten deaktiviert! Du kannst dies in den Privatsphäre Einstellungen umstellen")
        " |-> User ${user.asTag} has disabled DM's".log()
    }
}

suspend fun notifyRankAdd(rank: KeyInfoDisplays.Rank, jda: JDA, user: User) {

    val buttons = listOf(
        Button.link("https://mutils.de/dc", "MCreate").withEmoji(Emoji.fromEmote("mutils", 975780449903341579, false)),
    )
    val mcreate = jda.getGuildById(908621996009619477)
    val apiUser = getUser(user.id)
    if (apiUser?.id == null) {
        ">> Rank Adder -> FAILED - User has no Account".log()
        return
    }
    ">> Rank Adder -> ${apiUser.id} ${rank.type}".log()
    callAPI(API.MUTILS, "admin.php?call=addrank&pw=${apiKey}&id=${apiUser.id}&type=${rank.type}&slots=${rank.slots}&expire=${rank.expireDate}")
    val timestamp = if (rank.expireDate == "never") "Lifetime" else "<t:${Timestamp.valueOf(rank.expireDate).time.div(1000)}:F>"
    val embed = EmbedBuilder()
        .setTitle("⚙️ | MUtils Account Update")
        .setDescription("Ein neuer Rang wurde soeben auf deinem Account aktiviert! Dies erhöht deine maximale Server Slot Anzahl für Premium Server. Für mehr Informationen gebe auf dem [MCreate Discord](https://mutils.de/dc) `/key-info` ein.")
        .addField("Hinzugefügter Rang", "> Name `->` ${rank.type}\n> Slots `->` ${rank.slots}\n> Gültig bis `->` $timestamp", false)
        .setColor(0x008f1d)
        .setFooter("MUtils - The Ultimate Challenge-Utility Plugin!", "https://i.imgur.com/xe2N5eF.png")
    try {
        user.openPrivateChannel().complete()!!.sendMessage(" ").setEmbeds(embed.build()).setActionRow(buttons).queue()
    } catch (e: Exception) {
        mcreate?.getTextChannelById(908839500527050804)!!
            .sendMessage("${user.asMention} Du hast eine private Benachrichtigung erhalten, jedoch sind deine Direktnachrichten deaktiviert! Du kannst dies in den Privatsphäre Einstellungen umstellen")
            .queue()
        " |-> User ${user.asTag} has disabled DM's".log()
    }
}