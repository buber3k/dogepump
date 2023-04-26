package com.dogepump.stream

import java.time.Instant

import com.dogepump.helper.TimeHelper
import grizzled.slf4j.Logging
import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.event.message.MessageCreateEvent

import scala.concurrent.duration.Duration

case class DiscordMessage(author: String, content: String, channelId: Long, messageTime: Instant)

class DiscordStream(discordClient: DiscordApi) extends TimeHelper with Logging {

  private var lastLogTime = Instant.ofEpochMilli(0)

  private[this] var ignoreMessages = false

  def streamMessages(expectedServer: Long, expectedChannels: Seq[Long], maxDelayTime: Duration)(f: PartialFunction[DiscordMessage, Unit]): Unit = {
    discordClient.addMessageCreateListener((event: MessageCreateEvent) => {
      val messageServer = event.getServer.get().getId
      val messageChannel = event.getChannel.getId
      val messageChannelName = event.getChannel match {
        case channel: ServerTextChannel => channel.getName
        case _ => "UNKNOWN"
      }
      val content = event.getMessage.getContent
      val author = event.getMessage.getAuthor.getName
      val message = DiscordMessage(author, content, messageChannel, event.getMessage.getCreationTimestamp)

      if (!ignoreMessages &&
        messageServer == expectedServer
        && expectedChannels.contains(messageChannel)
        && f.isDefinedAt(message)
        && timeIsOk(event.getMessage.getCreationTimestamp, maxDelayTime)) {
        f(message)
      }

      if (lastLogTime.plusSeconds(30).isBefore(timeNow()) && !ignoreMessages) {
        lastLogTime = timeNow()
        info(s"$messageChannelName: $author: $content")
      }

    })

  }

  def setIgnoreMessages(flag: Boolean): Unit = ignoreMessages.synchronized {
    ignoreMessages = flag
  }

  def getIgnoreMessages(): Boolean = ignoreMessages.synchronized(ignoreMessages)

}
