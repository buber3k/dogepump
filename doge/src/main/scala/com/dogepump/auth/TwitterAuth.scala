package com.dogepump.auth

case class TwitterAuth(consumerKey: String,
                       consumerSecret: String,
                       accessToken: String,
                       secretToken: String) extends Auth