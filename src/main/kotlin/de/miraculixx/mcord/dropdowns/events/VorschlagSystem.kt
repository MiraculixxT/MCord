package de.miraculixx.mcord.dropdowns.events

import de.miraculixx.mcord.dropdowns.DropDowns
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

class VorschlagSystem: DropDowns {
    override suspend fun trigger(it: SelectMenuInteractionEvent) {
        val options = it.selectedOptions
        if (options.isEmpty()) return
        val selected = options.first()
        val user = it.user
        val title = when (selected.value) {
            "vorschlag1" -> "Minecraft Challenges"
            "vorschlag2" -> "Seltene Sachen in MC"
            "vorschlag3" -> "Discord Verbesserungen"
            else -> "Error"
        }
        val input = TextInput.create("desc", "Vorschlag Erklärung", TextInputStyle.PARAGRAPH)
        input.placeholder = "Gebe deinem Vorschlag einen Namen"
        input.maxLength = 2000
        input.minLength = 50
        input.isRequired = true
        val input2 = TextInput.create("name", "Vorschlag Name", TextInputStyle.SHORT)
        input2.placeholder = "Erkläre hier, wie dein Vorschlag aufgebaut ist"
        input2.maxLength = 50
        input2.minLength = 8
        input2.isRequired = true
        val modal = Modal.create(selected.value+"_${user.id}", title)
            .addActionRows(ActionRow.of(input2.build()), ActionRow.of(input.build()))
        it.replyModal(modal.build()).queue()
    }
}