package de.miraculixx.mcord.modules.utils.events

import de.miraculixx.mcord.config.ConfigManager
import de.miraculixx.mcord.config.Configs
import de.miraculixx.mcord.utils.Color
import de.miraculixx.mcord.utils.log
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


object MessageReactor {
    // GuildID - ChannelID
    private val suggest = ConfigManager.getConfig(Configs.SETTINGS).getObjectList<String>("Suggestion-Channel")
    private val s = mapOf("908621996009619477" to "922851208295755808")

    fun startListen(jda: JDA) = jda.listener<MessageReceivedEvent> {
        val message = it.message
        val member = it.member ?: return@listener
        val content = message.contentRaw
        val lower = content.lowercase()
        val guild = it.guild
        if (member.user.isBot) return@listener

        CoroutineScope(Dispatchers.Default).launch {
            // Prevent Users from using legacy commands
            if ((content.startsWith('/') || content.startsWith('!')) && content.length > 1) {
                message.delete().queue()
                    message.reply_("> <:slash:983086645505065020> ${member.asMention} Slash Commands sind keine Chat Nachrichten! Wähle sie im Menü aus oder lasse sie dir von Discord vervollständigen\nhttps://i.imgur.com/rN1IFHQ.png")
                        .queue { message ->
                            getDefaultScope().launch {
                                selfDelete(message, 10.seconds)
                            }
                        }
                return@launch
            }

            // Fun Area
            if (lower.contains("kuhl ") || lower.contains("cool"))
                message.addReaction(Emoji.fromUnicode("\uD83C\uDD92")).queue()

            // Suggest Checker
            suggest.toString().log(Color.RED)
            "${suggest["908621996009619477"]} - ${suggest[guild.id]}".log(Color.RED)
            if (suggest.containsKey(guild.id) && it.channel is TextChannel) {
                val channel = it.textChannel
                if (suggest.containsValue(channel.id)) {
                    message.delete().queue()
                    channel.sendMessageEmbeds(
                        Embed {
                            title = "\uD83D\uDCE8 || SUGGESTIONS"
                            description = "Is this your correct suggestion?"
                            field {
                                name = "Your Suggestion"
                                value = "```fix\n$content```"
                            }
                        }
                    ).setActionRow(
                        SelectMenu("SUGGEST_YES_${member.id}") {
                            placeholder = "What is your suggestion about?"
                            maxValues = 1
                            minValues = 1
                            option("MCreate Server", "MCREATE", emoji = Emoji.fromFormatted("<:protect:885240719202197545>"))
                            option("MUtils Plugin", "MUTILS", emoji = Emoji.fromFormatted("<:mutils:975780449903341579>"))
                            option("MGame Club", "GAME_CLUB", emoji = Emoji.fromFormatted("<:mcoin:996386525208117258>"))
                            option("Generell / Other", "OTHER", emoji = Emoji.fromUnicode("❔"))
                        },
                        Button.success("SUGGEST_NO_${member.id}", "CANCEL").withEmoji(Emoji.fromUnicode("✖️"))
                    ).queue {
                        CoroutineScope(Dispatchers.Default).launch {
                            selfDelete(it, 1.minutes)
                        }
                    }
                }
            }
        }
    }

    private suspend fun selfDelete(response: Message, duration: Duration) {
        delay(duration)
        try {
            response.delete().queue()
        } catch (_: Exception) {
        }
    }
}