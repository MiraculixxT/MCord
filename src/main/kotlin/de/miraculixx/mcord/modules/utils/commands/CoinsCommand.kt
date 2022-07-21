package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.dailyChallenges
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
        val member = if (it.getOption("user") != null) it.getOption("user")!!.asMember ?: return
            else it.member ?: return
        val guild = it.guild ?: return
        val userData = SQL.getUser(member.idLong, guild.idLong, daily = true)
        val dailyData = userData.daily
        if (dailyData == null) {
            it.reply("```diff\n- Wir konnten leider keine Daten über dich finden :(```").setEphemeral(true).queue()
            return
        }
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timestamp = Clock.System.now().minus(date.minute.minutes)
            .minus(date.second.seconds).plus((25 - date.hour).hours).epochSeconds

        val defaultEmbed = Embed {
            color = 0xd39526
            title = "<:mcoin:996386525208117258> ~~M~~-**COINS** - `${userData.coins}`"
            description = "<:blanc:784059217890770964> <:blanc:784059217890770964>"
            field {
                name = "\uD83C\uDFAF  ||  DAILY CHALLENGES"
                value = "```diff\n" +
                        "${if (dailyData.c1) "+" else "-"} ${dailyChallenges?.get(0)?.name}\n" +
                        "${if (dailyData.c2) "+" else "-"} ${dailyChallenges?.get(1)?.name}\n" +
                        "${if (dailyData.c3) "+" else "-"} ${dailyChallenges?.get(2)?.name}\n" +
                        "```\n" +
                        "> New Challenges <t:$timestamp:R>" +
                        if (!member.isBoosting) "\n*Boost to Unlock Bonus Rewards*" else ""
            }
            field {
                name = "**REWARDS**"
                value = "```ini\n" +
                        "[=> ${dailyChallenges?.get(0)?.reward}\n" +
                        "[=> ${dailyChallenges?.get(1)?.reward}\n" +
                        "[=> ${dailyChallenges?.get(2)?.reward}\n" +
                        "```"
            }
        }

        if (member.isBoosting)
            it.replyEmbeds(defaultEmbed, Embed {
                color = 0xb026d3
                field {
                    name = "<:booster:981486698431127602> ||  BONUS CHALLENGE"
                    value = "```diff\n" +
                            "${if (dailyData.bonus) "+" else "-"} ${dailyChallenges?.get(3)?.name}\n" +
                            "```"
                }
                field {
                    name = "**REWARDS**"
                    value = "```ini\n" +
                            "[=> ${dailyChallenges?.get(3)?.reward}\n" +
                            "```"
                }
            }).queue()
        else it.replyEmbeds(defaultEmbed).queue()
    }
}