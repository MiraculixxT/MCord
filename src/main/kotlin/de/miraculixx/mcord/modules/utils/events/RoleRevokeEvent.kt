package de.miraculixx.mcord.modules.utils.events

import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class RoleRevokeEvent {
    fun startListen(jda: JDA) = jda.listener<GuildMemberRoleRemoveEvent> {
        println("role remove")
        val roles = it.roles.map { role -> role.idLong }
        val isReminder = roles.contains(1021514836871499826)
        if (roles.contains(895912578713260072) || isReminder) {
            val member = it.member
            if (!member.roles.map { role -> role.idLong }.contains(1021514836871499826) && !isReminder) return@listener
            println("remind sub - remove")
            val pm = member.user.openPrivateChannel().complete()
            pm.sendMessageEmbeds(
                Embed {
                    title = ":money_with_wings:  >> Dein freundlicher SUB-Reminder :)"
                    description = "Dein wunderbarer **SUB** bei Miraculixx (aka dem besten Streamer) ist gerade abgelaufen <:PepeHands:822394486818734080>\n" +
                            "\n" +
                            "Aber dass kannst du ganz schnell ändern mit nur einem (oder ein paar mehr) klicks!\n" +
                            "> Klicke einfach auf den **Button** unten"
                    url = "https://www.twitch.tv/subs/miraculixxt"
                    color = 0xca0000
                    thumbnail = "https://cdn.discordapp.com/emojis/1012025999279530054.gif?size=240&quality=lossless"
                }

            ).addComponents(ActionRow.of(button("https://www.twitch.tv/subs/miraculixxt", "Resub NOW", Emoji.fromFormatted("<:twitch:909185661716795433>"), ButtonStyle.LINK))).queue()
        }
    }

    fun startListen2(jda: JDA) = jda.listener<GuildMemberRoleAddEvent> {
        println("add role")
        val roles = it.roles.map { role -> role.idLong }
        if (roles.contains(895912578713260072) || roles.contains(1021514836871499826)) {
            val member = it.member
            if (!member.roles.map { role -> role.idLong }.contains(1021514836871499826)) return@listener
            println("remind sub - add")
            val pm = member.user.openPrivateChannel().complete()
            pm.sendMessageEmbeds(
                Embed {
                    title = ":money_with_wings:  >> Dein freundlicher SUB-Reminder :)"
                    description = "Dein **SUB** bei Miraculixx (aka dem besten Streamer) ist wieder aktiv mit allen Perks <a:heCrazy:872415705868402710>\n" +
                            "\n" +
                            "Du weißt es bestimmt schon, aber gerade jetzt wollen wir dir noch mal sagen, was für eine **tolle** Person du doch bist!"
                    color = 0x0ca206
                    thumbnail = "https://cdn.discordapp.com/emojis/935492895228702740.webp?size=240&quality=lossless"
                }
            ).queue()
        }
    }
}