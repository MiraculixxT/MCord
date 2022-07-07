package de.miraculixx.mcord.utils.manager

import de.miraculixx.mcord.modules.mutils.CommandAccount
import de.miraculixx.mcord.modules.utils.commands.LanguageCommand
import de.miraculixx.mcord.utils.entities.LateInit
import de.miraculixx.mcord.utils.entities.SlashCommandEvent
import de.miraculixx.mcord.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

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
        val keys = CommandAccount()
        commands["key-generate"] = keys
        commands["key-delete"] = keys
        commands["key-info"] = keys
        commands["key-update"] = keys

        //Implement all Commands into Discord
        // - Moved to MCord-MUtils
    }
}