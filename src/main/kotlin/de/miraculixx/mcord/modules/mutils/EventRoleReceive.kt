package de.miraculixx.mcord.modules.mutils

import de.miraculixx.mcord.utils.KeyInfoDisplays
import de.miraculixx.mcord.utils.log
import de.miraculixx.mcord.utils.notifyRankAdd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventRoleReceive : ListenerAdapter() {
    override fun onGuildMemberRoleAdd(it: GuildMemberRoleAddEvent) {
        val jda = it.jda
        val guild = it.guild
        val member = it.member
        val displayRole = jda.getRoleById(909192161386430484)!!
        val added = it.roles.map { it.idLong }

        val rank = when {
            added.contains(987015931828002887) -> KeyInfoDisplays.Rank(0, "Unlimited", "never", 6)
            added.contains(987017400983646278) -> KeyInfoDisplays.Rank(0, "Lite", "never", 3)
            added.contains(938898054151561226) -> KeyInfoDisplays.Rank(0, "Subscriber", "never", 2)
            added.contains(973103811885432842) -> KeyInfoDisplays.Rank(0, "Booster", "never", 1)

            else -> return
        }
        CoroutineScope(Dispatchers.Default).launch {
            guild.addRoleToMember(UserSnowflake.fromId(member.id), displayRole).queue()
            ">> PREMIUM >> Add ${rank.type} to ${member.user.asTag}".log()
            notifyRankAdd(rank, jda, member.user)
        }
    }
}