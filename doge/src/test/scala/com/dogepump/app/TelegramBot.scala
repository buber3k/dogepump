package com.dogepump.app

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage

object TelegramBot extends App {
  val bot = new TelegramBot("BOT.ID")
  val request = new SendMessage(-CHAT.ID, "Hello!")
  bot.execute(request)

}
