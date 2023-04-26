package com.dogepump.app

import com.dogepump.app.Config.Implicits._
import com.dogepump.exchange.BinanceService
import com.dogepump.exchange.ExchangeOperator
import com.dogepump.helper.TimeHelper
import com.dogepump.stream.FeedStream
import com.dogepump.stream.TweetStream
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object MainCoinbase extends App with TimeHelper with Logging {
  val streamingService = new TweetStream()
  val exchangeService = new BinanceService()
  val coins = Config.notCoinbaseCoins
  val binanceServices = coins.map(coinName => {
    val operator = new ExchangeOperator(exchangeService, s"${coinName}USDT")
    coinName -> operator
  }).toMap

  val feedUrl = "https://blog.coinbase.com/feed"
  val feedStream = new FeedStream
  feedStream.streamFeeds(feedUrl, 2.second, 5.seconds) {
    case feed if feed.title.contains("now available") =>
      val INCH = "1INCH"
      val ENJ = "ENJ"
      val leverage = 20
      val cost = 1000
      val expectedDownPercent = 1

      if (feed.title.contains(INCH)) {
        val order = binanceServices(INCH).buyWithCost(cost, leverage)
        binanceServices(INCH).setStopMarket(order.averagePrice, expectedDownPercent, order.quantity)
        println(s"BOUGHT 1INCH ${order}")
        Future {
          binanceServices(INCH).monitorForStopLossChange(order, 0.5, 0.1)
        }
        sendNotification(INCH, order.averagePrice)
      }
      if (feed.title.contains(ENJ)) {
        val order = binanceServices(ENJ).buyWithCost(cost, leverage)
        binanceServices(ENJ).setStopMarket(order.averagePrice, expectedDownPercent, order.quantity)
        println(s"BOUGHT ENJ ${order}")
        Future {
          binanceServices(ENJ).monitorForStopLossChange(order, 0.5, 0.1)
        }
        sendNotification(ENJ, order.averagePrice)
      }
      Thread.sleep(15.seconds.toMillis)
  }

  def sendNotification(coinName: String, price: Double): Unit = {
    val request = new SendMessage(-503274131L, s"BOUGHT $coinName for $price")
    new TelegramBot("1709448466:AAGdS9t9Iw6PNnv3CD5wN-QzKWvpErEO2XE").execute(request)
  }
}
