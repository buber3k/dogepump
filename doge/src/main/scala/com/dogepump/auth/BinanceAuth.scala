package com.dogepump.auth

case class BinanceAuth(apiKey: String,
                       secretKey: String) extends Auth


case class BinanceConfiguration(auth: BinanceAuth,
                                apiBaseUrl: String,
                                wsApiBaseUrl: String)
