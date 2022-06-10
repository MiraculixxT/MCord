package de.miraculixx.mcord.utils

import de.miraculixx.mcord.utils.api.API
import de.miraculixx.mcord.utils.api.callAPI
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.entities.EntityBuilder
import java.sql.Timestamp

class KeyInfoDisplays(private val hook: InteractionHook, private val jda: JDA) {
    val await = "<a:loading:972893675145265262> Communicating with Service..."

    suspend fun accountInfo(dcID: String) {
        val user = getUser(dcID)
        if (user == null) {
            hook.editOriginal("```diff\n- Du besitzt aktuell keinen Account!\n- Erstelle einen über /key-generate```").queue()
            return
        } else {
            val ranks = user.ranks
            var slots = 0
            var displayRanks = ""
            ranks!!.forEach {
                slots += it.slots
                displayRanks += "\\n> <:blanc:784059217890770964> • ${it.type}"
                if (it.expireDate != "never") {
                    displayRanks += " (<t:${Timestamp.valueOf(it.expireDate).time.div(1000)}:R>)"
                }
            }
            if (ranks.isEmpty()) displayRanks = "*Keine*"

            hook.editOriginal("").setEmbeds(
                EntityBuilder(jda).createMessageEmbed(
                    DataObject.fromJson( //"type":"rich",
                        "{\"type\":\"rich\",\"title\":\"Account Informationen\",\"description\":\"Gebe folgende Daten an niemanden weiter! Diese Daten geben dir vollständigen Zugriff auf MUtils und deine erworbenen Lizenzen.\",\"color\":36637,\"fields\":[{\"name\":\"**Aktivierte Lizenz**\",\"value\":\"> Key `->` ||${user.licence}||\\n> Slots `->` ${slots}\\n> Ranks `->` $displayRanks\",\"inline\":true},{\"name\":\"**Account Daten**\",\"value\":\"> Minecraft Name `->` ${user.mc}\\n> Minecraft UUID `->` ${user.uuid}\\n> Discord ID `->` ${user.dc}\",\"inline\":true},{\"name\":\"Nutzung\",\"value\":\"Gebe auf einem gewünschten Server, auf welchem MUtils installiert ist, den Befehl `/verify <key>` ein. Sollte nun noch ein Slot verfügbar sein, wird der Server mit MUtils Premium neugestartet.\\nWICHTIG -> Dies geht nur über den angegebenen Minecraft Account\"}],\"footer\":{\"text\":\"MUtils - The Ultimate Challenge-Utility Plugin!\",\"icon_url\":\"https://i.imgur.com/xe2N5eF.png\"}}"
                    )
                )
            ).setActionRow(getDropDown(user.id, user.licence, slots)).queue()
        }
    }

    suspend fun serverInfo(sourceMessage: Message, userID: String, ip: String, update: Boolean = false, dcID: String = "0") {
        val response = callAPI(API.MUTILS, "admin.php?call=singleconnection&pw=$pw&id=$userID&ip=$ip")
        val connection = Json.decodeFromString<Connection>(response)
        val versionSplit = connection.serverVersion?.split('_')
        val serverSoftware = if ((versionSplit?.size ?: 0) > 1) versionSplit?.get(0) ?: "*Unbekannt*" else "*Unbekannt*"
        val serverVersion = if (versionSplit == null || versionSplit.isEmpty()) "*Unbekannt*" else if (versionSplit.size == 1) versionSplit[0] else versionSplit[1]
        val timestamp = "<t:${Timestamp.valueOf(connection.date).time.div(1000)}:F>"
        val data = "${userID}_$ip"
        val serverName = connection.name ?: "*Unset*"

        val renameButton = Button.primary("conButtonRename_$data", "Rename Server").withEmoji(Emoji.fromUnicode("\uD83C\uDFF7️"))
        val deleteButton = Button.danger("conButtonDelete_$data", "Delete Server").withEmoji(Emoji.fromUnicode("\uD83D\uDDD1️"))
        val overviewButton = Button.secondary("conButtonBack_$data", "Overview").withEmoji(Emoji.fromUnicode("⚙️"))
        var serverDropDown: SelectMenu? = null
        if (!update)
            sourceMessage.actionRows.forEach { row ->
                row.components.forEach { component ->
                    if (component.type == Component.Type.SELECT_MENU)
                        serverDropDown = component as SelectMenu
                }
            }
        else {
            val user = getUser(dcID)
            val ranks = user?.ranks
            var slots = 0
            ranks?.forEach { slots += it.slots }
            if (user == null) {
                hook.editOriginal("```diff\n- Unable to find the request Account\n- Maybe you don't created one? (/key-generate)```")
            } else serverDropDown = getDropDown(userID.toInt(), user.licence, slots)
        }

        hook.editOriginal("").setEmbeds(
            EntityBuilder(jda).createMessageEmbed(
                DataObject.fromJson( //"type":"rich",
                    "{\"type\":\"rich\",\"title\":\"Server Übersicht\",\"description\":\"Beobachte oder editiere direkt von Discord aus alle verbundenen Server! Du kannst ihnen über den Knopf `Rename Server` einen eigenen Namen geben, um ihn leichter zu erkennen\",\"color\":36637,\"fields\":[{\"name\":\"**Server Informationen**\",\"value\":\"> IP `->` ||${connection.ip}||\\n> Name `->` ${serverName}\\n> Server Version `->` ${serverVersion}\\n> Server Software `->` ${serverSoftware}\\n> MUtils Version `->` ${connection.mutils ?: "Unbekannt"}\\n> Aktiviert `->` ${timestamp}\",\"inline\":true}],\"footer\":{\"text\":\"MUtils - The Ultimate Challenge-Utility Plugin!\",\"icon_url\":\"https://i.imgur.com/xe2N5eF.png\"}}"
                )
            )
        ).setActionRows(listOf(ActionRow.of(overviewButton, renameButton, deleteButton), ActionRow.of(serverDropDown))).queue()
    }


    //UTILITYS

    @kotlinx.serialization.Serializable
    data class User(
        val id: Int,
        val uuid: String,
        val licence: String,
        val mc: String,
        val dc: String,
        val date: String,
        val ranks: List<Rank>? = null
    )

    @kotlinx.serialization.Serializable
    data class Rank(
        val id: Int,
        val type: String,
        val expireDate: String,
        val slots: Int
    )

    @kotlinx.serialization.Serializable
    data class Connection(
        val ip: String,
        val date: String,
        val serverVersion: String?,
        val mutils: String?,
        val name: String?
    )
    @kotlinx.serialization.Serializable
    data class Version(
        val last: String,
        val latest: String,
    )

    suspend fun getUser(id: String): User? {
        val json = callAPI(API.MUTILS, "admin.php?call=user&pw=$pw&dc=$id")
        if (json.isEmpty() || json.startsWith("error")) {

            return null
        }
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            println("ERROR - Json Parser hat verkackt")
            println(json)
            null
        }
    }

    private suspend fun getDropDown(id: Int, key: String, max: Int): SelectMenu? {
        val builder = SelectMenu.create("editcons_${id}_${key}")
        builder.maxValues = 1
        val response = callAPI(API.MUTILS, "admin.php?call=connections&pw=$pw&id=${id}")
        val empty = response.contains("NO_ENTRYS")
        if (response.startsWith("error") && !empty) {
            delay(5000)
            hook.editOriginal("```diff\n- Ein unerwarteter Fehler ist aufgetreten. Möglicherweise hast du noch kein Account /key-generate\n- Andernfalls kontaktiere bitte das Team!```")
                .queue()
            println(response)
            return null
        }

        if (!empty) {
            val conns = Json.decodeFromString<List<Connection>>(response)
            conns.forEach {
                val name = it.name ?: it.ip
                val sVersion = it.serverVersion?.lowercase() ?: "error"
                val emote = if (sVersion.contains("paper")) Emoji.fromEmote("paper", 972959254740881418, false)
                else if (sVersion.contains("spigot")) Emoji.fromEmote("spigot", 313644584355758080, false)
                else Emoji.fromEmote("minecraft", 973316627166806086, false)
                builder.addOption(name, "serverSelect_${id}_${it.ip}", "Auswählen zum editieren des Servers", emote)
            }
            builder.placeholder = "Verbundene Server - Slots ${conns.size}/${max}"

        } else {
            builder.addOption("No Server", "noserver", "No Server found")
            builder.isDisabled = true
            builder.placeholder = "Keine verbundenen Server - Slots 0/${max}"
        }
        return builder.build()
    }
}