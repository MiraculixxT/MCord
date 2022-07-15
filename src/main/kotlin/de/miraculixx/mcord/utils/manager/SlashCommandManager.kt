package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.INSTANCE
import de.miraculixx.mcord.modules.utils.commands.AdminCommand
import de.miraculixx.mcord.modules.utils.commands.LetMeGoogleCommand
import de.miraculixx.mcord.modules.utils.commands.MessagesCommand
import de.miraculixx.mcord.utils.guildMCreate
import de.miraculixx.mcord.utils.guildMiraculixx
import de.miraculixx.mcord.utils.log
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
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
        ">> ${it.user.asTag} -> /${it.name}".log()
        commandClass.trigger(it)
    }

    init {
        //Implement all Commands into Discord
        val jda = INSTANCE.jda

        guildMCreate.updateCommands()
            .addCommands(
                Commands.slash("language", "Change your personal MCreate Server language")
                    .addOption(OptionType.STRING, "language", "Choose your new language", true, true),
                Commands.slash("key-generate", "Generate a new account to activate your Premium and receive a Key")
                    .addOption(OptionType.STRING, "minecraft_name", "Enter your Minecraft name. Only the given account can activate a key on servers", true),
                Commands.slash("key-delete", "Delete all your Account data and Licences. WARNING!"),
                Commands.slash("key-info", "Display and Manage your account settings and server connections"),
                Commands.slash("key-update", "Refresh your MUtils Unlimited, Twitch Sub and Boost Status to activate it on your Account")
                    .addOption(OptionType.STRING, "rank", "Which rank do you want to refresh? (Spamming this command leads into blacklist)", true, true),
                Commands.slash("admin", "A Admin only command for testing")
                    .addOption(OptionType.STRING, "call", "Action to do", true, true)
                    .addOption(OptionType.BOOLEAN, "status", "Switch Online Status of MUtils"),
                Command("message", "Utility command for messages") {
                    subcommand("add-link", "Add a link button to a message") {
                        addOption(OptionType.STRING, "msg-id", "Message ID", true)
                        addOption(OptionType.STRING, "link", "URL", true)
                        addOption(OptionType.STRING, "label", "Button Name", true)
                        addOption(OptionType.STRING, "emote", "Button Emote", true)
                    }
                    subcommand("create-embed", "Create a new Embed")  {
                        addOption(OptionType.STRING, "code", "JSON code (Empty = Dummy)")
                        addOption(OptionType.STRING, "msg-id", "Message ID (Empty = New Message)")
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
                }
            ).queue()
        jda.updateCommands().queue()
    }
}