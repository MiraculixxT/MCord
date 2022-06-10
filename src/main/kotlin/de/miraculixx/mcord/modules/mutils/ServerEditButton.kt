package de.miraculixx.mcord.modules.mutils

import de.miraculixx.mcord.utils.KeyInfoDisplays
import de.miraculixx.mcord.utils.api.API
import de.miraculixx.mcord.utils.api.callAPI
import de.miraculixx.mcord.utils.entities.Buttons
import de.miraculixx.mcord.utils.log
import de.miraculixx.mcord.utils.pw
import de.miraculixx.mcord.utils.stupidCatching
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

class ServerEditButton : Buttons {
    override suspend fun trigger(it: ButtonInteractionEvent) {
        val id = it.button.id ?: return
        val splitter = id.split('_')
        val userID = splitter[1]
        val ip = splitter[2]
        val dcID = it.user.id

        when {
            id.startsWith("conButtonRename_") -> {
                val modal = Modal.create("renameCon_${userID}_$ip", "Benenne deinen Server um")
                val input = TextInput.create("renameCon_$ip", "Gebe hier den neuen Namen ein", TextInputStyle.SHORT)
                input.placeholder = "Der Name des Servers darf nicht länger als 16 Zeichen sein"
                input.maxLength = 16
                input.minLength = 2
                input.isRequired = true
                modal.addActionRow(input.build())

                it.replyModal(modal.build()).queue()
                stupidCatching[it.user.id] = it.message
            }

            id.startsWith("conButtonDelete_") -> {
                val hook = it.hook
                val tool = KeyInfoDisplays(hook, it.jda)
                disableAll(it, tool.await)
                delay(4000)

                val overviewButton = Button.primary("conButtonBack_${userID}_$ip", "Back to Overview").withEmoji(Emoji.fromUnicode("✖"))
                val deleteButton = Button.danger("conButtonDelConfirm_${userID}_$ip", "Confirm Delete").withEmoji(Emoji.fromUnicode("✔"))

                hook.editOriginal(
                    ":warning: __**Server Löschen**__ :warning:\n" +
                            "```diff\n" +
                            "- Bist du sicher, dass du deine Server Verbindung zu MUtils unwiderruflich auflösen möchtest? \n" +
                            "- Dadurch kannst du auf dem ausgewählten Server keine Premium Funktionen mehr verwenden und alle Einstellungen gehen verloren!\n" +
                            "```\n" + "\n" +
                            "> Du kannst aus Sicherheitsgründen nur **3** Verbindungen pro Woche löschen"
                )
                    .setActionRow(overviewButton, deleteButton).setEmbeds().queue()
            }

            id.startsWith("conButtonDelConfirm_") -> {
                val hook = it.hook
                val tool = KeyInfoDisplays(hook, it.jda)
                disableAll(it, tool.await)
                val overviewButton = Button.primary("conButtonBack_${userID}_$ip", "Back to Overview").withEmoji(Emoji.fromUnicode("✖"))
                callAPI(API.MUTILS, "admin.php?call=deleteconnection&pw=$pw&id=$userID&ip=$ip")
                delay(1000)

                hook.editOriginal(
                    "\uD83D\uDDD1️ __**Server Gelöscht**__ \uD83D\uDDD1️\n" +
                            "Die ausgewählte Server Verbindung wurde erfolgreich gelöscht. Der Server wird demnächst neu gestartet und die gespeicherten Einstellungen entfernt. Solltest du erneut eine Verbindung herstellen wollen, kannst du auf einem beliebigen Minecraft Server auf welchem MUtils installiert ist `/verify <key>` eingeben!\n" + "\n" +
                            "> Du kannst aus Sicherheitsgründen nur **3** Verbindungen pro Woche löschen"
                )
                    .setActionRow(overviewButton).queue()
                "${it.user.asTag} -> DELETE - $ip".log()
            }

            id.startsWith("conButtonBack_") -> {
                val tool = KeyInfoDisplays(it.hook, it.jda)
                disableAll(it, tool.await)
                delay(500)
                tool.accountInfo(dcID)
            }

            else -> {
                it.reply("```diff\n- Ein unerwarteter Fehler ist aufgetreten!\n- Bitte melde diesen Fehler an das Team```").setEphemeral(true).queue()
            }
        }
    }

    private fun disableAll(it: ButtonInteractionEvent, await: String) {
        val rows = ArrayList<ActionRow>()
        val message = it.message
        message.actionRows.forEach { row ->
            rows.add(row.asDisabled())
        }
        it.editMessage(await).setActionRows(rows).queue()
    }
}