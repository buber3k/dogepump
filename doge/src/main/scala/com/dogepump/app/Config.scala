package com.dogepump.app

import java.io.File
import java.io.FileInputStream
import java.util.Properties

import com.dogepump.auth.BinanceAuth
import com.dogepump.auth.BinanceConfiguration
import com.dogepump.auth.TwitterAuth
import com.dogepump.auth.YoutubeAuth
import com.dogepump.exchange.ExchangeInformation

import scala.concurrent.duration.DurationInt

object PropertiesReader {
  val properties: Properties = {
    val applicationProperties = new Properties()
    val configurationLocation = System.getProperty("configuration.location")
    val fileStream = new FileInputStream(new File(configurationLocation))
    applicationProperties.load(fileStream)
    fileStream.close()
    applicationProperties
  }
}

object Config {

  object Implicits {
    implicit val youtubeAuth: YoutubeAuth = YoutubeAuth(YOUTUBE_API_KEY)
    implicit val twitterAuth: TwitterAuth = TwitterAuth(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET, TWITTER_ACCESS_TOKEN, TWITTER_SECRET_TOKEN)
    implicit val binanceAuth: BinanceAuth = Config.BINANCE_CONFIGURATION.auth
    implicit val exchangeInformation: ExchangeInformation = new ExchangeInformation()
  }

  import PropertiesReader.properties

  val TWITTER_CONSUMER_KEY: String = properties.getProperty("twitter.consumerKey")
  val TWITTER_CONSUMER_SECRET: String = properties.getProperty("twitter.secretKey")
  val TWITTER_ACCESS_TOKEN: String = properties.getProperty("twitter.accessToken")
  val TWITTER_SECRET_TOKEN: String = properties.getProperty("twitter.secretToken")

  val YOUTUBE_API_KEY: String = properties.getProperty("youtube.apiKey")

  val DISCORD_TOKEN: String = properties.getProperty("discord.token")

  val EXPECTED_PROFIT: Double = properties.getProperty("expectedProfit").toDouble

  val EXPECTED_DOWN_PERCENT: Double = properties.getProperty("expectedDownPercent").toDouble

  val EXPECTED_JOHN_PROFIT: Double = properties.getProperty("expectedJohnProfit").toDouble

  val EXPECTED_ELIZ_PROFIT: Double = properties.getProperty("expectedElizProfit").toDouble

  val USD_COST: Int = properties.getProperty("usdCost").toInt

  val LEVERAGE: Int = properties.getProperty("leverage").toInt

  val UP_ACTIVATION_PERCENT: Double = properties.getProperty("upActivationPercent").toDouble

  val NEW_STOP_MARKET_PERCENT: Double = properties.getProperty("newStopMarketPercent").toDouble

  val CHALLENGE_PROFIT: Double = properties.getProperty("challengeProfit").toDouble

  val CHALLENGE_ENABLED: Boolean = properties.getProperty("challengeEnabled").toBoolean

  val TELEGRAM_CHAT_ID: Long = properties.getProperty("telegram.chatId").toLong

  val TELEGRAM_BOT_TOKEN: String = properties.getProperty("telegram.botToken")

  val BINANCE_CONFIGURATION: BinanceConfiguration = {
    val testMode = properties.getProperty("testMode").toBoolean
    if (testMode) {
      println("RUNNING TEST MODE")
      TEST_BINANCE_CONFIGURATION
    } else {
      println("!!!!!!!!!! RUNNING REAL MODE !!!!!!!!!!!!!")
      println("!!!!!!!!!!  CTRL-C TO ABORT. !!!!!!!!!!!!!")
      for (i <- 1.to(3).reverse) {
        print(s"$i... ")
        Thread.sleep(1.second.toMillis)
      }
      println()
      REAL_BINANCE_CONFIGURATION
    }
  }

  def REAL_BINANCE_CONFIGURATION: BinanceConfiguration = BinanceConfiguration(
    BinanceAuth(properties.getProperty("binance.apiKey"), properties.getProperty("binance.secretKey")),
    "https://fapi.binance.com", "wss://fstream.binance.com/ws"
  )

  def TEST_BINANCE_CONFIGURATION: BinanceConfiguration = BinanceConfiguration(
    BinanceAuth(properties.getProperty("binance.testApiKey"), properties.getProperty("binance.testSecretKey")),
    "https://testnet.binancefuture.com", "wss://stream.binancefuture.com"
  )

  val ELON_ID = 44196397L
  val BUBA_ID = 3238513305L
  val COINBASE_PRO_ID = 720487892670410753L

  val TRADES_CHANNEL_ID = 835157438092279818L

  val TEST_CHANNEL_ID = 828734442918772796L

  val TEST_SERVER_ID = 826853324618727425L

  val CHALLENGE_CHANNEL_ID = 832991478246408242L

  val TRADES_SERVER_ID = 742797926761234463L

  val PLAYLIST_ID_BUBA = "UUlqam3ctPfm8m52igi6Wc6w"
  val PLAYLIST_ID_COIN_BUREAU = "UUqK_GSMbpiV8spgD3ZGloSw"

  val notCoinbaseCoins = List(
    "TRX", "DOGE", "THETA", "HNT", "DENT", "HOT", "KAVA", "KSM",
    "SXP", "CHZ", "AVAX", "ONT", "VET", "EGLD", "BTS", "LUNA", "QTUM", "NEO",
    "SOL", "FTM", "FLM", "TOMO", "BLZ", "ENJ", "ZEN", "ONE", "BEL", "REEF",
    "TRB", "1INCH", "ZIL", "YFII", "IOST", "SFP", "ALPHA", "ICX", "RSR", "COTI",
    "RUNE", "XMR", "SAND", "STMX", "AKRO", "CTK", "SRM", "NEAR", "AXS", "RLC",
    "MTL", "XEM", "BZRX", "LINA", "CHR", "ALICE", "HBAR", "WAVES", "CELR",
    "DODO", "OCEAN", "RVN", "LIT", "OGN", "UNFI", "DEFI",
  )

  val coinbaseCoins = Set("1INCH", "AAVE", "ADA", "ALGO", "ANKR", "ATOM",
    "BAL", "BAND", "BAT", "BCH", "BNT", "BSV", "BTC", "CGLD", "COMP", "CRV",
    "CVC", "DAI", "DASH", "DNT", "ENJ", "EOS", "ETC", "ETH",
    "FIL", "GRT", "GNT", "KNC", "LINK", "LOOM", "LRC", "LTC", "MANA",
    "MATIC", "MKR", "NMR", "NKN", "NU", "OGN", "OMG", "OXT", "REN", "REP", "SUSHI",
    "SKL", "SNX", "STORJ", "USDC", "UMA", "UNI", "WBTC", "XLM", "XRP", "XTZ", "YFI", "ZEC", "ZRX")

  val binanceFuturesCoins = Set(
    "SUSHI", "CVC", "BTS", "HOT", "ZRX", "QTUM", "IOTA", "WAVES", "LIT",
    "XTZ", "BNB", "AKRO", "HNT", "ETC", "XMR", "YFI", "ALICE", "ALPHA", "SFP", "REEF", "BAT",
    "RLC", "TRX", "STORJ", "SNX", "XLM", "NEO", "UNFI", "SAND", "DASH", "KAVA", "RUNE",
    "CTK", "LINK", "CELR", "RSR", "SKL", "REN", "TOMO", "MTL", "LTC", "DODO", "KSM", "EGLD",
    "VET", "ONT", "TRB", "MANA", "COTI", "CHR", "GRT", "FLM", "EOS", "OGN", "BAL", "STMX",
    "LUNA", "DENT", "KNC", "SRM", "ENJ", "ZEN", "ATOM", "NEAR", "BCH", "IOST", "HBAR", "ZEC",
    "BZRX", "AAVE", "ALGO", "LRC", "AVAX", "MATIC", "1INCH", "MKR", "THETA", "UNI", "LINA",
    "RVN", "FIL", "DEFI", "COMP", "SOL", "OMG", "ICX", "BLZ", "FTM", "YFII", "BAND",
    "XRP", "SXP", "CRV", "BEL", "DOT", "XEM", "ONE", "ZIL", "AXS", "OCEAN", "CHZ", "ANKR"
  )

  // https://www.googleapis.com/youtube/v3/channels?id=UClqam3ctCfm8m52igi6Wc6w&key=AIzaSyBRAnmfrRb9FIWpox2Lt0Nao6yX2qLXX6Q&part=contentDetails
  // https://www.googleapis.com/youtube/v3/playlistItems?part=snippet%2CcontentDetails&maxResults=50&playlistId=UUlqam3ctCfm8m52igi6Wc6w&key=AIzaSyBRAnmfrRb9FIWpox2Lt0Nao6yX2qLXX6Q
}