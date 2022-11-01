package de.miraculixx.mcord.modules.games.connectFour

import de.miraculixx.mcord.config.Config
import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.modules.games.utils.enums.SkinType
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectOption

class C4Command : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val subcommand = it.subcommandName ?: return
        if (subcommand == "skin") {
            //Filter already bought emotes
            val member = it.member ?: return
            val guildID = it.guild?.idLong ?: return
            val userEmotes = SQL.getUser(member.idLong, guildID, emotes = true).emotes!!
            val primaryEmotes = userEmotes.owned.filter { it.key == "C4_P" }.values
            val secEmotes = userEmotes.owned.filter { it.key == "C4_S" }.values

            //Building default skin dropdowns
            val conf = ConfigManager.getConfig(Configs.GAME_SETTINGS)
            val primary = StringSelectMenu("GAME_C4_SKIN_1") {
                placeholder = "Primary Chip Skin"
                minValues = 1
                maxValues = 1
                addOptions(addEmote("\uD83D\uDFE1", SkinType.FREE))
                addOptions(addEmotes(conf, primaryEmotes, userEmotes.c4))
            }
            val secondary = StringSelectMenu("GAME_C4_SKIN_2") {
                placeholder = "Secondary Chip Skin"
                minValues = 1
                maxValues = 1
                addOptions(addEmote("\uD83D\uDD34", SkinType.FREE))
                addOptions(addEmotes(conf, secEmotes, userEmotes.c42))
            }

            //Building Message
            it.replyEmbeds(
                Embed {
                    color = 0xFF69F2
                    title = "\uD83C\uDF1F || SKIN CHANGER"
                    description = "Setze dir einen exklusiven Chip Skin für das Spiel **4 Gewinnt**!\n" +
                            "> - Primary `->` Dein Hauptskin. Wird in den meisten Fällen verwendet\n" +
                            "> - Secondary `->` Dein Ausweichskin. Wird verwendet, wenn dein Herausforderer den selben Skin nutzt"
                }
            ).setComponents(ActionRow.of(primary), ActionRow.of(secondary))
                .setEphemeral(true).queue()
            return
        }
        val tools = GameTools("4G", "4 Gewinnt", Game.CONNECT_4)
        tools.command(it)
    }

    private fun addEmote(emote: String, type: SkinType, price: Int = 0): SelectOption {
        val emoji = Emoji.fromFormatted(emote)
        return when (type) {
            SkinType.FREE, SkinType.BOUGHT -> SelectOption.of("Unlocked", "${emote}_FREE").withEmoji(emoji)
            SkinType.BOOST -> SelectOption.of("Server Booster Skin", "${emote}_BOOST").withEmoji(emoji)
            SkinType.COINS -> SelectOption.of("Unlock Price >> $price", "${emote}_$price").withEmoji(emoji)
            SkinType.SELECTED -> SelectOption.of(">> CURRENT SKIN <<", "${emote}_SELECTED").withEmoji(emoji)
        }
    }

    private fun addEmotes(config: Config, list: Collection<String>, current: String): List<SelectOption> {
        return buildList {
            config.getObjectList<Int>("Connect4-RawEmotes").forEach { (emote, price) ->
                if (list.contains(emote))
                    if (current == emote)
                        add(addEmote(emote, SkinType.SELECTED))
                    else add(addEmote(emote, SkinType.BOUGHT))
                else add(addEmote(emote, SkinType.COINS, price))
            }
            config.getStringList("Connect4-SpecialEmotes").forEach { emote ->
                if (current == emote)
                    add(addEmote(emote, SkinType.SELECTED))
                else add(addEmote(emote, SkinType.BOOST))
            }
        }
    }
}