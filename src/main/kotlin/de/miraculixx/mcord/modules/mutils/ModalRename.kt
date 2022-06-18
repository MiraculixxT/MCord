package de.miraculixx.mcord.modules.mutils

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.utils.KeyInfoDisplays
import de.miraculixx.mcord.utils.api.API
import de.miraculixx.mcord.utils.api.callAPI
import de.miraculixx.mcord.utils.entities.ModalEvent
import de.miraculixx.mcord.utils.messageCache
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

class ModalRename : ModalEvent {
    override suspend fun trigger(it: ModalInteractionEvent) {
        val tool = KeyInfoDisplays(it.hook, it.jda)
        val dcID = it.user.id
        val message = messageCache[dcID]
        if (message == null) {
            it.reply("```diff\n- Unable to find source message.\n- Maybe you took to long for Discord?```").setEphemeral(true).queue()
            return
        }
        messageCache.remove(dcID)

        val id = it.modalId.split('_')
        val content = it.values.first().asString.replace(' ', '_')
        if (!"[a-zA-Z\\d-_]+".toRegex().matches(content)) {
            delay(2000)
            it.reply("```diff\n- Der angegebene Name '$content' enthält verbotene Zeichen!\n- Es sind nur folgende Zeichen erlaubt -> A-Z a-Z 0-9 _ -```").setEphemeral(true).queue()
            return
        }
        it.editMessage(tool.await).queue()

        callAPI(API.MUTILS, "admin.php?call=updateconnection&pw=${ConfigManager.apiKey}&id=${id[1]}&ip=${id[2]}&name=$content")
        tool.serverInfo(message, id[1], id[2], true, dcID)
        println("${it.user.asTag} -> RENAME - ${id[2]} - $content")
    }
}