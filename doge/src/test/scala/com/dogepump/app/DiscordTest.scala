package com.dogepump.app

import com.dogepump.app.Config.Implicits._
import com.dogepump.app.Config.TRADES_CHANNEL_ID
import com.dogepump.app.Config.TRADES_SERVER_ID
import com.dogepump.exchange.BinanceService
import com.dogepump.exchange.ExchangeOperator
import com.dogepump.stream.DiscordStream
import grizzled.slf4j.Logging
import org.javacord.api.AccountType
import org.javacord.api.DiscordApiBuilder

import scala.concurrent.duration.DurationInt

object DiscordTest extends App with Logging {
  val token = Config.DISCORD_TOKEN
  val discordClient = new DiscordApiBuilder().setAccountType(AccountType.CLIENT).setToken(token).login().join()
  val streamingService = new DiscordStream(discordClient)
  val exchangeService = new BinanceService()
  val coins = Config.binanceFuturesCoins

  val binanceServices = coins.map(coinName => {
    val operator = new ExchangeOperator(exchangeService, s"${coinName}USDT")
    coinName -> operator
  }).toMap

  streamingService.streamMessages(
    expectedServer = TRADES_SERVER_ID,
    expectedChannels = Seq(TRADES_CHANNEL_ID),
    maxDelayTime = 5.seconds) {
    case message if List("limit", "short", "spot").forall(badWord => !message.content.contains(badWord)) =>
      val maybeTicker = getCoinFromMessage(message.content)
      if (maybeTicker.isDefined) {
        val ticker = maybeTicker.get
        info(s"BUY $ticker")
        binanceServices.get(ticker).map(operator => {
          val lastOrder = operator.buyWithCost(1100, 20)

          operator.setTakeProfit(lastOrder.averagePrice, expectedUpPercent = 2, quantity = (lastOrder.quantity * 0.5))
          operator.setTakeProfit(lastOrder.averagePrice, expectedUpPercent = 3, quantity = (lastOrder.quantity * 0.3))
          operator.setTakeProfit(lastOrder.averagePrice, expectedUpPercent = 4, quantity = (lastOrder.quantity * 0.2))
          operator.setStopMarket(lastOrder.averagePrice, expectedDownPercent = 1, quantity = lastOrder.quantity)
        })
      }
  }

  def getCoinFromMessage(text: String): Option[String] = {
    text.toUpperCase.split(" ").find(maybeTicker => {
      coins.contains(maybeTicker)
    })
  }

}
