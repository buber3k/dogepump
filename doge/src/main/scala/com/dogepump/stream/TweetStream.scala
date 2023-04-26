package com.dogepump.stream

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.AccessToken
import com.danielasfregola.twitter4s.entities.ConsumerToken
import com.danielasfregola.twitter4s.entities.Tweet
import com.dogepump.auth.TwitterAuth
import com.dogepump.helper.TimeHelper
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

class TweetStream(implicit auth: TwitterAuth) extends TimeHelper with Logging {

  def streamUserTweets(userId: Long, sleepTime: Duration, maxDelayTime: Duration)(f: PartialFunction[Tweet, Unit]): Unit = {
    val consumerToken = ConsumerToken(key = auth.consumerKey, secret = auth.consumerSecret)
    val accessToken = AccessToken(key = auth.accessToken, secret = auth.secretToken)
    val client = TwitterRestClient(consumerToken, accessToken)

    breakable {
      while (true) {
        client.userTimelineForUserId(userId, count = 1).map(tweets => {
          tweets.data.headOption.foreach {
            case v if isCreatedByUser(v, userId) && f.isDefinedAt(v) && timeIsOk(v.created_at, maxDelayTime) =>
              info(s"${fromInstant(v.created_at)}: TWEET TIME")
              info(s"${timeNowString()}: EXPECTED TWEET: ${v.text}")
              f(v)
              break()
            case lastTweet =>
              info(s"${timeNowString()} Last tweet: ${lastTweet.text}")
          }
        })
        Thread.sleep(sleepTime.toMillis)
      }
    }
  }

  private def isCreatedByUser(tweet: Tweet, id: Long) = {
    tweet.user.exists(_.id == id) &&
      !isRetweeted(tweet) &&
      !isReplyToTweet(tweet) &&
      !isReplyToUserId(tweet) &&
      !isReplyToStatusId(tweet) &&
      !isQuoteStatus(tweet)
  }

  private def isRetweeted(tweet: Tweet): Boolean = tweet.retweeted_status.isDefined

  private def isReplyToTweet(tweet: Tweet): Boolean = tweet.in_reply_to_screen_name.isDefined

  private def isReplyToUserId(tweet: Tweet): Boolean = tweet.in_reply_to_user_id.isDefined

  private def isReplyToStatusId(tweet: Tweet): Boolean = tweet.in_reply_to_status_id.isDefined

  private def isQuoteStatus(tweet: Tweet): Boolean = tweet.is_quote_status
}
