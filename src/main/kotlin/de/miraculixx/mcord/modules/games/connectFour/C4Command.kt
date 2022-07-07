package de.miraculixx.mcord.modules.games.connectFour

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.Games
import de.miraculixx.mcord.modules.games.utils.SkinType
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption

class C4Command : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val subcommand = it.subcommandName ?: return
        if (subcommand == "skin") {
            //Building default skin dropdowns
            val conf = ConfigManager.getConfig(Configs.GAME_SETTINGS)
            val primary = SelectMenu.create("GAME_4G_SKIN_1")
            val secondary = SelectMenu.create("GAME_4G_SKIN_2")
            primary.placeholder = "Primary Chip Skin"
            secondary.placeholder = "Secondary Chip Skin"
            primary.minValues = 1
            secondary.minValues = 1
            primary.maxValues = 1
            secondary.maxValues = 1
            primary.addOptions(addEmote("\uD83D\uDFE1", SkinType.FREE, false))
            secondary.addOptions(addEmote("\uD83D\uDD34", SkinType.FREE, false))

            //Filter already bought emotes
            val member = it.member ?: return
            val userEmotes = SQL.getUser(member.idLong, true, false).emotes!!
            val primaryEmotes = userEmotes.owned.filter { it.key == "C4_Primary" }.values
            val secEmotes = userEmotes.owned.filter { it.key == "C4_Secondary" }.values
            conf.getObjectList<Int>("Connect4-RawEmotes").forEach { (emote, price) ->
                primary.addOptions(
                    if (primaryEmotes.contains(emote))
                        if (userEmotes.c4 == emote)
                            addEmote(emote, SkinType.SELECTED, true)
                        else addEmote(emote, SkinType.BOUGHT, true)
                    else addEmote(emote, SkinType.COINS, true, price)
                )
                secondary.addOptions(
                    if (secEmotes.contains(emote))
                        if (userEmotes.c42 == emote)
                            addEmote(emote, SkinType.SELECTED, true)
                        else addEmote(emote, SkinType.BOUGHT, true)
                    else addEmote(emote, SkinType.COINS, true, price)
                )
            }
            conf.getStringList("Connect4-SpecialEmotes").forEach { emote ->
                if (userEmotes.c4 == emote)
                    primary.addOptions(addEmote(emote, SkinType.SELECTED, false))
                else primary.addOptions(addEmote(emote, SkinType.BOOST, false))
                if (userEmotes.c42 == emote)
                    secondary.addOptions(addEmote(emote, SkinType.SELECTED, false))
                else secondary.addOptions(addEmote(emote, SkinType.BOOST, false))
            }

            //Building Message
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

    private fun addEmote(emote: String, type: SkinType, unicode: Boolean, price: Int = 0): SelectOption {
        val emoji = if (unicode) Emoji.fromUnicode(emote) else Emoji.fromMarkdown(emote)
        return when (type) {
            SkinType.FREE, SkinType.BOUGHT -> SelectOption.of("Unlocked", "${emote}_FREE").withEmoji(emoji)
            SkinType.BOOST -> SelectOption.of("Server Booster Skin", "${emote}_BOOST").withEmoji(emoji)
            SkinType.COINS -> SelectOption.of("Unlock Price >> $price", "${emote}_$price").withEmoji(emoji)
            SkinType.SELECTED -> SelectOption.of(">> CURRENT SKIN <<", "${emote}_SELECTED").withEmoji(emoji)
        }
    }
}