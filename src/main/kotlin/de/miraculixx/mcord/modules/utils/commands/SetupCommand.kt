package de.miraculixx.mcord.modules.utils.commands

import de.miraculixx.mcord.config.guildCache
import de.miraculixx.mcord.config.msg
import de.miraculixx.mcord.modules.games.UpdaterGame
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import de.miraculixx.mcord.utils.notify
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.interactions.components.buttons.Button

class SetupCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        when (it.subcommandName) {
            "help" -> it.reply("HELP")
            "channel" -> {
                val guild = it.guild ?: return
                it.deferReply().queue()
                val hook = it.hook
                val botPerms = listOf(
                    Permission.VIEW_CHANNEL,
                    Permission.CREATE_PUBLIC_THREADS,
                    Permission.CREATE_PRIVATE_THREADS,
                    Permission.MESSAGE_SEND,
                    Permission.MESSAGE_SEND_IN_THREADS,
                    Permission.MANAGE_WEBHOOKS,
                    Permission.MANAGE_THREADS,
                    Permission.MESSAGE_MANAGE
                )

                if (it.getOption("stats-channel") != null) {
                    val g = SQL.getGuild(guild.idLong)
                    if (g.premium) {
                        val target = it.getOption("stats-channel")?.asChannel as? MessageChannel
                        if (target == null) {
                            hook.editOriginal("```diff\n- Please select a valid channel. NOT a category```")
                            return
                        }
                        try {
                            if (target.getHistoryFromBeginning(10).complete().size() != 0) {
                                hook.editOriginal("```diff\n- This Channel has to much traffic! Please choose an empty Channel to setup```").queue()
                                return
                            }

                            //target.upsertPermissionOverride(it.jda.selfUser)
                            UpdaterGame.updateLeaderboardGuild(guild, target)
                            //target?.upsertPermissionOverride(it.jda.selfUser)
                            hook.editOriginal("**>> ERFOLG**\n${target.asMention} ist nun der Game Stats Channel!").queue()

                            SQL.call("UPDATE guildData SET Stats_Channel=${target.id} WHERE Discord_ID=${guild.id}")
                        } catch (e: InsufficientPermissionException) {
                            e.notify(hook)
                        }
                    } else {
                        hook.editOriginal("```diff\n- Your Guild does not own Premium!\n- Activate it in MCreate (Bots Master-Guild) or get it on our Webshop!```")
                            .setActionRow(
                                Button.link("https://discord.gg/VEcR8RbnSH", "MCreate").withEmoji(Emoji.fromFormatted("<:mutils:975780449903341579>")),
                                Button.link("https://miraculixx.de/mcreate/shop", "Webshop").withEmoji(Emoji.fromUnicode("\uD83D\uDED2"))
                            )
                            .queue()
                        return
                    }
                    if (it.getOption("game-channel") != null) {

                    }
                }
            }
            "language" -> {
                val guildID = it.guild?.idLong ?: return
                val language = it.getOption("lang")!!.asString
                val langKey = when (language) {
                    "German" -> "DE_DE"
                    "English" -> "EN_US"
                    else -> "Error"
                }
                SQL.call("UPDATE guildData SET lang='$langKey' WHERE Discord_ID=$guildID")
                guildCache[guildID] = langKey
                it.replyEmbeds(
                    Embed {
                        title = "\uD83C\uDF0D  **||  LANGUAGE SWITCHER**"
                        description = "```diff\n" +
                                "+ ${msg("systemLanguageSwitch", guildID)}```\n" +
                                "**New Language ~~⠀⠀>~~** `$language ($langKey)`\n" +
                                "<:blanc:784059217890770964>\n" +
                                msg("systemLanguageInfo", guildID)
                        color = 0xc99d11
                        footer {
                            name = "MGame-Club - Play games inside of Discord everywhere!"
                            iconUrl = "https://i.imgur.com/Im1QNQ9.png"
                        }
                    }
                )
            }
        }
    }
}