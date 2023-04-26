package com.dogepump.helper

trait PriceHelper {

  protected def addPercent(price: Double, percentProfit: Double, precision: Int): Double = {
    val expectedPrice = ((percentProfit + 100) / 100) * price
    BigDecimal(expectedPrice).setScale(precision, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  protected def minusPercent(price: Double, percentLoss: Double, precision: Int): Double = {
    val expectedPrice = ((100 - percentLoss) / 100) * price
    BigDecimal(expectedPrice).setScale(precision, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  protected def roundWithPrecision(price: Double, precision: Int): Double = {
    BigDecimal(price).setScale(precision, BigDecimal.RoundingMode.DOWN).toDouble
  }
}
