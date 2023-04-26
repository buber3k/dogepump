package com.dogepump.app

import com.dogepump.app.Config.Implicits._
import com.dogepump.exchange.{BinanceService, ExchangeOperator}
import com.dogepump.helper.TimeHelper
import com.dogepump.stream.{FeedStream, TweetStream}
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object RssFeedReader extends App with TimeHelper with Logging {

  val streamingService = new TweetStream()
  val exchangeService = new BinanceService()
  val coins = exchangeInformation.getAvailableCoinNames.toSet -- Config.coinbaseCoins
  val binanceServices = coins.map(coinName => {
    val operator = new ExchangeOperator(exchangeService, s"${coinName}USDT")
    coinName -> operator
  }).toMap

  val feedUrl = "https://medium.com/feed/@coinbaseblog"
  val feedStream = new FeedStream
  feedStream.streamFeeds(feedUrl, sleepTime = 1.second, maxDelayTime = 10.seconds) {
    case feed if timeIsOk(feed.date, 7.days) =>
      info(s"${timeNowString()}: ${feed.title}")
      val maybeCoins = getCoinsFromTweet(feed.title)
      maybeCoins.foreach(coin => {
        info(s"Found coin: $coin!")
        Future {
          if (maybeCoins.size == 1) {
            if (coin == "DOT")  {
              binanceServices(coin).buyWithCost(2000, 20)
            } else {
              binanceServices(coin).buyWithCost(1100, 20)
              binanceServices(coin).buyWithCost(700, 20)
            }
          } else if (maybeCoins.size == 2) {
            binanceServices(coin).buyWithCost(1000, 20)
          } else {
            binanceServices(coin).buyWithCost(700, 20)
          }
        }
      })
  }

  def getCoinsFromTweet(text: String): Seq[String] = {
    val textWithoutUsdt = text.replaceAll("USDT", "")
    val replacedText = textWithoutUsdt.replaceAll("[^A-Za-z0-9]", " ");
    replacedText.split(" ").filter(maybeCoin => {
      coins.contains(maybeCoin)
    }).toSet.toSeq
  }
}
