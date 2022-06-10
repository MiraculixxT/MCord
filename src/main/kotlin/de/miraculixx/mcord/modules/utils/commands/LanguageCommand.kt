package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.utils.entities.SlashCommands
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class LanguageCommand: SlashCommands {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val user = it.user
        val id = UserSnowflake.fromId(user.id)
        val guild = it.guild
        if (guild == null) {
            it.reply("This Command is only available on the **MCreate** Server\nJoin now https://discord.gg/VEcR8RbnSH").queue()
            return
        }

        val roleEnglish = guild.getRoleById(909155037727559710)!!
        val roleGerman = guild.getRoleById(909154807246385192)!!
        when (it.getOption("language")?.asString?.lowercase()) {
            "german" -> {
                guild.removeRoleFromMember(id, roleEnglish).queue()
                guild.addRoleToMember(id, roleGerman).queue()
                it.reply("Erfolgreich die Sprache zu `german` gewechselt!\n" +
                        "Ändere diese Einstellung jederzeit in <#908621996676501567>").setEphemeral(true).queue()
            }
            "english" -> {
                guild.removeRoleFromMember(id, roleGerman).queue()
                guild.addRoleToMember(id, roleEnglish).queue()
                it.reply("Successfully switched your language to `english`!\n" +
                        "Switch it at any time in <#908621996676501567>").setEphemeral(true).queue()
            }
            else -> {
                it.reply("Please select a valid language! The language change all Bot command and information channel messages." +
                        "\n\n**Current Languages**\n" +
                        "> - English\n" +
                        "> - German").setEphemeral(true).queue()
                return
            }
        }
    }
}