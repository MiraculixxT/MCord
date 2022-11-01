package de.miraculixx.mcord.modules.games.idle

import de.miraculixx.mcord.utils.entities.DropDownEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class DropDownHelp : DropDownEvent {
    override suspend fun trigger(it: GenericSelectMenuInteractionEvent<String, StringSelectMenu>) {
        val options = it.values
        if (options.isEmpty()) return

        val embed = when (options.first()) {
            "gameinfo" -> EmbedBuilder()
                .setTitle("\uD83C\uDF34 | Game Information")
                .setDescription("Hier stehen Informationen zum Spiel an sich")

            "upgrades" -> EmbedBuilder()
                .setTitle("\uD83D\uDD3A | Upgrades")
                .setDescription("Hier stehen Informationen zu Upgrades und wie diese funktionieren")

            "buildings" -> EmbedBuilder()
                .setTitle("\uD83C\uDFD7️ | Buildings")
                .setDescription("Hier stehen Informationen zu Buildings und wie diese funktionieren")

            "prestige" -> EmbedBuilder()
                .setTitle("\uD83D\uDC8E | Prestige")
                .setDescription("Hier stehen Informationen zu Prestige und was das alles für Effekte hat")

            else -> EmbedBuilder()
                .setTitle("ERROR").setDescription("ERROR")
        }.setColor(0xEEA82D).build()
        it.reply(" ").setEphemeral(true)
            .addEmbeds(embed).queue()
    }
}