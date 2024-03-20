package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.INSTANCE
import de.miraculixx.mcord.modules.utils.commands.AdminCommand
import de.miraculixx.mcord.modules.utils.commands.LetMeGoogleCommand
import de.miraculixx.mcord.modules.utils.commands.MessagesCommand
import de.miraculixx.mcord.utils.guildMCreate
import de.miraculixx.mcord.utils.guildMiraculixx
import de.miraculixx.mcord.utils.log.log
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

object SlashCommandManager {
    private val commands = mapOf(
        "admin" to AdminCommand(),
        "message" to MessagesCommand(),
        "letmegoogle" to LetMeGoogleCommand()
    )

    fun startListen(jda: JDA) = jda.listener<SlashCommandInteractionEvent> {
        val commandClass = commands[it.name] ?: return@listener
        ">> ${it.user.asTag} -> /${it.name} ${it.subcommandName ?: ""}".log()
        commandClass.trigger(it)
    }

    init {
        //Implement all Commands into Discord
        val jda = INSTANCE.jda

        guildMiraculixx.updateCommands()
            .addCommands(
                Commands.slash("admin", "A Admin only command for testing")
                    .addOption(OptionType.STRING, "call", "Action to do", true, true)
                    .addOption(OptionType.BOOLEAN, "status", "Switch Online Status of MUtils"),
                Command("letmegoogle", "Create a let-me-google-that link for retarded people") {
                    addOption(OptionType.STRING, "search", "What should we search for", true)
                    addOption(OptionType.USER, "ping", "Who is the retarded person here?")
                },
                Command("message", "Utility command for messages") {
                    subcommand("send", "Send a Message") {
                        addOption(OptionType.ROLE, "ping", "Ping a role")
                    }
                }
            ).queue()
        jda.updateCommands().queue()
    }
}