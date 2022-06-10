package de.miraculixx.mcord.modules.starboard

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class StarboardReaction(private val jda: JDA) : ListenerAdapter() {

  override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
    if(event.reactionEmote.idLong != 984924524720058398) return
    if(event.reaction.count < 5)return

    jda.getTextChannelById(746752229821644862).let {
      it!!.sendMessage("New starboard entry: ${event.retrieveMessage().queue { message ->
        message.contentRaw
      }
      }")
    }
  }

}