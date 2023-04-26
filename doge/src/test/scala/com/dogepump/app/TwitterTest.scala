package com.dogepump.app

import com.danielasfregola.twitter4s.entities.Tweet
import com.dogepump.app.Config.DDROZD_ID
import com.dogepump.app.Config.Implicits._
import com.dogepump.helper.TimeHelper
import com.dogepump.stream.TweetStream
import grizzled.slf4j.Logging

import scala.concurrent.duration.DurationInt

object TwitterTest extends App with TimeHelper with Logging {
  val streamingService = new TweetStream()

  streamingService.streamUserTweets(BUBA_ID, 2.seconds, 10.seconds) {
    case tweet: Tweet if tweet.text.contains("doge") =>
      info(s"${fromInstant(tweet.created_at)}: Tweet: ${tweet.text}")
      info(s"${timeNowString()}: in app")
  }

  info("Exiting.")
  System.exit(0)
}
