package de.miraculixx.mcord.dropdowns.events

import de.miraculixx.mcord.dropdowns.DropDowns
import de.miraculixx.mcord.utils.KeyInfoDisplays
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

class ServerManagerMenu : DropDowns {
    override suspend fun trigger(it: SelectMenuInteractionEvent) {
        val tool = KeyInfoDisplays(it.hook, it.jda)
        it.editMessage(tool.await).queue()

        val option = it.selectedOptions.first()
        val data = option.value.removePrefix("serverSelect_")
        val message = it.message
        val split = option.value.split('_')
        tool.serverInfo(message, split[1], split[2])
    }
}