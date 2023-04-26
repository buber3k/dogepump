package com.dogepump.app

import com.dogepump.app.Config.Implicits._
import com.dogepump.exchange.BinanceService
import com.dogepump.exchange.ExchangeOperator
import com.dogepump.helper.PriceHelper

object BinanceTest extends App with PriceHelper {
  val exchangeService = new BinanceService()
  val positions = exchangeService.getOpenPositions("HBAR")
  println(positions)
}
