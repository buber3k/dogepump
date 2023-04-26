package com.dogepump.exchange

import com.binance.client.model.enums._
import com.binance.client.model.trade.AccountBalance
import com.binance.client.model.trade.Order
import com.binance.client.model.trade.Position
import com.dogepump.auth.BinanceAuth
import com.dogepump.helper.RequestHelper

import scala.jdk.CollectionConverters._

class BinanceService(implicit auth: BinanceAuth, exchangeInformation: ExchangeInformation) extends RequestHelper {
  def setTrailingStop(ticker: String, quantity: Double, callbackRate: Double): Unit = {
    val syncRequestClient = createRequestClient

    syncRequestClient.postOrder(
      ticker,
      OrderSide.SELL,
      PositionSide.BOTH,
      OrderType.TRAILING_STOP_MARKET,
      TimeInForce.GTC,
      quantity.toString,
      null,
      "true",
      null,
      null,
      null,
      NewOrderRespType.RESULT,
      callbackRate.toString
    )
  }

  def buy(ticker: String, quantity: Double, leverage: Int): Order = {
    val syncRequestClient = createRequestClient
    syncRequestClient.changeInitialLeverage(ticker, leverage)

    syncRequestClient.postOrder(
      ticker,
      OrderSide.BUY,
      null,
      OrderType.MARKET,
      null,
      quantity.toString,
      null,
      null,
      null,
      null,
      null,
      NewOrderRespType.RESULT,
      null
    )
  }

  def setTakeProfit(ticker: String, quantity: Double, stopPrice: Double): Unit = {
    val syncRequestClient = createRequestClient
    syncRequestClient.postOrder(
      ticker,
      OrderSide.SELL,
      PositionSide.BOTH,
      OrderType.TAKE_PROFIT,
      TimeInForce.GTC,
      quantity.toString,
      stopPrice.toString,
      "true",
      null,
      stopPrice.toString,
      null,
      NewOrderRespType.RESULT,
      null
    )
  }

  def setStopMarket(ticker: String, quantity: Double, stopPrice: Double): Unit = {
    val syncRequestClient = createRequestClient
    syncRequestClient.postOrder(
      ticker,
      OrderSide.SELL,
      PositionSide.BOTH,
      OrderType.STOP_MARKET,
      TimeInForce.GTC,
      quantity.toString,
      null,
      "true",
      null,
      stopPrice.toString,
      null,
      NewOrderRespType.RESULT,
      null
    )
  }

  def getOpenOrders: List[Order] = {
    val syncRequestClient = createRequestClient
    syncRequestClient.getOpenOrders("BTCUSDT").asScala.toList
  }

  def getBalance: List[AccountBalance] = {
    val syncRequestClient = createRequestClient
    syncRequestClient.getBalance.asScala.toList
  }

  def getOpenPositions(coinName: String): List[Position] = {
    val syncRequestClient = createRequestClient
    syncRequestClient.getAccountInformation.getPositions.asScala.toList
      .filter(_.getSymbol == s"${coinName}USDT")
      .filter(position =>
        position.getInitialMargin.compareTo(BigDecimal(0)) != 0
          && position.getUnrealizedProfit.compareTo(BigDecimal(0)) != 0)
  }

  def getCurrentPrice(ticker: String): Double = {
    exchangeInformation.getCoinPrice(ticker)
  }

  def getPricePrecision(ticker: String): Int = {
    exchangeInformation.getPricePrecision(ticker)
  }

  def getQuantityPrecision(ticker: String): Int = {
    exchangeInformation.getQuantityPrecision(ticker)
  }

  def fetchCurrentPrice(ticker: String): Double = {
    val syncRequestClient = createRequestClient
    syncRequestClient.getMarkPrice(ticker).iterator().asScala.find(_.getSymbol == ticker).get.getMarkPrice.doubleValue()
  }

  def removeOrder(ticker: String, orderId: Long): Unit = {
    val syncRequestClient = createRequestClient
    syncRequestClient.cancelOrder(ticker, orderId, null)
  }
}
