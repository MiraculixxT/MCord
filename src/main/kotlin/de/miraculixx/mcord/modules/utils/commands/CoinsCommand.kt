package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.config.msg
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.dailyGoals
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import dev.minn.jda.ktx.messages.Embed
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CoinsCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val ownStats = it.getOption("user") == null
        val member = if (ownStats) it.member ?: return
            else it.getOption("user")!!.asMember ?: return
        val guild = it.guild ?: return
        val guildID = guild.idLong
        val userData = SQL.getUser(member.idLong, guild.idLong, daily = true)
        val dailyData = userData.daily
        if (dailyData == null) {
            it.reply("```diff\n- Wir konnten leider keine Daten über den Account finden :(```").setEphemeral(true).queue()
            return
        }
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timestamp = Clock.System.now().minus(date.minute.minutes)
            .minus(date.second.seconds).plus((25 - date.hour).hours).epochSeconds

        val defaultEmbed = Embed {
            color = 0xd39526
            title = "<:mcoin:996386525208117258> ~~M~~-**COINS** - `${userData.coins}`"
            description = if (ownStats) "<:blanc:784059217890770964> <:blanc:784059217890770964>" else "<:blanc:784059217890770964> **↳** `Miraculixx#1234` (<@341998118574751745>)\n" +
                    "<:blanc:784059217890770964> **↳** `Booster Rank` ${if (member.isBoosting) "<:yes:998195646467145751>" else "<:no:998195603324551323>"}"
            if (ownStats) {
                field {
                    name = "\uD83C\uDFAF  ||  DAILY CHALLENGES"
                    value = "```diff\n" +
                            "${if (dailyData.c1) "+" else "-"} ${msg(dailyGoals?.get(0)?.name, guildID)}\n" +
                            "${if (dailyData.c2) "+" else "-"} ${msg(dailyGoals?.get(1)?.name, guildID)}\n" +
                            "${if (dailyData.c3) "+" else "-"} ${msg(dailyGoals?.get(2)?.name, guildID)}\n" +
                            "```\n" +
                            "> New Challenges <t:$timestamp:R>" +
                            if (!member.isBoosting) "\n*Boost to Unlock Bonus Rewards*" else ""
                }
                field {
                    name = "**REWARDS**"
                    value = "```ini\n" +
                            "${if (dailyData.c1) "]" else "["}=> ${dailyGoals?.get(0)?.reward}\n" +
                            "${if (dailyData.c1) "]" else "["}=> ${dailyGoals?.get(1)?.reward}\n" +
                            "${if (dailyData.c1) "]" else "["}=> ${dailyGoals?.get(2)?.reward}\n" +
                            "```"
                }
            }
        }

        if (member.isBoosting && ownStats)
            it.replyEmbeds(defaultEmbed, Embed {
                color = 0xb026d3
                field {
                    name = "<:booster:981486698431127602> ||  BONUS CHALLENGE"
                    value = "```diff\n" +
                            "${if (dailyData.bonus) "+" else "-"} ${msg(dailyGoals?.get(3)?.name, guildID)}\n" +
                            "```"
                }
                field {
                    name = "**REWARDS**"
                    value = "```ini\n" +
                            "[=> ${dailyGoals?.get(3)?.reward}\n" +
                            "```"
                }
            }).queue()
        else it.replyEmbeds(defaultEmbed).queue()
    }
}