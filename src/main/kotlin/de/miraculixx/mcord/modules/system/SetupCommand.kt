package de.miraculixx.mcord.modules.system

import de.miraculixx.mcord.modules.games.UpdaterGame
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import de.miraculixx.mcord.utils.notify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.interactions.components.buttons.Button

class SetupCommand : SlashCommandEvent {
    override suspend fun trigger(it: SlashCommandInteractionEvent) {
        val subName = it.subcommandName
        if (subName == "help")
            it.reply("HELP")
        else {
            val guild = it.guild ?: return
            it.deferReply().queue()
            val hook = it.hook

            CoroutineScope(Dispatchers.Default).launch {
                val botPerms = listOf(
                    Permission.VIEW_CHANNEL,
                    Permission.MESSAGE_SEND,
                    Permission.MESSAGE_SEND_IN_THREADS,
                    Permission.MANAGE_CHANNEL,
                    Permission.MANAGE_THREADS,
                    Permission.MANAGE_WEBHOOKS,
                    Permission.CREATE_PRIVATE_THREADS,
                    Permission.CREATE_PUBLIC_THREADS
                )
                if (it.getOption("stats-channel") != null) {
                    val g = SQL.getGuild(guild.idLong)
                    if (g.premium) {
                        val target = it.getOption("stats-channel")!!.asTextChannel
                        if (target == null) {
                            hook.editOriginal("```diff\n- Please select a valid channel. NOT a category```")
                            return@launch
                        }
                        try {
                            if (target.getHistoryFromBeginning(10).complete().size() != 0) {
                                hook.editOriginal("```diff\n- This Channel has to much traffic! Please choose an empty Channel to setup```").queue()
                                return@launch
                            }

                            target.upsertPermissionOverride(guild.publicRole).setDenied(Permission.ALL_TEXT_PERMISSIONS)
                                .setAllowed(Permission.VIEW_CHANNEL).queue()
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
                        return@launch
                    }
                }
                //if (it.getOption("game-channel") != null)
                //category.createTextChannel("\uD83C\uDFAE╏tic-tac-toe").complete()

            }
        }
    }
}