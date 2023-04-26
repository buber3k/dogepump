package com.dogepump.app

import com.dogepump.app.Config.Implicits._
import com.dogepump.app.Config._
import com.dogepump.exchange.BinanceService
import com.dogepump.exchange.ExchangeOperator
import com.dogepump.stream.DiscordMessage
import com.dogepump.stream.DiscordStream
import com.dogepump.telegram.TelegramMessenger
import grizzled.slf4j.Logging
import org.javacord.api.AccountType
import org.javacord.api.DiscordApiBuilder

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.jdk.FutureConverters.CompletionStageOps

object Main extends App with Logging {
  val token = Config.DISCORD_TOKEN
  val discordClient = new DiscordApiBuilder().setAccountType(AccountType.CLIENT).setToken(token).login().join()
  val telegramClient = new TelegramMessenger(Config.TELEGRAM_BOT_TOKEN, Config.TELEGRAM_CHAT_ID)
  val streamingService = new DiscordStream(discordClient)
  val exchangeService = new BinanceService()
  var unwantedCoins = Seq("BTC", "DOT", "ETH", "DOGE", "BNB", "ADA", "LINK", "XRP", "UNI")
  val wordsBlackList = Seq("limit", "short", "spot", "stopped", "sorry", "move", "filled", "hit", "cancel", "profit", "at be", "to be")
  val coins = exchangeInformation.getAvailableCoinNames
  var coinBlackList = getCoinsFromLastMessages(5) ++ unwantedCoins

  val binanceServices = coins.map(coinName => {
    val operator = new ExchangeOperator(exchangeService, s"${coinName}USDT")
    coinName -> operator
  }).toMap

  info(s"BLACKLIST COINS: ${coinBlackList.mkString(",")}")
  info(s"BLACKLIST WORDS: ${wordsBlackList.mkString(",")}")
  info(s"EXPECTED JOHN PROFIT: ${Config.EXPECTED_JOHN_PROFIT}")
  info(s"EXPECTED ELIZ PROFIT: ${Config.EXPECTED_ELIZ_PROFIT}")
  info(s"EXPECTED PROFIT: ${Config.EXPECTED_PROFIT}")
  info(s"STOP LOSS: ${Config.EXPECTED_DOWN_PERCENT}")
  info(s"USD COST: ${Config.USD_COST}")
  info(s"LEVERAGE: ${Config.LEVERAGE}")
  info(s"UP ACTIVATION: ${Config.UP_ACTIVATION_PERCENT}%")
  info(s"NEW STOP MARKET: ${Config.NEW_STOP_MARKET_PERCENT}%")
  info(s"CHALLENGE ENABLED: ${Config.CHALLENGE_ENABLED}")
  info(s"CHALLENGE PROFIT: ${Config.CHALLENGE_PROFIT}")

  streamingService.streamMessages(
    expectedServer = TRADES_SERVER_ID,
    expectedChannels = Seq(TRADES_CHANNEL_ID, CHALLENGE_CHANNEL_ID),
    maxDelayTime = 300.millis) {
    case message if wordsBlackList.forall(badWord => !message.content.toLowerCase.contains(badWord)) =>
      val tradesProfit = message.author match {
        case "CryptoGodJohn" => Config.EXPECTED_JOHN_PROFIT
        case "cryptoEliZ" => Config.EXPECTED_ELIZ_PROFIT
        case _ => Config.EXPECTED_PROFIT
      }

      message.channelId match {
        case TRADES_CHANNEL_ID => executeOrder(message, tradesProfit)
        case CHALLENGE_CHANNEL_ID if CHALLENGE_ENABLED => executeOrder(message, CHALLENGE_PROFIT)
        case _ => info(s"Challenge disabled: $message")
      }
    case other =>
      getCoinFromMessage(other.content).foreach { coin =>
        unwantedCoins = unwantedCoins :+ coin
      }
  }

  def executeOrder(message: DiscordMessage, expectedProfit: Double): Unit = {
    getCoinFromMessage(message.content)
      .filter(coinName => !coinBlackList.contains(coinName))
      .foreach { coinName =>
        binanceServices.get(coinName).map(operator => {
          val lastOrder = operator.buyWithCost(Config.USD_COST, Config.LEVERAGE)
          operator.setTakeProfit(lastOrder.averagePrice, expectedUpPercent = expectedProfit, quantity = lastOrder.quantity)
          operator.setStopMarket(lastOrder.averagePrice, expectedDownPercent = Config.EXPECTED_DOWN_PERCENT, quantity = lastOrder.quantity)

          telegramClient.sendMessage(s"BOUGHT $coinName for ${lastOrder.averagePrice}")
          telegramClient.sendMessage(s"${message.author}: ${message.content}")
          info(s"BOUGHT $coinName")
          info(s"${message.author}: ${message.content}")
          info(s"PRICE: ${lastOrder.averagePrice}")
          info(s"MESSAGE TIME: ${message.messageTime}")
          Future {
            operator.monitorForStopLossChange(lastOrder, Config.UP_ACTIVATION_PERCENT, Config.NEW_STOP_MARKET_PERCENT)
          }
          Future {
            operator.monitorForStopLossCancellation(telegramClient, exchangeInformation)
          }
        })
        unwantedCoins = unwantedCoins :+ coinName.toUpperCase
      }
  }

  def getCoinFromMessage(text: String): Option[String] = {
    text.toUpperCase.replaceAll("[^A-Za-z0-9]", " ").split(" ").find(maybeCoin => coins.contains(maybeCoin))
  }

  def getCoinsFromLastMessages(limit: Int) = {
    Await.result(discordClient.getTextChannelById(TRADES_CHANNEL_ID).get().getMessages(limit).asScala, 5.seconds)
      .iterator().asScala.map(m => getCoinFromMessage(m.getContent)).toSeq.flatten
  }

}
