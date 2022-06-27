package de.miraculixx.mcord.modules.games.fourWins

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.Games
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu

class C4Command : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val subcommand = it.subcommandName ?: return
        if (subcommand == "skin") {
            val conf = ConfigManager.getConfig(Configs.GAME_SETTINGS)
            val primary = SelectMenu.create("GAME_4G_SKIN_1")
            val secondary = SelectMenu.create("GAME_4G_SKIN_2")
            primary.placeholder = "Primary Chip Skin"
            secondary.placeholder = "Secondary Chip Skin"
            primary.minValues = 1
            secondary.minValues = 1
            primary.maxValues = 1
            secondary.maxValues = 1
            primary.addOption("Default Skin", "\uD83D\uDFE1_FREE", Emoji.fromMarkdown("\uD83D\uDFE1"))
            secondary.addOption("Default Skin", "🔴_FREE", Emoji.fromMarkdown("🔴"))
            conf.getObjectList<Int>("Connect4-RawEmotes").forEach { (emote, price) ->
                val emoji = Emoji.fromUnicode(emote)
                primary.addOption("Price >> $price", "${emote}_$price", emoji)
                secondary.addOption("Price >> $price", "${emote}_$price", emoji)
            }
            conf.getStringList("Connect4-SpecialEmotes").forEach {
                val emoji = Emoji.fromMarkdown(it)
                primary.addOption("Server Booster Skin", "${it}_BOOST", emoji)
                secondary.addOption("Server Booster Skin", "${it}_BOOST", emoji)
            }
            it.replyEmbeds(
                EmbedBuilder().setColor(0xFF69F2)
                    .setTitle("\uD83C\uDF1F || SKIN CHANGER")
                    .setDescription(
                        "Setze dir einen exklusiven Chip Skin für das Spiel **4 Gewinnt**!\n" +
                                "> - Primary `->` Dein Hauptskin. Wird in den meisten Fällen verwendet\n" +
                                "> - Secondary `->` Dein Ausweichskin. Wird verwendet, wenn dein Herausforderer den selben Skin nutzt"
                    ).build()
            ).addActionRows(ActionRow.of(primary.build()), ActionRow.of(secondary.build()))
                .setEphemeral(true).queue()
            return
        }
        val tools = GameTools("4G", "4 Gewinnt", Games.FOUR_WINS)
        tools.command(it)
    }
}