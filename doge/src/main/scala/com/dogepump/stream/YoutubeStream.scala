package com.dogepump.stream

import com.dogepump.auth.YoutubeAuth
import com.dogepump.helper.TimeHelper
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import grizzled.slf4j.Logging

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

case class YoutubeVideo(title: String, publishedAt: com.google.api.client.util.DateTime)

class YoutubeStream(implicit auth: YoutubeAuth) extends TimeHelper with Logging {

  private val youtube = new YouTube.Builder(new NetHttpTransport, new JacksonFactory, new HttpRequestInitializer() {
    override def initialize(request: HttpRequest): Unit = {}
  }).setApplicationName("doge").build()

  def streamVideos(playlistId: String, sleepTime: Duration, maxDelayTime: Duration)(f: PartialFunction[YoutubeVideo, Unit]): Unit = {
    val search = buildPlaylistRequest(playlistId)

    breakable {
      while (true) {
        val searchResponse = search.execute()
        val searchResultList = searchResponse.getItems.asScala

        searchResultList.toList.map(sr => YoutubeVideo(sr.getSnippet.getTitle, sr.getSnippet.getPublishedAt)).foreach {
          case v if f.isDefinedAt(v) && timeIsOk(v.publishedAt, maxDelayTime) =>
            info(s"${v.publishedAt}: VIDEO TIME")
            info(s"${timeNowString()}: EXPECTED VIDEO ${v.title}")
            f(v)
            break()
          case lastVideo =>
            info(s"${timeNowString()}: Last video: ${lastVideo.title}")
            Thread.sleep(sleepTime.toMillis)
        }
      }
    }
  }

  def buildPlaylistRequest(playlistId: String): YouTube#PlaylistItems#List = {
    val search = youtube.playlistItems().list(List("id", "snippet").asJava)
    search.setKey(auth.apiKey)
    search.setPlaylistId(playlistId)
    search.setFields("items(snippet/title,snippet/publishedAt)")
    search.setMaxResults(1)
    search
  }


}
