package de.miraculixx.mcord.modules.utils.events

import de.miraculixx.mcord.utils.WebClient
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.net.URL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object MessageReactor {
    // GuildID - ChannelID

    fun startListen(jda: JDA) = jda.listener<MessageReceivedEvent> {
        val message = it.message
        val member = it.member ?: return@listener
        val content = message.contentRaw
        val lower = content.lowercase()
        if (member.user.isBot) return@listener


        // Prevent Users from using legacy commands
        if ((content.startsWith('/') || content.startsWith('!')) && content.length > 1) {
            message.delete().queue()
            message.reply_("> <:slash:983086645505065020> ${member.asMention} Slash Commands sind keine Chat Nachrichten! Wähle sie im Menü aus oder lasse sie dir von Discord vervollständigen\nhttps://i.imgur.com/rN1IFHQ.png")
                .queue { message ->
                    getDefaultScope().launch {
                        selfDelete(message, 10.seconds)
                    }
                }
            return@listener
        }

        // Fun Area
        if (lower.contains("kuhl ") || lower.contains("cool"))
            message.addReaction(Emoji.fromUnicode("\uD83C\uDD92")).queue()

        // Code detector
        val att = message.attachments
        if (att.isNotEmpty()) {
            val uploads = buildSet {
                att.forEach { file ->
                    val extension = file.fileExtension ?: return@forEach
                    when (extension) {
                        "yml","yaml" -> uploadCode(file.url, "yaml")?.let { it1 -> add(it1) }
                        "json" -> uploadCode(file.url, "json")?.let { it1 -> add(it1) }
                        "kt","kts" -> {
                            try {
                                add(uploadCode(file.url, "kotlin") + " <-")
                            } catch (e: Exception) { println(e.message) }
                        }
                        "js","ts" -> uploadCode(file.url, "javascript")?.let { it1 -> add(it1) }
                        "txt","bat" -> uploadCode(file.url, "plain")?.let { it1 -> add(it1) }
                        "html","css","php","python","sql","go","xml","ini" -> uploadCode(file.url, extension)?.let { it1 -> add(it1) }
                    }
                }
            }
            if (uploads.isNotEmpty()) message.sendPastesMessage(uploads, member)
        }
    }

    private suspend fun selfDelete(response: Message, duration: Duration) {
        delay(duration)
        try {
            response.delete().queue()
        } catch (_: Exception) { }
    }

    private suspend fun uploadCode(url: String, type: String) =
        WebClient.client.post("https://api.pastes.dev/post") {
//            header("Content-Type", "text/$type")
            header("User-Agent", "MCord.v1.2")
            header("Accept", "application/json")
            setBody(URL(url).readText())
        }.headers["location"]

    private fun Message.sendPastesMessage(set: Set<String>, responder: Member) {
        reply_(buildString {
            append("${responder.asMention} we uploaded your code to make it easier readable for everyone <:peepoGlad:849687812713873519>\n")
            set.forEachIndexed { index, s -> append("$index. https://pastes.dev/$s") }
        }).setSuppressEmbeds(true).queue()
    }
}