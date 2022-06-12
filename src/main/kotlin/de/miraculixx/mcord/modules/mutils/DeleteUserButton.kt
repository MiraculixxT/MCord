package de.miraculixx.mcord.modules.mutils

import de.miraculixx.mcord.config.Config
import de.miraculixx.mcord.utils.api.API
import de.miraculixx.mcord.utils.api.callAPI
import de.miraculixx.mcord.utils.entities.Buttons
import de.miraculixx.mcord.utils.log
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class DeleteUserButton: Buttons {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val id = it.button.id ?: return
        val split = id.split('_')
        val userID = split[1]
        val userKey = split[2]

        callAPI(API.MUTILS, "admin.php?call=deleteuser&pw=${Config.API_KEY}&id=$userID&key=$userKey")
        it.editButton(it.button.asDisabled()).queue()
        val hook = it.hook
        hook.editOriginal("**Account Daten gelöscht**\nSchade dass du uns verlässt <:Sadge:820962368166428683> Die Zeit mit dir war schön...").queue()
        ">> Account Deleted -> ${it.user.asTag}".log()
    }
}