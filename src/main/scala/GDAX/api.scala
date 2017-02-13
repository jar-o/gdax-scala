package GDAX.api

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scalaj.http._
import scala.util.parsing.json._
import scalaj.http.Base64
import scalaj.http.HttpConstants._

import java.net._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Client {
  def public() : Public = {
    return new Public()
  }

  def authenticated(
      apiKey: String,
      secretKey: String,
      passPhrase: String) : Auth = {
    return new Auth(apiKey, secretKey, passPhrase)
  }

  def sleep(duration: Long) { Thread.sleep(duration) }
}

object HttpJsonFutures {
  def get(url: String, params: Map[String, String] = null) : Future[Option[Any]] = {
    return Future {
      val resp: HttpResponse[String] = if (params == null)
        Http(url).asString else Http(url).params(params).asString
      JSON.parseFull(resp.body)
    }
  }
}

class Public {
  // Default to sandbox environment
  var url = "https://api-public.sandbox.gdax.com"

  def products() : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + "/products")
  }

  def productOrderBook(id : String, level : String = "1") : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + s"/products/$id/book", Map("level" -> level))
  }

  def productTicker(id: String) : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + s"/products/$id/ticker")
  }

  def productTrades(id : String) : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + s"/products/$id/trades")
  }

  // 24hr stats
  def productStats(id : String) : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + s"/products/$id/stats")
  }

  //TODO
  //def productHistoricRates(id : String) : Future[Option[Any]] = {}

  def currencies() : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + "/currencies")
  }

  def time() : Future[Option[Any]] = {
    return HttpJsonFutures.get(url + "/time")
  }

}

class Auth(apiKey: String, secretKey: String, passPhrase: String) {
  // Default to sandbox environment
  var url = "https://api-public.sandbox.gdax.com"

  var httpreq : HttpRequest = Http(url + "/accounts")//.postData("{\"helo\":1}")
  val auth = CoinbaseAuth(
    apiKey,
    secretKey,
    passPhrase,
    httpreq//,
    //"{\"helo\":1}" //ugh, TODO
  ).headers()
  for ((k,v) <- auth) { httpreq.header(k,v) }
  val resp: HttpResponse[String] = httpreq.asString
  println(resp)
}

case class CoinbaseAuth(
    apiKey: String, secretKey: String, passPhrase: String,
    httpreq: HttpRequest, data: String = "") { //TODO how to get at in httpreq

  def headers() : Map[String, String] = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val reqpath: String = new URL(httpreq.url).getPath()

    val message = timestamp + httpreq.method.toUpperCase + reqpath + data
    println(s"message $message")
    val hmacKey: Array[Byte] = Base64.decode(secretKey)
    val secret = new SecretKeySpec(hmacKey, "HmacSHA256")
    val hmac = Mac.getInstance("HmacSHA256")
    hmac.init(secret)

    Map(
      "CB-ACCESS-SIGN" -> base64(hmac.doFinal(message.getBytes)),
      "CB-ACCESS-TIMESTAMP" -> timestamp.toString,
      "CB-ACCESS-KEY" -> apiKey,
      "CB-ACCESS-PASSPHRASE" -> passPhrase
      )
  }

}
