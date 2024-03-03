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

        guildMCreate.updateCommands()
            .addCommands(
                Command("account", "Display and Manage your account settings and server connections") {
                    subcommand("create", "Generate a new account to activate your Premium and receive a Key") {
                        option<String>("minecraft_name", "Enter your Minecraft name. Only the given account can activate a key on servers", true)
                    }
                    subcommand("info", "Display and Manage your account settings and server connections")
                    subcommand("update", "Refresh your Rank Status to activate it on your Account") {
                        option<String>("rank", "Which rank do you want to refresh? (Spamming this command leads into blacklist)", true, true)
                    }
                    subcommand("email", "Change your Email - You agree to https://mutils.de/legal/privacy by entering") {
                        option<String>("email", "Your new email - You agree to https://mutils.de/legal/privacy by entering", true)
                    }
                },
                Commands.slash("admin", "A Admin only command for testing")
                    .addOption(OptionType.STRING, "call", "Action to do", true, true)
                    .addOption(OptionType.BOOLEAN, "status", "Switch Online Status of MUtils")
                    .addOption(OptionType.ROLE, "role", "Some role"),
                Command("message", "Utility command for messages") {
                    subcommand("add-link", "Add a link button to a message") {
                        addOption(OptionType.STRING, "msg-id", "Message ID", true)
                        addOption(OptionType.STRING, "link", "URL", true)
                        addOption(OptionType.STRING, "label", "Button Name", true)
                        addOption(OptionType.STRING, "emote", "Button Emote", true)
                    }
                    subcommand("embed-create", "Create a new Embed") {
                        addOption(OptionType.STRING, "code", "JSON code (Empty = Dummy)")
                        addOption(OptionType.STRING, "msg-id", "Message ID (Empty = New Message)")
                    }
                    subcommand("embed-edit", "Edit a existing Embed") {
                        addOption(OptionType.STRING, "msg-id", "Message ID", true)
                        addOption(OptionType.STRING, "title", "Title")
                        addOption(OptionType.STRING, "title-url", "Title URL")
                        addOption(OptionType.STRING, "description", "Description")
                        addOption(OptionType.STRING, "color", "Color")
                        addOption(OptionType.STRING, "footer", "Footer Text")
                        addOption(OptionType.STRING, "footer-icon", "Footer Icon URL")
                        addOption(OptionType.STRING, "code", "JSON Code")
                    }
                    subcommand("send", "Send a Message") {
                        addOption(OptionType.ROLE, "ping", "Ping a role")
                    }
                    subcommand("remove-buttons", "Remove all Buttons") {
                        addOption(OptionType.STRING, "msg-id", "Message ID", true)
                    }
                }
            ).queue()
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