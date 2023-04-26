package com.dogepump.app

import com.dogepump.app.Config.Implicits._
import com.dogepump.app.Config.PLAYLIST_ID_DDROZD
import com.dogepump.helper.TimeHelper
import com.dogepump.stream.YoutubeStream
import grizzled.slf4j.Logging

import scala.concurrent.duration.DurationInt

object YoutubeTest extends App with TimeHelper with Logging {
  val reader = new YoutubeStream()
  reader.streamVideos(PLAYLIST_ID_BUBA, 2.seconds, 10.seconds) {
    case video if video.title.toLowerCase.contains("elrond") && timeIsOk(video.publishedAt, 10.seconds) =>
      info(s"${video.publishedAt}: Video: ${video.title}")
      info(s"${timeNowString()}: in app")
  }
}
