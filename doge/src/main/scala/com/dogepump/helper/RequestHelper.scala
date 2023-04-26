package com.dogepump.helper

import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.dogepump.auth.BinanceAuth

trait RequestHelper {
  def createRequestClient(implicit auth: BinanceAuth): SyncRequestClient = {
    val options = new RequestOptions()
    SyncRequestClient.create(auth.apiKey, auth.secretKey, options)
  }
}
