package com.dogepump.stream

import java.net.URL
import java.time.Instant

import com.dogepump.helper.TimeHelper
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import grizzled.slf4j.Logging

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

case class FeedEntry(title: String, date: Instant)

class FeedStream extends TimeHelper with Logging {

  def streamFeeds(rssUrl: String, sleepTime: Duration, maxDelayTime: Duration)(f: PartialFunction[FeedEntry, Unit]): Unit = {
    val feedUrl = new URL(rssUrl)

    breakable {
      while (true) {
        val input = new SyndFeedInput
        val feed: SyndFeed = input.build(new XmlReader(feedUrl))

        feed.getEntries.asScala.headOption.map(entry => FeedEntry(entry.getTitle, entry.getPublishedDate.toInstant))
          .foreach {
            case feed if f.isDefinedAt(feed) && timeIsOk(feed.date, maxDelayTime) =>
              info(s"${fromInstant(feed.date)}: POST TIME")
              info(s"${timeNowString()}: EXPECTED POST: ${feed.title}")
              f(feed)
              break()
            case feed =>
              info(feed.title)
              Thread.sleep(sleepTime.toMillis)
          }
      }
    }

  }
}
