package de.miraculixx.mcord.modules.mutils

import de.miraculixx.mcord.config.Config
import de.miraculixx.mcord.utils.KeyInfoDisplays
import de.miraculixx.mcord.utils.api.API
import de.miraculixx.mcord.utils.api.callAPI
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.entities.EntityBuilder

class CommandAccount : SlashCommandEvent {
    @kotlinx.serialization.Serializable
    private data class MC(val name: String, val id: String)

    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        //Check if user has Premium
        val member = it.member ?: return
        val guild = it.guild ?: return
        val premium = guild.getRoleById(909192161386430484)
        val sub = guild.getRoleById(938898054151561226)
        val jda = it.jda
        val tool = KeyInfoDisplays(it.hook, jda)

        it.reply("<a:loading:972893675145265262> Communicating with Service...").setEphemeral(true).queue()
        val hook = it.hook

        when (it.name) {
            "key-generate" -> {
                val user = tool.getUser(member.id)
                if (user == null) {
                    //New Key
                    val mc = it.getOption("minecraft_name")?.asString ?: "error"
                    val uuid = try {
                        Json.decodeFromString<MC>(callAPI(API.MINECRAFT, "minecraft/$mc"))
                    } catch (e: Exception) {
                        delay(1000)
                        hook.editOriginal("```diff\n- Der angegebene Minecraft Account konnte nicht gefunden werden!```").queue()
                        return
                    }
                    val newUser = Json.decodeFromString<KeyInfoDisplays.User>(callAPI(API.MUTILS, "admin.php?call=createuser&pw=${Config.API_KEY}&mc=${uuid.name}&uuid=${uuid.id}&dc=${member.id}"))

                    //Add Ranks
                    if (member.isBoosting) callAPI(API.MUTILS, "admin.php?pw=${Config.API_KEY}&call=addrank&id=${newUser.id}&type=Boosting&slots=1&expire=never")
                    if (member.roles.contains(premium)) callAPI(API.MUTILS, "admin.php?pw=${Config.API_KEY}&call=addrank&id=${newUser.id}&type=Unlimited&slots=6&expire=never")
                    if (member.roles.contains(sub)) callAPI(API.MUTILS, "admin.php?pw=${Config.API_KEY}&call=addrank&id=${newUser.id}&type=Subscriber&slots=2&expire=never")

                    delay(2000)
                    val buttons = listOf(
                        Button.link("https://mutils.de/m/shop", "Server Slots erhalten").withEmoji(Emoji.fromUnicode("\uD83D\uDED2")),
                        Button.link("https://mutils.de", "Download").withEmoji(Emoji.fromUnicode("\uD83D\uDD3B"))
                    )
                    hook.editOriginal("**Key Erfolgreich erstellt**")
                        .setEmbeds(
                            EntityBuilder(jda).createMessageEmbed(
                                DataObject.fromJson( //"type":"rich",
                                    "{\"type\":\"rich\",\"description\":\"Gebe folgende Daten an niemanden weiter! Diese Daten geben dir vollständigen Zugriff auf MUtils und deine erworbenen Lizenzen. Mehr Daten kannst du jederzeit über `/key-info` aufrufen!\\n<:blanc:784059217890770964>\",\"color\":36637,\"fields\":[{\"name\":\"**Aktivierte Lizenz**\",\"value\":\"> Key `->` ${newUser.licence}\\n> Server Slots `->` */key-info*\",\"inline\":true},{\"name\":\"**Account Daten**\",\"value\":\"> Minecraft Name `->` ${newUser.mc}\\n> Minecraft UUID `->` ${newUser.uuid}\\n> Discord ID `->` ${newUser.dc}\",\"inline\":true},{\"name\":\"Nutzung\",\"value\":\"Gebe auf einem gewünschten Server, auf welchem MUtils installiert ist, den Befehl `/verify <key>` ein. Sollte nun noch ein Slot verfügbar sein, wird der Server mit MUtils Premium neugestartet.\\nWICHTIG -> Dies geht nur über den angegebenen Minecraft Account\"}],\"footer\":{\"text\":\"MUtils - The Ultimate Challenge-Utility Plugin!\",\"icon_url\":\"https://i.imgur.com/xe2N5eF.png\"}}"
                                )
                            )
                        ).setActionRow(buttons).queue()
                } else {
                    delay(1000)
                    hook.editOriginal("```diff\n- Du hast bereits einen Account!\n- Rufe ihn über /key-info auf!```").queue()
                }
            }
            "key-delete" -> {
                val user = tool.getUser(member.id)
                delay(5000)
                if (user == null) {
                    hook.editOriginal("```diff\n- Du besitzt aktuell keinen Account!\n- Erstelle einen über /key-generate```").queue()
                    return
                } else {
                    hook.editOriginal("⚠️**Dieser Schritt kann nicht rückgängig gemacht werden** ⚠️ **Käufe gehen verloren** ⚠️\nBist du absolut sicher, dass du deinen Account vollständig löschen möchtest?\n\nWenn du dir sicher bist, klicke auf \"Confirm\"")
                        .setActionRow(Button.danger("deleteuser_${user.id}_${user.licence}", "Confirm")).queue()
                }
            }
            "key-info" -> {
                tool.accountInfo(member.id)
            }
            "key-update" -> {
                delay(2000)
                val user = tool.getUser(member.id)
                if (user == null) {
                    hook.editOriginal("```diff\n- Du besitzt aktuell keinen Account!\n- Erstelle einen über /key-generate```").queue()
                    return
                }
                val isBoosting = member.isBoosting
                val isPremium = member.roles.contains(premium)
                val isSub = member.roles.contains(sub)
                val option = it.getOption("rank")?.asString?.lowercase()

                val rankTypes = user.ranks?.map { it.type }!!

                when (option) {
                    "unlimited" -> {
                        if (!isPremium || rankTypes.contains("Unlimited")) {
                            hook.editOriginal(noPerms("Unlimited")).queue()
                            return
                        } else callAPI(API.MUTILS, "admin.php?call=addrank&pw=${Config.API_KEY}&id=${user.id}&type=Unlimited&slots=6&expire=never")
                    }
                    "booster" -> {
                        if (!isBoosting || rankTypes.contains("Boosting")) {
                            hook.editOriginal(noPerms("Booster")).queue()
                            return
                        } else callAPI(API.MUTILS, "admin.php?call=addrank&pw=${Config.API_KEY}&id=${user.id}&type=Boosting&slots=1&expire=never")
                    }
                    "subscriber" -> {
                        if (!isSub || rankTypes.contains("Subscriber")) {
                            hook.editOriginal(noPerms("Subscriber")).queue()
                            return
                        } else callAPI(API.MUTILS, "admin.php?call=addrank&pw=${Config.API_KEY}&id=${user.id}&type=Subscriber&slots=2&expire=never")
                    }
                    else -> {
                        hook.editOriginal(noPerms("None")).queue()
                        return
                    }
                }
                hook.editOriginal("**Rang Aktiviert**\nDir wurde erfolgreich der Rang \"$option\" aktiviert! Gebe `/key-info` ein, um mehr zu erfahren.").queue()
            }
        }
    }

    private fun noPerms(type: String): String {
        return "```diff\n- Auf deinem Discord Account konnte der Rang \"$type\" nicht gefunden werden!\n" +
                "- Unlimited kannst du im Webshop erwerben - https://mutils.de/m/shop\n" +
                "- Subscriber wird über eine Twitch Subscription erhalten - https://miraculixx.de/tv\n" +
                "- Booster sind Discord Booster dieses Servers oder des Community Servers - https://miraculixx.de/dc ```"
    }
}