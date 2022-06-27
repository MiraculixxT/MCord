package de.miraculixx.mcord.modules.games.fourWins

import de.miraculixx.mcord.modules.games.utils.GameTools
import de.miraculixx.mcord.modules.games.utils.Games
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.entities.ButtonEvent
import de.miraculixx.mcord.utils.entities.DropDownEvent
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

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
        when (val price = data[1]) {
            "FREE" -> applyEmote(emote, member, secondary)
            "BOOST" -> {
                if (member.isBoosting)
                    applyEmote(emote, member, secondary)
                else it.reply(
                    "```diff\n- Nur Server Booster können ihren Skin wechseln!\n+ Erhalte 2 Booster kostenlos mit Discord Nitro!\n\n" +
                            "+ Aktuell ist unser Ziel Boost Level 2 für private Threads und mehr Spielmöglichkeiten```"
                ).setEphemeral(true).queue()
            }
            else -> it.reply("```diff\n- Du hast leider nicht genug Coins um $emote freizuschalten!\n- Du benötigst $price Coins```").setEphemeral(true).queue()
        }
    }

    private fun applyEmote(emote: String, member: Member, secondary: Boolean) {

        //SQL.setUserEmote(SQL.UserEmote(member.idLong, emote))
    }
}