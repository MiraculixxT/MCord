package de.miraculixx.mcord.commands

import de.miraculixx.mcord.Main
import de.miraculixx.mcord.commands.events.AdminCommand
import de.miraculixx.mcord.commands.events.LanguageCommand
import de.miraculixx.mcord.commands.events.MCTransferCommand
import de.miraculixx.mcord.commands.events.PremiumCommand
import de.miraculixx.mcord.utils.LateInit
import de.miraculixx.mcord.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

class SlashCommandManager : ListenerAdapter(), LateInit {

    private val commands = HashMap<String, SlashCommands>()

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
        commands["premium"] = PremiumCommand()
        commands["mc-info"] = MCTransferCommand()
        commands["admin"] = AdminCommand()
        val keys = PremiumCommand()
        commands["key-generate"] = keys
        commands["key-delete"] = keys
        commands["key-info"] = keys
        commands["key-update"] = keys

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
                    .addOption(OptionType.STRING, "rank","Which rank do you want to refresh? (Spamming this command leads into blacklist)", true, true),
                //Commands.slash("mc-info", "Enter a Minecraft UUID or Name and get full information about it")
                    //.addOption(OptionType.STRING, "uuid", "The UUID is created with the account and cannot be changed")
                    //.addOption(OptionType.STRING, "name", "The name can be changed at any time (with cooldown)"),
                Commands.slash("admin", "A Admin only command for testing")
                    .addOption(OptionType.STRING, "call", "Action to do", true, true)
                    .addOption(OptionType.BOOLEAN, "status", "Switch Online Status of MUtils")
            ).queue()
        community.updateCommands()
            .addCommands(
                Commands.slash("mc-info", "Gebe einen Nutzer Namen oder eine MC UUID ein, um Informationen über den Account zu bekommen")
                    .addOption(OptionType.STRING, "uuid", "Die UUID wird mit dem Account erstellt und kann nicht geändert werden")
                    .addOption(OptionType.STRING, "name", "Der Name kann jederzeit geändert werden (mit Cooldown)"),
                Commands.slash("admin", "A Admin only command for testing")
                    .addOption(OptionType.STRING, "call", "Action to do", true, true)
                    .addOption(OptionType.BOOLEAN, "status", "Switch Online Status of MUtils")
            ).queue()
        jda.updateCommands().queue()
    }
}