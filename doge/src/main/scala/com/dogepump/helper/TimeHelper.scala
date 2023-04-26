package com.dogepump.helper

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import scala.concurrent.duration.Duration

trait TimeHelper {

  private val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

  def timeNow(): Instant = {
    Instant.now()
  }

  def timeNowString(): String = {
    val now = LocalDateTime.now(ZoneId.systemDefault)
    dtf.format(now)
  }

  def fromInstant(instant: Instant): String = {
    dtf.format(toLocalDateTime(instant))
  }


  def fromMillis(millis: Long): String = {
    val t = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault)
    dtf.format(t)
  }

  def toLocalDateTime(instant: Instant): LocalDateTime = {
    LocalDateTime.ofInstant(instant, ZoneId.systemDefault)
  }

  def timeIsOk(createdTime: Instant, atMost: Duration): Boolean = {
    createdTime.plusMillis(atMost.toMillis).isAfter(Instant.now())
  }

  def timeIsOk(createdTime: com.google.api.client.util.DateTime, atMost: Duration): Boolean = {
    Instant.ofEpochMilli(createdTime.getValue).plusMillis(atMost.toMillis).isAfter(Instant.now())
  }

}
