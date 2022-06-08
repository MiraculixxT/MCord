package de.miraculixx.mcord.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun Member.getLanguage(guild: Guild): Messages {
    val german = guild.getRoleById(909154807246385192)
    if (this.roles.contains(german)) return Messages.GERMAN
    val english = guild.getRoleById(909155037727559710)
    if (this.roles.contains(english)) return Messages.ENGLISH
    return Messages.ENGLISH
}

fun msg(path: String, member: Member): String {
    val final = when (member.getLanguage(member.guild)) {
        Messages.GERMAN -> german[path]
        Messages.ENGLISH -> english[path]
    }
    return final ?: "```diff\n- $path```"
}

enum class Messages {
    GERMAN,
    ENGLISH
}

fun String.log() {
    val date = LocalDate.now()
    val time = LocalTime.now()
    println("[$date ${time.hour}:${time.minute}:${time.second}] $this")
}