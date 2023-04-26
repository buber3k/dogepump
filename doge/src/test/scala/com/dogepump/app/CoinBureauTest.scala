package com.dogepump.app

import com.dogepump.app.Config.Implicits._
import com.dogepump.app.Config.PLAYLIST_ID_COIN_BUREAU
import com.dogepump.exchange.{BinanceService, ExchangeOperator}
import com.dogepump.helper.TimeHelper
import com.dogepump.stream.YoutubeStream
import grizzled.slf4j.Logging

import scala.concurrent.duration.DurationInt

object CoinBureauTest extends App with TimeHelper with Logging {

  val streamingService = new YoutubeStream()
  val exchangeService = new BinanceService()
  val egldOperator = new ExchangeOperator(exchangeService, "EGLDUSDT")
  val thetaOperator = new ExchangeOperator(exchangeService, "THETAUSDT")

  streamingService.streamVideos(PLAYLIST_ID_COIN_BUREAU, sleepTime = 2.seconds, maxDelayTime = 10.seconds) {
    case video if video.title.toLowerCase.contains("elrond") || video.title.toLowerCase.contains("egld") =>
      val openOrder = egldOperator.buyWithCost(usdCost = 1150, leverage = 20)
      egldOperator.setStopMarket(orderPrice = openOrder.averagePrice, expectedDownPercent = 1, quantity = openOrder.quantity)
      egldOperator.setTakeProfit(orderPrice = openOrder.averagePrice, expectedUpPercent = 3.0, quantity = openOrder.quantity)
    case video if video.title.toLowerCase.contains("theta") =>
      val openOrder = thetaOperator.buyWithCost(usdCost = 1150, leverage = 20)
      thetaOperator.setStopMarket(orderPrice = openOrder.averagePrice, expectedDownPercent = 1, quantity = openOrder.quantity)
      thetaOperator.setTakeProfit(orderPrice = openOrder.averagePrice, expectedUpPercent = 3.0, quantity = openOrder.quantity)

  }

  info("Stopping...")
  System.exit(0)
}
