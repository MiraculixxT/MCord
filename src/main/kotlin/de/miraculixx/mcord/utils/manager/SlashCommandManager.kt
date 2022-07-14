package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.Main
import de.miraculixx.mcord.modules.games.chess.ChessCommand
import de.miraculixx.mcord.modules.games.connectFour.C4Command
import de.miraculixx.mcord.modules.games.tictactoe.TTTCommand
import de.miraculixx.mcord.modules.system.AdminCommand
import de.miraculixx.mcord.modules.system.SetupCommand
import de.miraculixx.mcord.utils.entities.LateInit
import de.miraculixx.mcord.utils.log
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.subcommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType

object SlashCommandManager : ListenerAdapter(), LateInit {
    private val commands = mapOf(
        "tictactoe" to TTTCommand(),
        "connect-4" to C4Command(),
        "chess" to ChessCommand(),
        "setup" to SetupCommand(),
        "admin" to AdminCommand()
    )

    fun startListen(jda: JDA) = jda.listener<SlashCommandInteractionEvent> {
        val commandClass = commands[it.name] ?: return@listener
        ">> ${it.user.asTag} -> /${it.name} ${it.subcommandName}".log()
        commandClass.trigger(it)
    }

    init {
        //Implement all Commands into Discord
        val jda = Main.INSTANCE.jda
        val mainServer = jda?.getGuildById(707925156919771158)!!
        jda.updateCommands().addCommands(
            Command("tictactoe", "Play Tic-Tac-Toe against others") {
                subcommand("user", "Play Tic-Tac-Toe against an other User") {
                    addOption(OptionType.USER, "request", "Send a game request to your selected User")
                }
                subcommand("bot", "Play Tic-Tac-Toe against our AI") {
                    addOption(OptionType.STRING, "difficulty", "How good should the AI play?", true, true)
                }
            },
            Command("connect-4", "Play Connect-4 against others") {
                subcommand("user", "Play Connect 4 against an other User") {
                    addOption(OptionType.USER, "request", "Send a game request to your selected User")
                }
                subcommand("bot", "Play Connect 4 against our AI") {
                    addOption(OptionType.STRING, "difficulty", "How good should the AI play?", true, true)
                }
                subcommand("skin", "Choose a Skin for your Chip")
            },
            Command("chess", "Play Chess against others") {
                subcommand("user", "Play Chess against an other User") {
                    addOption(OptionType.USER, "request", "Send a game request to your selected User")
                }
                /*
                    subcommand("bot", "Play Chess against our AI") {
                        addOption(OptionType.STRING, "difficulty", "How good should the AI play?", true, true)
                    }
                    subcommand("skin", "Choose a Skin for your Chip")
                     */
            },

            Command("coins", "Inspect your personal stats") {
                addOption(OptionType.USER, "user", "Inspect the stats from an other User")
            },
            Command("setup", "Setup all bot settings to start gaming real quick") {
                subcommand("help", "All information about how to setup everything perfectly")
                subcommand("channel", "Create channels to play in with perfect settings") {
                    addOption(OptionType.CHANNEL, "stats-channel", "Setup current channel your stats channel? (PREMIUM ONLY)", false)
                    addOption(OptionType.CHANNEL, "game-channel", "Setup current channel to a Only-Gaming channel?", false)
                }
            }
        ).queue()
        mainServer.updateCommands()
            .addCommands(
                Command("admin", "Admin Command") {
                    isDefaultEnabled = false
                    subcommand("swap-daily", "Ändern der Täglichen Challenges")
                    subcommand("refresh-stats", "Erneuert die Stats")
                }
            ).queue()
    }
}