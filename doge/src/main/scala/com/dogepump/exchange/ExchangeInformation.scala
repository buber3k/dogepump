package com.dogepump.exchange

import akka.actor.ActorSystem
import com.binance.client.model.market.ExchangeInfoEntry
import com.dogepump.auth.BinanceAuth
import com.dogepump.helper.RequestHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.CollectionHasAsScala

class ExchangeInformation(implicit auth: BinanceAuth) extends RequestHelper {
  var coinInfos = Map.empty[String, ExchangeInfoEntry]
  var coinPriceInfos = Map.empty[String, Double]
  var infoIsAccessible = false
  var currentBalance: Double = 0.0

  private val system = ActorSystem("coin-price-updater")

  system.scheduler.scheduleAtFixedRate(0.second, 15.seconds)(() => {
    try {
      updateCoinInfo()
      updateCoinPrice()
      updateBalance()
      infoIsAccessible = true
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }
  })

  private def updateCoinPrice(): Unit = {
    val syncRequestClient = createRequestClient
    val newPrices = syncRequestClient.getMarkPrice(null).asScala.toList.map(markPrice =>
      markPrice.getSymbol -> markPrice.getMarkPrice.doubleValue()
    ).toMap
    coinPriceInfos = newPrices
  }

  private def updateBalance(): Unit = {
    val syncRequestClient = createRequestClient
    currentBalance = syncRequestClient.getBalance.asScala.filter(_.getAsset == "USDT").map(_.getBalance).head.doubleValue()
  }

  private def updateCoinInfo(): Unit = {
    val syncRequestClient = createRequestClient
    val newInfos = syncRequestClient.getExchangeInformation.getSymbols.asScala.map(infoEntry =>
      infoEntry.getSymbol -> infoEntry
    ).toMap
    coinInfos = newInfos
  }

  def getPricePrecision(ticker: String): Int = {
    while (!infoIsAccessible) Thread.sleep(500)
    coinInfos(ticker).getPricePrecision.toInt
  }

  def getCoinPrice(ticker: String): Double = {
    while (!infoIsAccessible) Thread.sleep(500)
    coinPriceInfos(ticker)
  }

  def getQuantityPrecision(ticker: String): Int = {
    while (!infoIsAccessible) Thread.sleep(500)
    coinInfos(ticker).getQuantityPrecision.toInt
  }

  def getAvailableCoinNames: Seq[String] = {
    while (!infoIsAccessible) Thread.sleep(500)
    coinInfos.values.map(_.getBaseAsset).toSet.toSeq
  }

}
