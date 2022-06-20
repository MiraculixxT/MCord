package de.miraculixx.mcord.utils

import de.miraculixx.mcord.Main
import net.dv8tion.jda.api.entities.Message

val messageCache = HashMap<String, Message>()

val guildMiraculixx = Main.INSTANCE.jda?.getGuildById(707925156919771158)!!
val guildMCreate = Main.INSTANCE.jda?.getGuildById(908621996009619477)!!