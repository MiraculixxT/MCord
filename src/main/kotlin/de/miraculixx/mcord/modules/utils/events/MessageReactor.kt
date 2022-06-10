package de.miraculixx.mcord.modules.utils.events

import de.miraculixx.mcord.Main
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds


class MessageReactor : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message
        val member = event.member
        val content = message.contentRaw
        val lower = content.lowercase()
        val jda = Main.INSTANCE.jda
        val guild = event.guild
        if ((content.startsWith('/') || content.startsWith('!')) && content.length > 1) {
            CoroutineScope(Dispatchers.Default).launch {
                message.delete().queue()
                val response =
                    withContext(Dispatchers.IO) {
                        message.reply("> <:slash:983086645505065020> ${member?.asMention} Slash Commands sind keine Chat Nachrichten! Wähle sie im Menü aus oder lasse sie dir von Discord vervollständigen\nhttps://i.imgur.com/rN1IFHQ.png")
                            .submit().get()
                    }
                selfDelete(response)
            }
            return
        }

        if (member != null && !member.user.isBot) {
            //Message is from a valid User
            if (lower.contains("https://")) {
                //Has Embed Link
                if (!content.contains("tenor.com") && !content.contains("imgur.com"))
                    message.suppressEmbeds(true).queue()
            }
            if (lower.contains("kuhl") || lower.contains("cool"))
                message.addReaction("\uD83C\uDD92").queue()
            if (lower.contains("sus"))
                message.addReaction(jda?.getEmoteById(984179178758897674)!!).queue()
            if (lower.contains("xd") || lower.contains("joy")) {
                message.addReaction(jda?.getEmoteById(949671949670428733)!!).queue()
                message.addReaction(jda.getEmoteById(984174041923485716)!!).queue()
                message.addReaction(jda.getEmoteById(984174039713075251)!!).queue()
                message.addReaction(jda.getEmoteById(984174038396043324)!!).queue()
            }
            if (lower.contains("paula")) {
                message.addReaction("\uD83C\uDDF5 ").queue()
                message.addReaction("\uD83C\uDDE6").queue()
                message.addReaction("\uD83C\uDDFA").queue()
                message.addReaction("\uD83C\uDDF1").queue()
                message.addReaction("\uD83C\uDD70️").queue()
            }
            if (lower.contains("join mal call")) {
                val am = guild.audioManager
                am.openAudioConnection(jda?.getVoiceChannelById(707930638002683904))
            }
            if (lower.contains("<@958434923100913804>")) {
                member.timeoutFor(Random.nextLong(10,500), TimeUnit.SECONDS).queue()
                message.reply("${member.asMention} Pings sind uncool... ${member.asMention} weißt du das eigentlich? \n${member.asMention} dafür haste jetzt random Timeout kassiert...").queue()
            }
        }
    }

    private suspend fun selfDelete(response: Message) = Dispatchers.Default {
        delay(5.seconds)
        launch { response.addReaction("\uD83D\uDD1F").queue() }
        delay(5.seconds)
        launch {
            response.addReaction("5️⃣").queue()
            response.clearReactions("\uD83D\uDD1F").queue()
        }
        delay(1.seconds)
        delay(1.seconds)
        launch {
            response.addReaction("3️⃣").queue()
            response.clearReactions("5️⃣").queue()
        }
        delay(1.seconds)
        launch {
            response.addReaction("2️⃣").queue()
            response.clearReactions("3️⃣").queue()
        }
        delay(1500)
        response.delete().queue()
    }
}