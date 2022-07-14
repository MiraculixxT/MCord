package de.miraculixx.mcord.modules.syncChat

import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import de.miraculixx.mcord.Main
import de.miraculixx.mcord.utils.entities.LateInit
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.user.UserTypingEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SyncCodingChat: ListenerAdapter(), LateInit {
    private var mcreate: JDAWebhookClient? = null
    private var miraculixx: JDAWebhookClient? = null
    private var webhookMC: Webhook? = null
    private var webhookMI: Webhook? = null

    override fun onMessageUpdate(it: MessageUpdateEvent) {}

    override fun onUserTyping(it: UserTypingEvent) {
        val user = it.member?.user ?: return
        if (user.isBot) return
        println(user.asTag + " is writing...")

        when (it.textChannel?.id) {
            "908839604755497000" -> { //MCreate
                webhookMI?.channel?.sendTyping()?.queue()
            }
            "851100264516812810" -> { //Miraculixx
                webhookMC?.channel?.sendTyping()?.queue()
            }
        }
    }

    override fun onMessageReceived(it: MessageReceivedEvent) {
        return
        if (it.isWebhookMessage) return
        if (!it.isFromGuild) return
        val message = it.message
        val user = it.member?.user ?: return
        val id = it.textChannel.id
        if (id != "908839604755497000" && id != "851100264516812810") return

        val url = message.attachments.firstOrNull()?.url

        val builder = WebhookMessageBuilder()
            .setUsername(user.name)
            .setAvatarUrl(user.avatarUrl)
            .setContent(message.contentRaw)
        if (url != null) {
            builder.addEmbeds(WebhookEmbedBuilder()
                .setImageUrl(url)
                .setColor(0x2F3136).build())
        }

        when (id) {
            "908839604755497000" -> { //MCreate
                miraculixx?.send(builder.build())
            }
            "851100264516812810" -> { //Miraculixx
                mcreate?.send(builder.build())
            }
        }
    }

    override fun setup() {
        val jda = Main.INSTANCE.jda!!
        webhookMC = jda.retrieveWebhookById("974303753094193214").complete()
        mcreate = JDAWebhookClient.from(webhookMC!!)
        webhookMI = jda.retrieveWebhookById("974304494898782268").complete()
        miraculixx = JDAWebhookClient.from(webhookMI!!)
    }
}