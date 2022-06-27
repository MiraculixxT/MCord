package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.Main
import de.miraculixx.mcord.modules.games.chess.ChessCommand
import de.miraculixx.mcord.modules.games.fourWins.C4Command
import de.miraculixx.mcord.modules.games.tictactoe.TTTCommand
import de.miraculixx.mcord.modules.mutils.CommandAccount
import de.miraculixx.mcord.modules.utils.commands.AdminCommand
import de.miraculixx.mcord.modules.utils.commands.LanguageCommand
import de.miraculixx.mcord.modules.utils.commands.MCTransferCommand
import de.miraculixx.mcord.utils.entities.LateInit
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import de.miraculixx.mcord.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class SlashCommandManager : ListenerAdapter(), LateInit {

    private val commands = HashMap<String, SlashCommandEvent>()

    override fun onSlashCommandInteraction(it: SlashCommandInteractionEvent) {
        val commandClass = commands[it.name] ?: return
        ">> ${it.user.asTag} -> /${it.name}".log()
        CoroutineScope(Dispatchers.Default).launch {
            commandClass.trigger(it)
        }
    }

    override fun setup() {
        //Implement all Command Events
        commands["language"] = LanguageCommand()
        commands["premium"] = CommandAccount()
        commands["mc-info"] = MCTransferCommand()
        commands["admin"] = AdminCommand()
        val keys = CommandAccount()
        commands["key-generate"] = keys
        commands["key-delete"] = keys
        commands["key-info"] = keys
        commands["key-update"] = keys
        commands["tictactoe"] = TTTCommand()
        commands["connect-4"] = C4Command()
        commands["chess"] = ChessCommand()

        //Implement all Commands into Discord
        val jda = Main.INSTANCE.jda!!
        val mcreate = jda.getGuildById(908621996009619477)!!
        val community = jda.getGuildById(707925156919771158)!!

        mcreate.updateCommands()
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
                    .addOption(OptionType.BOOLEAN, "status", "Switch Online Status of MUtils")
            ).queue()
        community.updateCommands()
            .addCommands(
                Commands.slash("admin", "A Admin only command for testing")
                    .addOption(OptionType.STRING, "call", "Action to do", true, true)
                    .addOption(OptionType.BOOLEAN, "status", "Switch Online Status of MUtils"),

                Commands.slash("tictactoe", "Spiele Tic-Tac-Toe gegen einen anderen Nutzer")
                    .addSubcommands(
                        SubcommandData("user", "Spiele Tic-Tac-Toe gegen einen anderen Nutzer")
                            .addOption(OptionType.USER, "request", "Sende eine Anfrage an einen genauen Nutzer")
                    ),
                Commands.slash("connect-4", "Spiele 4 Gewinnt gegen andere oder eine AI")
                    .addSubcommands(
                        SubcommandData("user", "Spiele 4 Gewinnt gegen einen anderen Nutzer")
                            .addOption(OptionType.USER, "request", "Sende eine Anfrage an einen genauen Nutzer"),
                        SubcommandData("bot", "Spiele alleine gegen die Bot AI"),
                        SubcommandData("skin", "Wähle einen Skin für deinen Spielchip")
                    ),
                Commands.slash("chess", "Spiele Schach gegen einen anderen Nutzer")
                    .addSubcommands(
                        SubcommandData("user", "Spiele Schach gegen einen anderen Nutzer")
                            .addOption(OptionType.USER, "request", "Sende eine Anfrage an einen genauen Nutzer")
                    ),
            ).queue()
        jda.updateCommands().queue()
    }
}