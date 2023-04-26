package com.dogepump.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage

class TelegramMessenger(token: String, chatId: Long) {

  val telegramBot = new TelegramBot(token)

  def sendMessage(message: String): Unit = {
    val request = new SendMessage(chatId, message)
    telegramBot.execute(request)
  }
}
