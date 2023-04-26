package com.dogepump.exchange

import com.binance.client.model.enums.OrderType
import com.dogepump.helper.PriceHelper
import com.dogepump.helper.TimeHelper
import com.dogepump.telegram.TelegramMessenger
import grizzled.slf4j.Logging

import scala.concurrent.duration.DurationInt
import scala.math.BigDecimal.javaBigDecimal2bigDecimal

case class OpenOrder(averagePrice: Double, quantity: Double)

class ExchangeOperator(service: BinanceService, ticker: String) extends PriceHelper with TimeHelper with Logging {

  private val pricePrecision = service.getPricePrecision(ticker)

  private val quantityPrecision = service.getQuantityPrecision(ticker)

  private val coinName = ticker.replace("USDT", "")

  def buy(quantity: Double, leverage: Int): OpenOrder = {
    val order = service.buy(ticker, quantity, leverage)
    val currentOrderPrice = order.getAvgPrice.doubleValue()

    info(s"$ticker BUY TIME : ${fromMillis(order.getUpdateTime)}")
    info(s"MARGIN: ${order.getExecutedQty * currentOrderPrice / leverage}, PRICE: $currentOrderPrice")
    OpenOrder(currentOrderPrice, quantity)
  }

  def buyWithCost(usdCost: Double, leverage: Int): OpenOrder = {
    val currentPrice = service.getCurrentPrice(ticker)
    val availableQuantity = (usdCost / currentPrice)
    val orderQuantity = roundWithPrecision(availableQuantity * leverage * 0.95, quantityPrecision)
    buy(orderQuantity, leverage)
  }

  def setTakeProfit(orderPrice: Double, expectedUpPercent: Double, quantity: Double): Unit = {
    val takeProfit = addPercent(orderPrice, expectedUpPercent, pricePrecision)
    val quantityWithPrecision = roundWithPrecision(quantity, quantityPrecision)
    service.setTakeProfit(ticker, quantityWithPrecision, takeProfit)
  }

  def setStopMarket(orderPrice: Double, expectedDownPercent: Double, quantity: Double): Unit = {
    val stopLossPrice = minusPercent(orderPrice, expectedDownPercent, pricePrecision)
    val quantityWithPrecision = roundWithPrecision(quantity, quantityPrecision)
    service.setStopMarket(ticker, quantityWithPrecision, stopLossPrice)
  }

  def setTrailingStop(quantity: Double, callbackRate: Double): Unit = {
    val quantityWithPrecision = roundWithPrecision(quantity, quantityPrecision)
    service.setTrailingStop(ticker, quantityWithPrecision, callbackRate)
  }

  def monitorForStopLossChange(openOrder: OpenOrder, upActivationPercent: Double, stopMarketNewPriceUpPercent: Double): Unit = {
    Thread.sleep(5.seconds.toMillis)
    val currentStopMarketOrders = service.getOpenOrders.filter(_.getSymbol.startsWith(coinName)).filter(_.getType == OrderType.STOP_MARKET.toString).map(_.getOrderId)
    val changeStopLossActivationPrice = addPercent(openOrder.averagePrice, upActivationPercent, pricePrecision)
    val newStopMarketPrice = addPercent(openOrder.averagePrice, stopMarketNewPriceUpPercent, pricePrecision)

    var currentPrice = service.fetchCurrentPrice(ticker)
    while (currentPrice < changeStopLossActivationPrice && service.getOpenPositions(coinName).nonEmpty) {
      info(s"MONITORING FOR CHANGING STOP LOSS PRICE: $currentPrice, EXPECTED $changeStopLossActivationPrice")
      Thread.sleep(500)
      currentPrice = service.fetchCurrentPrice(ticker)
    }

    info(s"SETTING NEW STOP LOSS PRICE: [$ticker, qty = ${openOrder.quantity}, newStopMarketPrice = $newStopMarketPrice, activationPrice = $changeStopLossActivationPrice]")

    if (service.getOpenPositions(coinName).nonEmpty) {
      service.setStopMarket(ticker, openOrder.quantity, newStopMarketPrice)
      currentStopMarketOrders.foreach(x => {
        service.removeOrder(ticker, x)
      })
    }
  }

  def monitorForStopLossCancellation(telegramClient: TelegramMessenger, exchangeInformation: ExchangeInformation): Unit = {
    val balanceBefore = exchangeInformation.currentBalance
    Thread.sleep(1.seconds.toMillis)
    while (service.getOpenPositions(coinName).nonEmpty) {
      Thread.sleep(2.seconds.toMillis)
    }
    service.getOpenOrders.filter(_.getSymbol.startsWith(coinName)).map(_.getOrderId).foreach(orderId =>
      service.removeOrder(ticker, orderId)
    )
    val balanceAfter = service.getBalance.filter(_.getAsset == "USDT").map(_.getBalance).head.doubleValue()
    val profit = roundWithPrecision(balanceAfter - balanceBefore, precision = 2)
    telegramClient.sendMessage(s"$coinName PROFIT: $$$profit")
  }

}
