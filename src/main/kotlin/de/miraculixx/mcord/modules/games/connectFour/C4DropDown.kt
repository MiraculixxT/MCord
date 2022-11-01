package de.miraculixx.mcord.modules.games.connectFour

import de.miraculixx.mcord.modules.games.GoalManager
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.entities.DropDownEvent
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class C4DropDown : DropDownEvent {
    override suspend fun trigger(it: GenericSelectMenuInteractionEvent<String, StringSelectMenu>) {
        val member = it.member ?: return
        val secondary = it.selectMenu.id?.split('_')?.get(3) == "2"
        val data = it.values.first().split('_')
        val emote = data[0]
        val guildID = it.guild?.idLong ?: return

        it.deferReply(true).queue()
        val hook = it.hook

        when (val price = data[1]) {
            "FREE" -> applyEmote(emote, member, guildID, secondary, hook, null)
            "BOOST" -> {
                if (member.isBoosting)
                    applyEmote(emote, member, guildID, secondary, hook, SQL.getUser(member.idLong, guildID, emotes = true).emotes)
                else hook.editOriginal(
                    "```diff\n- Nur Server Booster können ihren Skin wechseln!\n+ Erhalte 2 Booster kostenlos mit Discord Nitro!\n\n" +
                            "+ Aktuell ist unser Ziel Boost Level 2 für private Threads und mehr Spielmöglichkeiten```"
                ).queue()
            }

            "SELECTED" -> hook.editOriginal("```diff\n- Du nutzt bereits diesen Skin!```")
            else -> {
                val id = member.idLong
                val user = SQL.getUser(member.idLong, guildID, emotes = true)
                val type = if (secondary) "C4_S" else "C4_P"
                if (user.emotes!!.owned.filter { it.key == type }.containsValue(emote))
                    applyEmote(emote, member, guildID, secondary, hook, user.emotes)

                // User do not own the chosen Emote
                val priceInt = price.toIntOrNull() ?: 0
                if (user.coins < priceInt)
                    hook.editOriginal("```diff\n- Du hast leider nicht genug Coins um $emote freizuschalten!\n- Du benötigst noch ${priceInt - user.coins} Coins```").queue()
                else {
                    SQL.setUserCoins(id, guildID, user.coins - priceInt)
                    SQL.addEmote(id, guildID, type, emote)
                    applyEmote(emote, member, guildID, secondary, hook, user.emotes)
                }
            }
        }
    }

    private suspend fun applyEmote(emote: String, member: Member, guildID: Long, secondary: Boolean, hook: InteractionHook, emoteData: SQL.UserEmote?) {
        val id = member.idLong
        val other = if (!secondary) emoteData?.c42 else emoteData?.c4
        if (other == emote) {
            hook.editOriginal("```diff\n- Du kannst nicht den selben Skin zweimal auswählen!```").queue()
            return
        }
        SQL.setActiveEmote(
            id, guildID,
            if (secondary) "C4_S" else "C4_P",
            emote
        )
        GoalManager.registerSkinChange(Game.CONNECT_4, id, guildID)
        hook.editOriginal("✅ **|| Skin Erfolgreich Geändert**\nDu nutzt nun $emote als 4 Gewinnt Spielstein").queue()
    }
}