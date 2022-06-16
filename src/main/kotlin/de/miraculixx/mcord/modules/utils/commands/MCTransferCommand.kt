package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.utils.api.MinecraftTools
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import java.sql.Timestamp
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MCTransferCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val name = it.getOption("name")
        val uuid = it.getOption("uuid")
        val mcTools = MinecraftTools()
        if (uuid != null || name != null) {
            val data = if (uuid != null) mcTools.getUserData(uuid.asString) else mcTools.getUserData(name!!.asString)
            val id = uuid ?: data.uuid
            println("Data found")
            val history = mcTools.nameHistory(data.uuid)
            println("Name history found")
            val timestamp = if (data.creation_date != null) "<t:"+Timestamp.valueOf(data.creation_date + " 12:00:00").time.div(1000)+":D>"
            else "*Mojang deletes it...*"

            val builder = StringBuilder(
                "**Current Name** `->` ${data.username}\n" +
                        "**UUID** `->` $id\n" +
                        "**Creation Date** `->` $timestamp\n" +
                        "\n**Name History** \uD83D\uDCD7\n"
            )
            if (history.size <= 1) {
                builder.append("> *Never changed*")
            } else {
                history.reversed().forEach { nameData ->
                    if (nameData.changedToAt == 0L) {
                        builder.append("> ${nameData.name} `->` $timestamp (First Name)\n")
                    } else
                        builder.append("> ${nameData.name} `->` <t:${nameData.changedToAt.div(1000)}:f>\n")
                }
            }
            it.reply(builder.toString()).queue()
        } else {
            it.reply("""
                ```diff
                - You need to put at least one name or a uuid
                ```
            """.trimIndent()).setEphemeral(true).queue()
            return
        }
    }
}