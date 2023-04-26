package com.dogepump.app

import com.danielasfregola.twitter4s.entities.Tweet
import com.dogepump.app.Config.COINBASE_PRO_ID
import com.dogepump.app.Config.Implicits._
import com.dogepump.app.RssFeedReader.binanceServices
import com.dogepump.exchange.{BinanceService, ExchangeOperator}
import com.dogepump.helper.PriceHelper
import com.dogepump.stream.TweetStream
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object CoinbaseTest extends App with PriceHelper with Logging {
  val streamingService = new TweetStream()
  val exchangeService = new BinanceService()
  val coins = exchangeInformation.getAvailableCoinNames.toSet -- Config.coinbaseCoins

  val binanceServices = coins.map(coinName => {
    val operator = new ExchangeOperator(exchangeService, s"${coinName}USDT")
    coinName -> operator
  }).toMap

  streamingService.streamUserTweets(COINBASE_PRO_ID, sleepTime = 1500.millis, maxDelayTime = 10.seconds) {
    case tweet: Tweet =>
      val maybeCoins = getCoinsFromTweet(tweet.text)
      maybeCoins.foreach(coin => {
        info(s"Found coin: $coin!")
        Future {
          if (maybeCoins.size == 1) {
            if (coin == "DOT")  {
              binanceServices(coin).buyWithCost(2000, 20)
            } else {
              binanceServices(coin).buyWithCost(1100, 20)
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
