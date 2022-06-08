package de.miraculixx.mcord.commands.events

import de.miraculixx.mcord.commands.SlashCommands
import de.miraculixx.mcord.utils.MinecraftTools
import de.miraculixx.mcord.utils.msg
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.sql.Timestamp

class MCTransferCommand : SlashCommands {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val member = it.member ?: return
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
            it.reply(msg("missingValue", member)).setEphemeral(true).queue()
            return
        }
    }
}