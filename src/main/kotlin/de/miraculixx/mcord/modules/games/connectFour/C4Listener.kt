@file:Suppress("BooleanLiteralArgument")

package de.miraculixx.mcord.modules.games.connectFour

import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.Games
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.entities.ButtonEvent
import de.miraculixx.mcord.utils.entities.DropDownEvent
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook

class C4Listener : ButtonEvent, DropDownEvent {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val tools = GameTools("4G", "4 Gewinnt", Games.FOUR_WINS)
        tools.buttons(it)
    }

    override suspend fun trigger(it: SelectMenuInteractionEvent) {
        val member = it.member ?: return
        val secondary = it.selectMenu.id?.split('_')?.get(3) == "2"
        val data = it.selectedOptions.first().value.split('_')
        val emote = data[0]

        it.deferReply(true)
        val hook = it.hook

        when (val price = data[1]) {
            "FREE" -> applyEmote(emote, member, secondary, hook, null)
            "BOOST" -> {
                if (member.isBoosting)
                    applyEmote(emote, member, secondary, hook, SQL.getUser(member.idLong, true, false).emotes)
                else hook.editOriginal(
                    "```diff\n- Nur Server Booster können ihren Skin wechseln!\n+ Erhalte 2 Booster kostenlos mit Discord Nitro!\n\n" +
                            "+ Aktuell ist unser Ziel Boost Level 2 für private Threads und mehr Spielmöglichkeiten```"
                ).queue()
            }
            else -> {
                val id = member.idLong
                val user = SQL.getUser(member.idLong, true, false)
                val type = if (secondary) "C4_Secondary" else "C4_Primary"
                if (user.emotes!!.owned.filter { it.key == type }.containsValue(emote))
                    applyEmote(emote, member, secondary, hook, user.emotes)

                // User do not own the chosen Emote
                val priceInt = price.toIntOrNull() ?: 0
                if (user.coins < priceInt)
                    hook.editOriginal("```diff\n- Du hast leider nicht genug Coins um $emote freizuschalten!\n- Du benötigst noch ${priceInt - user.coins} Coins```").queue()
                else {
                    SQL.setUserCoins(id, user.coins - priceInt)
                    SQL.addEmote(id, type, emote)
                    applyEmote(emote, member, secondary, hook, user.emotes)
                }
            }
        }
    }

    private fun applyEmote(emote: String, member: Member, secondary: Boolean, hook: InteractionHook, emoteData: SQL.UserEmote?) {
        val id = member.idLong
        val other = if (!secondary) emoteData?.c42 else emoteData?.c4
        if (other == emote) {
            hook.editOriginal("```diff\n- Du kannst nicht den selben Skin zweimal auswählen!```").queue()
            return
        }
        val success = SQL.setActiveEmote(
            id,
            if (secondary) "C4_Secondary" else "C4_Primary",
            emote
        )
        if (success) hook.editOriginal("✅ **|| Skin Erfolgreich Geändert**\nDu nutzt nun $emote als 4 Gewinnt Spielstein").queue()
    }
}